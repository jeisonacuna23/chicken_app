package com.pollosk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private Bitmap bitmapGenerado;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_recibo);
        setTitle("Recibo");

        long ventaId = getIntent().getLongExtra("ventaId", -1);
        if (ventaId < 0) { finish(); return; }

        VentaDAO ventaDAO = new VentaDAO(this);
        ItemVentaDAO itemDAO = new ItemVentaDAO(this);

        venta = ventaDAO.buscarPorId(ventaId);
        if (venta == null) { finish(); return; }
        items = itemDAO.listarPorVenta(ventaId);

        ((TextView) findViewById(R.id.tvReciboRef)).setText(venta.referencia);
        ((TextView) findViewById(R.id.tvReciboFecha)).setText(venta.fecha);
        ((TextView) findViewById(R.id.tvReciboCliente)).setText("Cliente: " + venta.cliente);
        ((TextView) findViewById(R.id.tvReciboPrecio)).setText(
                "Precio por kilo: " + formatoCOP(venta.precioKg));

        LinearLayout cont = findViewById(R.id.llItems);
        LayoutInflater inf = LayoutInflater.from(this);
        for (ItemVenta it : items) {
            View fila = inf.inflate(R.layout.item_linea_recibo, cont, false);
            ((TextView) fila.findViewById(R.id.tvLineaPeso)).setText(
                    String.format(Locale.US, "• %.3f kg", it.pesoKg));
            ((TextView) fila.findViewById(R.id.tvLineaSubtotal)).setText(formatoCOP(it.subtotal));
            cont.addView(fila);
        }

        ((TextView) findViewById(R.id.tvReciboTotal)).setText("Total: " + formatoCOP(venta.total));
        ((TextView) findViewById(R.id.tvReciboMetodo)).setText("Método de pago: " + venta.metodoPago);

        ((Button) findViewById(R.id.btnCompartir)).setOnClickListener(v -> generarPrevia());

        ((Button) findViewById(R.id.btnVolverMenu)).setOnClickListener(x -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    private void generarPrevia() {
        try {
            bitmapGenerado = ReciboImagen.generarBitmap(this, venta, items);
            
            if (bitmapGenerado == null) {
                Toast.makeText(this, "Error: La imagen salió vacía", Toast.LENGTH_LONG).show();
                return;
            }

            ImageView preview = findViewById(R.id.ivPreviewRecibo);
            preview.setImageBitmap(bitmapGenerado);
            preview.setVisibility(View.VISIBLE);

            Button btn = findViewById(R.id.btnCompartir);
            btn.setText("Compartir por WhatsApp");
            btn.setOnClickListener(v -> compartirImagen());
            
            Toast.makeText(this, "Previa generada. Desliza hacia abajo para verla.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error al generar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void compartirImagen() {
        if (bitmapGenerado != null) {
            Uri uri = ReciboImagen.guardarYCompartir(this, bitmapGenerado, "recibo-" + venta.referencia);
            if (uri != null) {
                ReciboImagen.compartir(this, uri, venta.referencia);
            } else {
                Toast.makeText(this, "Error al preparar el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }
}
