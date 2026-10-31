package com.pollosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.util.List;
import java.util.Locale;

public class PagadosActivity extends Activity {

    private VentaDAO ventaDAO;
    private ItemVentaDAO itemDAO;
    private ListView lv;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_pagados);
        setTitle("Pagados");
        ventaDAO = new VentaDAO(this);
        itemDAO = new ItemVentaDAO(this);
        lv = findViewById(R.id.lvPagados);
        tvVacio = findViewById(R.id.tvPagadosVacio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar();
    }

    private void refrescar() {
        List<Venta> lista = ventaDAO.listarPagadas();
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

    private class Adaptador extends ArrayAdapter<Venta> {
        Adaptador(List<Venta> lista) {
            super(PagadosActivity.this, R.layout.item_venta, lista);
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
                Intent i = new Intent(PagadosActivity.this, EditarVentaActivity.class);
                i.putExtra("ventaId", v.id);
                startActivity(i);
            });

            // En Pagados no se elimina desde aquí
            cv.findViewById(R.id.btnItemEliminar).setVisibility(View.GONE);

            return cv;
        }
    }
}
