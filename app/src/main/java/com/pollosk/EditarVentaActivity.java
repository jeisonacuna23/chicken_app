package com.pollosk;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditarVentaActivity extends Activity {

    private VentaDAO ventaDAO;
    private ItemVentaDAO itemDAO;
    private Venta venta;
    private List<ItemVenta> items;
    private final List<EditText> etPesos = new ArrayList<>();

    private EditText etPrecio;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_editar_venta);
        setTitle("Editar venta");

        ventaDAO = new VentaDAO(this);
        itemDAO = new ItemVentaDAO(this);

        long ventaId = getIntent().getLongExtra("ventaId", -1);
        venta = ventaDAO.buscarPorId(ventaId);
        if (venta == null) { finish(); return; }
        items = itemDAO.listarPorVenta(ventaId);

        ((TextView) findViewById(R.id.tvEditarRef)).setText(venta.referencia);
        ((TextView) findViewById(R.id.tvEditarCliente)).setText(
                "Cliente: " + venta.cliente + " · " + venta.metodoPago);

        etPrecio = findViewById(R.id.etEditarPrecio);
        etPrecio.setText(String.format(Locale.US, "%.0f", venta.precioKg));

        tvTotal = findViewById(R.id.tvEditarTotal);
        LinearLayout cont = findViewById(R.id.llEditarItems);

        for (ItemVenta it : items) {
            EditText et = new EditText(this);
            et.setHint("Peso en kg");
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                    | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            et.setText(String.format(Locale.US, "%.2f", it.pesoKg));
            et.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                public void onTextChanged(CharSequence s, int a, int b, int c) { recalcular(); }
                public void afterTextChanged(Editable s) {}
            });
            cont.addView(et);
            etPesos.add(et);
        }

        etPrecio.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { recalcular(); }
            public void afterTextChanged(Editable s) {}
        });

        ((Button) findViewById(R.id.btnEditarGuardar)).setOnClickListener(x -> guardar());

        recalcular();
    }

    private double leerPrecio() {
        String t = etPrecio.getText().toString().trim().replace(',', '.');
        if (t.isEmpty()) return 0;
        try { return Double.parseDouble(t); } catch (Exception e) { return 0; }
    }

    private double leerPeso(int i) {
        String t = etPesos.get(i).getText().toString().trim().replace(',', '.');
        if (t.isEmpty()) return 0;
        try { return Double.parseDouble(t); } catch (Exception e) { return 0; }
    }

    private void recalcular() {
        double precio = leerPrecio();
        double total = 0;
        for (int i = 0; i < etPesos.size(); i++) total += leerPeso(i) * precio;
        tvTotal.setText("Nuevo total: " + formatoCOP(total));
    }

    private void guardar() {
        double precio = leerPrecio();
        if (precio < 1) { toast("El precio por kilo debe ser al menos 1"); return; }

        for (int i = 0; i < etPesos.size(); i++) {
            if (leerPeso(i) <= 0) { toast("Todos los pesos deben ser mayores a 0"); return; }
        }

        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemVenta it = items.get(i);
            double peso = leerPeso(i);
            double sub = peso * precio;
            it.pesoKg = peso;
            it.subtotal = sub;
            total += sub;

            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put("peso_kg", peso);
            cv.put("subtotal", sub);
            com.pollosk.db.DBHelper.get(this).getWritableDatabase()
                    .update("items_venta", cv, "id = ?",
                            new String[]{String.valueOf(it.id)});

            android.content.ContentValues cvPollo = new android.content.ContentValues();
            cvPollo.put("peso_kg", peso);
            com.pollosk.db.DBHelper.get(this).getWritableDatabase()
                    .update("pollos", cvPollo, "id = ?",
                            new String[]{String.valueOf(it.polloId)});
        }

        venta.precioKg = precio;
        venta.total = total;
        ventaDAO.actualizar(venta);

        toast("Cambios guardados");
        finish();
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
