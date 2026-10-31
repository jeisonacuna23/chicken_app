package com.pollosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.db.PolloDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Pollo;
import com.pollosk.model.Venta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VenderActivity extends Activity {

    private PolloDAO polloDAO;
    private VentaDAO ventaDAO;
    private ItemVentaDAO itemDAO;

    private EditText etCliente, etPrecioKg;
    private LinearLayout llPollos;
    private TextView tvSinPollos, tvTotalPreview;
    private RadioGroup rgMetodo;

    private final List<Pollo> pollosDisponibles = new ArrayList<>();
    private final List<CheckBox> checkboxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_vender);
        setTitle("Registrar venta");

        polloDAO = new PolloDAO(this);
        ventaDAO = new VentaDAO(this);
        itemDAO = new ItemVentaDAO(this);

        etCliente = findViewById(R.id.etCliente);
        etPrecioKg = findViewById(R.id.etPrecioKg);
        llPollos = findViewById(R.id.llPollos);
        tvSinPollos = findViewById(R.id.tvSinPollos);
        tvTotalPreview = findViewById(R.id.tvTotalPreview);
        rgMetodo = findViewById(R.id.rgMetodo);

        TextWatcher tw = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { actualizarTotal(); }
            public void afterTextChanged(Editable s) {}
        };
        etPrecioKg.addTextChangedListener(tw);

        ((Button) findViewById(R.id.btnGenerar)).setOnClickListener(v -> generarVenta());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPollos();
        actualizarTotal();
    }

    private void cargarPollos() {
        pollosDisponibles.clear();
        pollosDisponibles.addAll(polloDAO.listarDisponibles());

        llPollos.removeAllViews();
        checkboxes.clear();

        if (pollosDisponibles.isEmpty()) {
            tvSinPollos.setVisibility(View.VISIBLE);
            return;
        }
        tvSinPollos.setVisibility(View.GONE);

        for (Pollo p : pollosDisponibles) {
            CheckBox cb = new CheckBox(this);
            cb.setText(String.format(Locale.US, "%.2f kg", p.pesoKg));
            cb.setTag(p.id);
            cb.setOnCheckedChangeListener((v, c) -> actualizarTotal());
            llPollos.addView(cb);
            checkboxes.add(cb);
        }
    }

    private double leerPrecio() {
        String t = etPrecioKg.getText().toString().trim().replace(',', '.');
        if (t.isEmpty()) return 0;
        try { return Double.parseDouble(t); } catch (Exception e) { return 0; }
    }

    private List<Pollo> pollosSeleccionados() {
        List<Pollo> out = new ArrayList<>();
        for (int i = 0; i < checkboxes.size(); i++) {
            if (checkboxes.get(i).isChecked()) out.add(pollosDisponibles.get(i));
        }
        return out;
    }

    private void actualizarTotal() {
        double precio = leerPrecio();
        double total = 0;
        for (Pollo p : pollosSeleccionados()) total += p.pesoKg * precio;
        tvTotalPreview.setText("Total: " + formatoCOP(total));
    }

    private void generarVenta() {
        String cliente = etCliente.getText().toString().trim();
        if (cliente.isEmpty()) { toast("Falta el nombre del cliente"); return; }

        double precio = leerPrecio();
        if (precio < 1) { toast("El precio por kilo debe ser al menos 1"); return; }

        List<Pollo> sel = pollosSeleccionados();
        if (sel.isEmpty()) { toast("Selecciona al menos un pollo"); return; }

        String metodo;
        int id = rgMetodo.getCheckedRadioButtonId();
        if (id == R.id.rbNequi) metodo = Venta.METODO_NEQUI;
        else if (id == R.id.rbFiado) metodo = Venta.METODO_FIADO;
        else metodo = Venta.METODO_EFECTIVO;

        double total = 0;
        for (Pollo p : sel) total += p.pesoKg * precio;

        Venta v = new Venta();
        v.referencia = ventaDAO.generarReferencia();
        v.cliente = cliente;
        v.precioKg = precio;
        v.metodoPago = metodo;
        v.fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        v.total = total;

        long ventaId = ventaDAO.insertar(v);
        if (ventaId <= 0) { toast("Error al guardar la venta"); return; }

        List<Long> ids = new ArrayList<>();
        for (Pollo p : sel) {
            double sub = p.pesoKg * precio;
            itemDAO.insertar(new ItemVenta(ventaId, p.id, p.pesoKg, sub));
            ids.add(p.id);
        }
        polloDAO.marcarVendidos(ids, ventaId);

        Intent i = new Intent(this, ReciboActivity.class);
        i.putExtra("ventaId", ventaId);
        startActivity(i);
        finish();
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
