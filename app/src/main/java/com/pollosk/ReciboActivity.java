package com.pollosk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.util.List;
import java.util.Locale;

public class ReciboActivity extends Activity {

    private Venta venta;
    private List<ItemVenta> items;
    private Typeface courier;
    private Typeface courierBold;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_recibo);
        setTitle("Recibo");

        try {
            courier = Typeface.createFromAsset(getAssets(), "fonts/CourierPrime-Regular.ttf");
            courierBold = Typeface.createFromAsset(getAssets(), "fonts/CourierPrime-Bold.ttf");
        } catch (Exception e) {
            courier = Typeface.MONOSPACE;
            courierBold = Typeface.MONOSPACE;
        }

        long ventaId = getIntent().getLongExtra("ventaId", -1);
        if (ventaId < 0) { finish(); return; }

        VentaDAO ventaDAO = new VentaDAO(this);
        ItemVentaDAO itemDAO = new ItemVentaDAO(this);

        venta = ventaDAO.buscarPorId(ventaId);
        if (venta == null) { finish(); return; }
        items = itemDAO.listarPorVenta(ventaId);

        TextView tvRef = findViewById(R.id.tvReciboRef);
        tvRef.setTypeface(courierBold);

        TextView tvFecha = findViewById(R.id.tvReciboFecha);
        tvFecha.setTypeface(courier);

        TextView tvCliente = findViewById(R.id.tvReciboCliente);
        tvCliente.setTypeface(courier);

        TextView tvPrecio = findViewById(R.id.tvReciboPrecio);
        tvPrecio.setTypeface(courier);

        tvRef.setText(venta.referencia);
        tvFecha.setText(venta.fecha);
        tvCliente.setText("Cliente: " + venta.cliente);
        tvPrecio.setText("Precio por kilo: " + formatoCOP(venta.precioKg));

        LinearLayout cont = findViewById(R.id.llItems);
        LayoutInflater inf = LayoutInflater.from(this);
        for (ItemVenta it : items) {
            View fila = inf.inflate(R.layout.item_linea_recibo, cont, false);
            TextView tvPeso = fila.findViewById(R.id.tvLineaPeso);
            TextView tvSub = fila.findViewById(R.id.tvLineaSubtotal);
            tvPeso.setTypeface(courier);
            tvSub.setTypeface(courierBold);
            tvPeso.setText(String.format(Locale.US, "%.2f kg", it.pesoKg));
            tvSub.setText(formatoCOP(it.subtotal));
            cont.addView(fila);
        }

        TextView tvTotal = findViewById(R.id.tvReciboTotal);
        tvTotal.setTypeface(courierBold);
        tvTotal.setText("Total: " + formatoCOP(venta.total));

        TextView tvMetodo = findViewById(R.id.tvReciboMetodo);
        tvMetodo.setTypeface(courier);
        tvMetodo.setText("Metodo de pago: " + venta.metodoPago);

        ((Button) findViewById(R.id.btnCompartir)).setOnClickListener(v -> compartir());

        ((Button) findViewById(R.id.btnVolverMenu)).setOnClickListener(x -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    private void compartir() {
        try {
            Uri uri = ReciboImagen.generarYGuardar(this, venta, items);
            if (uri == null) {
                toast("No se pudo generar la imagen");
                return;
            }
            ReciboImagen.compartir(this, uri, venta.referencia);
        } catch (Exception e) {
            toast("Error: " + e.getMessage());
        }
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}
