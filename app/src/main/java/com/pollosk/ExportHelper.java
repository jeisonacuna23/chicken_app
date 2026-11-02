package com.pollosk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.pollosk.db.ItemVentaDAO;
import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

public class ExportHelper {

    public static void exportarVentas(Context ctx, List<Venta> ventas) {
        try {
            File dir = new File(ctx.getCacheDir(), "exports");
            dir.mkdirs();
            File file = new File(dir, "historial_ventas.csv");

            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");

            // Escribimos los encabezados (separados por punto y coma para que Excel en español lo lea bien)
            writer.write("Referencia;Cliente;Fecha;Metodo de pago;Cant. Pollos;Total\n");

            ItemVentaDAO itemDAO = new ItemVentaDAO(ctx);

            for (Venta v : ventas) {
                List<ItemVenta> items = itemDAO.listarPorVenta(v.id);
                int cantPollos = items.size();
                
                // Reemplazamos la coma decimal por punto para evitar conflictos
                String totalStr = String.format(Locale.US, "%.2f", v.total);
                
                writer.write(v.referencia + ";" +
                        v.cliente + ";" +
                        v.fecha + ";" +
                        v.metodoPago + ";" +
                        cantPollos + ";" +
                        totalStr + "\n");
            }

            writer.close();
            fos.close();

            Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Historial de Ventas - Chicken");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(Intent.createChooser(intent, "Compartir o abrir con..."));

        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(ctx, "Error al exportar: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
