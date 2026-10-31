package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.db.PolloDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.util.List;
import java.util.Locale;

public class HistorialActivity extends Activity {

    private VentaDAO ventaDAO;
    private ItemVentaDAO itemDAO;
    private PolloDAO polloDAO;
    private ListView lvHistorial;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_historial);
        setTitle("Historial");

        ventaDAO = new VentaDAO(this);
        itemDAO = new ItemVentaDAO(this);
        polloDAO = new PolloDAO(this);
        lvHistorial = findViewById(R.id.lvHistorial);
        tvVacio = findViewById(R.id.tvHistorialVacio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar();
    }

    private void refrescar() {
        List<Venta> ventas = ventaDAO.listarTodas();
        if (ventas.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            lvHistorial.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            lvHistorial.setVisibility(View.VISIBLE);
            lvHistorial.setAdapter(new Adaptador(ventas));
        }
    }

    private void eliminar(final Venta v) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar venta")
            .setMessage("Se eliminará " + v.referencia + " y los pollos volverán al inventario. ¿Continuar?")
            .setPositiveButton("Sí, eliminar", (d, w) -> {
                polloDAO.devolverAlInventario(v.id);
                itemDAO.eliminarPorVenta(v.id);
                ventaDAO.eliminar(v.id);
                toast("Venta eliminada. Pollos devueltos al inventario.");
                refrescar();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private class Adaptador extends ArrayAdapter<Venta> {
        Adaptador(List<Venta> lista) {
            super(HistorialActivity.this, R.layout.item_venta, lista);
        }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            if (cv == null) cv = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_venta, parent, false);
            Venta v = getItem(pos);

            ((TextView) cv.findViewById(R.id.tvItemRef)).setText(v.referencia);
            ((TextView) cv.findViewById(R.id.tvItemTotal)).setText(formatoCOP(v.total));
            ((TextView) cv.findViewById(R.id.tvItemCliente)).setText("Cliente: " + v.cliente);

            List<ItemVenta> items = itemDAO.listarPorVenta(v.id);
            ((TextView) cv.findViewById(R.id.tvItemInfo)).setText(
                    items.size() + " pollo(s) · " + v.metodoPago + " · " + v.fecha);

            ((Button) cv.findViewById(R.id.btnItemEditar)).setOnClickListener(x -> {
                Intent i = new Intent(HistorialActivity.this, EditarVentaActivity.class);
                i.putExtra("ventaId", v.id);
                startActivity(i);
            });

            ((Button) cv.findViewById(R.id.btnItemEliminar)).setOnClickListener(x ->
                    eliminar(v));

            return cv;
        }
    }
}
