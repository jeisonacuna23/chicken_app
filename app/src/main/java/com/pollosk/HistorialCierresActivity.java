package com.pollosk;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pollosk.db.CierreDAO;
import com.pollosk.model.Cierre;

import java.util.List;
import java.util.Locale;

public class HistorialCierresActivity extends Activity {

    private ListView lv;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_historial_cierres);
        setTitle("Cierres Anteriores");
        lv = findViewById(R.id.lvCierres);
        tvVacio = findViewById(R.id.tvCierresVacio);

        CierreDAO dao = new CierreDAO(this);
        List<Cierre> lista = dao.listarTodos();

        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            lv.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            lv.setAdapter(new Adaptador(lista));
        }
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private class Adaptador extends ArrayAdapter<Cierre> {
        Adaptador(List<Cierre> lista) {
            super(HistorialCierresActivity.this, R.layout.item_cierre, lista);
        }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            if (cv == null) cv = LayoutInflater.from(getContext()).inflate(R.layout.item_cierre, parent, false);
            Cierre c = getItem(pos);

            ((TextView) cv.findViewById(R.id.tvCierreItemFecha)).setText(c.fecha);
            ((TextView) cv.findViewById(R.id.tvCierreItemInfo)).setText(
                    c.cantidadPollos + " pollos · " + String.format(Locale.US, "%.3f kg", c.pesoTotal));
            ((TextView) cv.findViewById(R.id.tvCierreItemTotal)).setText(formatoCOP(c.montoTotal));

            return cv;
        }
    }
}
