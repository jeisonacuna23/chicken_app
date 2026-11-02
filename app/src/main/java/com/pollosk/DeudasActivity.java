package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.VentaDAO;
import com.pollosk.model.Venta;

import java.util.List;
import java.util.Locale;

public class DeudasActivity extends Activity {

    private VentaDAO dao;
    private ListView lv;
    private TextView tvVacio;
    private EditText etBuscar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_deudas);
        setTitle("Deudas");
        dao = new VentaDAO(this);
        lv = findViewById(R.id.lvDeudas);
        tvVacio = findViewById(R.id.tvDeudasVacio);
        etBuscar = findViewById(R.id.etBuscarDeudas);

        etBuscar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { refrescar(); }
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar();
    }

    private void refrescar() {
        String query = etBuscar.getText().toString().trim();
        List<Venta> lista = query.isEmpty() ? dao.listarDeudas() : dao.buscarDeudas(query);
        
        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            lv.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            lv.setAdapter(new Adaptador(lista));
        }
    }

    private void pagar(final Venta v) {
        String[] opciones = {"Efectivo", "Nequi"};
        new AlertDialog.Builder(this)
            .setTitle("Registrar pago")
            .setItems(opciones, (d, w) -> {
                String metodo = opciones[w];
                dao.marcarComoPagada(v.id, metodo);
                Toast.makeText(this, "Pago registrado con " + metodo, Toast.LENGTH_SHORT).show();
                refrescar();
            })
            .show();
    }

    private void recordar(Venta v) {
        try {
            Bitmap bmp = ReciboImagen.generarBitmapRecordatorio(this, v);
            Uri uri = ReciboImagen.guardarYCompartir(this, bmp, "recordatorio-" + v.referencia);
            if (uri != null) {
                // TEXTO PERSONALIZADO AQUÍ
                ReciboImagen.compartir(this, uri, v.referencia, "Karen Acuña Nequi: 3025656772");
            } else {
                Toast.makeText(this, "No se pudo generar", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatoCOP(double n) { return String.format(Locale.US, "$%,.0f", n).replace(',', '.'); }

    private class Adaptador extends ArrayAdapter<Venta> {
        Adaptador(List<Venta> lista) { super(DeudasActivity.this, R.layout.item_deuda, lista); }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            if (cv == null) cv = LayoutInflater.from(getContext()).inflate(R.layout.item_deuda, parent, false);
            Venta v = getItem(pos);

            ((TextView) cv.findViewById(R.id.tvDeudaRef)).setText(v.referencia);
            ((TextView) cv.findViewById(R.id.tvDeudaMonto)).setText(formatoCOP(v.total));
            ((TextView) cv.findViewById(R.id.tvDeudaCliente)).setText("Cliente: " + v.cliente);
            ((TextView) cv.findViewById(R.id.tvDeudaFecha)).setText(v.fecha);

            ((Button) cv.findViewById(R.id.btnDeudaPagar)).setOnClickListener(x -> pagar(v));
            ((Button) cv.findViewById(R.id.btnDeudaRecordar)).setOnClickListener(x -> recordar(v));

            return cv;
        }
    }
}
