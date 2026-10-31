package com.pollosk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextPaint;

import androidx.core.content.FileProvider;

import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReciboImagen {

    private static final int ANCHO = 800;
    private static final int MARGEN = 40;
    private static final int LINEA = 42;

    public static Bitmap generarBitmap(Context ctx, Venta v, List<ItemVenta> items) {
        return dibujarRecibo(ctx, v, items);
    }
    
    public static Bitmap generarBitmapRecordatorio(Context ctx, Venta v) {
        return dibujarRecordatorio(ctx, v);
    }

    public static Uri guardarYCompartir(Context ctx, Bitmap bmp, String prefijo) {
        try {
            File cachePath = new File(ctx.getCacheDir(), "images");
            cachePath.mkdirs();
            String nombre = prefijo + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".png";
            File file = new File(cachePath, nombre);

            FileOutputStream stream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            return null;
        }
    }

    private static TextPaint paint(Context ctx, int tamano, boolean bold, Paint.Align align, boolean isRed) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(isRed ? Color.parseColor("#C62828") : Color.parseColor("#240028"));
        p.setTextSize(tamano);
        p.setTextAlign(align);
        try {
            String archivo = bold ? "fonts/CourierPrime-Bold.ttf" : "fonts/CourierPrime-Regular.ttf";
            Typeface tf = Typeface.createFromAsset(ctx.getAssets(), archivo);
            p.setTypeface(tf);
        } catch (Exception e) {
            p.setTypeface(Typeface.MONOSPACE);
        }
        p.setFakeBoldText(bold);
        return p;
    }

    // Carga el logo directo desde los recursos (el mismo del menú)
    private static Bitmap cargarLogo(Context ctx) {
        try {
            Bitmap logoOriginal = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo_app);
            if (logoOriginal == null) return null;
            
            // Lo hacemos que ocupe casi todo el ancho del recibo (700px de 800px)
            int nuevoAncho = 700; 
            int nuevoAlto = (int) (((float) logoOriginal.getHeight() / logoOriginal.getWidth()) * nuevoAncho);
            return Bitmap.createScaledBitmap(logoOriginal, nuevoAncho, nuevoAlto, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap dibujarRecibo(Context ctx, Venta v, List<ItemVenta> items) {
        int alto = 400 + LINEA * (6 + items.size() + 6); // Un poco más de alto por si el logo es grande
        Bitmap bmp = Bitmap.createBitmap(ANCHO, alto, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);

        TextPaint ref        = paint(ctx, 34, false, Paint.Align.CENTER, false);
        TextPaint fecha      = paint(ctx, 26, false, Paint.Align.CENTER, false);
        TextPaint normal     = paint(ctx, 28, false, Paint.Align.LEFT, false);
        TextPaint derecho    = paint(ctx, 28, false, Paint.Align.RIGHT, false);
        TextPaint negrita    = paint(ctx, 30, true,  Paint.Align.LEFT, false);
        TextPaint negritaDer = paint(ctx, 32, true,  Paint.Align.RIGHT, false);

        int y = MARGEN + 20;
        Bitmap logo = cargarLogo(ctx);
        if (logo != null) {
            c.drawBitmap(logo, (ANCHO - logo.getWidth()) / 2f, y, null);
            y += logo.getHeight() + 30;
        } else {
            y += 60;
        }

        c.drawText(v.referencia, ANCHO / 2f, y, ref);
        y += 40;
        c.drawText(v.fecha, ANCHO / 2f, y, fecha);
        y += 50;
        c.drawLine(MARGEN, y, ANCHO - MARGEN, y, normal);
        y += 40;

        c.drawText("Cliente: " + v.cliente, MARGEN, y, normal);
        y += LINEA;
        c.drawText("Precio por kilo: " + formatoCOP(v.precioKg), MARGEN, y, normal);
        y += LINEA + 10;

        c.drawText("Detalle", MARGEN, y, negrita);
        y += LINEA;

        for (ItemVenta it : items) {
            c.drawText(String.format(Locale.US, "%.2f kg", it.pesoKg), MARGEN, y, normal);
            c.drawText(formatoCOP(it.subtotal), ANCHO - MARGEN, y, derecho);
            y += LINEA;
        }

        y += 10;
        c.drawLine(MARGEN, y, ANCHO - MARGEN, y, normal);
        y += 50;

        c.drawText("TOTAL", MARGEN, y, negrita);
        c.drawText(formatoCOP(v.total), ANCHO - MARGEN, y, negritaDer);
        y += LINEA + 10;
        c.drawText("Metodo de pago: " + v.metodoPago, MARGEN, y, normal);

        return bmp;
    }

    private static Bitmap dibujarRecordatorio(Context ctx, Venta v) {
        int alto = 400 + LINEA * 11;
        Bitmap bmp = Bitmap.createBitmap(ANCHO, alto, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);

        TextPaint subtitulo  = paint(ctx, 28, false, Paint.Align.CENTER, false);
        TextPaint normal     = paint(ctx, 28, false, Paint.Align.LEFT, false);
        TextPaint montoRojo  = paint(ctx, 48, true,  Paint.Align.CENTER, true);
        TextPaint pie        = paint(ctx, 24, false, Paint.Align.CENTER, false);

        int y = MARGEN + 20;
        Bitmap logo = cargarLogo(ctx);
        if (logo != null) {
            c.drawBitmap(logo, (ANCHO - logo.getWidth()) / 2f, y, null);
            y += logo.getHeight() + 20;
        }

        c.drawText("Recordatorio de pago", ANCHO / 2f, y, subtitulo);
        y += 60;
        c.drawLine(MARGEN, y, ANCHO - MARGEN, y, normal);
        y += 50;

        c.drawText("Cliente: " + v.cliente, MARGEN, y, normal);
        y += LINEA + 6;
        c.drawText("Referencia: " + v.referencia, MARGEN, y, normal);
        y += LINEA + 6;
        c.drawText("Fecha: " + v.fecha, MARGEN, y, normal);
        y += LINEA + 30;

        c.drawText("Saldo pendiente", ANCHO / 2f, y, subtitulo);
        y += 60;
        c.drawText(formatoCOP(v.total), ANCHO / 2f, y, montoRojo);
        y += 80;
        
        c.drawText("Agradecemos su pronto pago.", ANCHO / 2f, y, pie);

        return bmp;
    }

    public static void compartir(Context ctx, Uri uri, String referencia) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/png");
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.putExtra(Intent.EXTRA_SUBJECT, "Recibo " + referencia);
        i.putExtra(Intent.EXTRA_TEXT, "Recibo " + referencia + " - Chicken");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ctx.startActivity(Intent.createChooser(i, "Compartir"));
    }

    private static String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }
}
