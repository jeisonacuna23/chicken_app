package com.pollosk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextPaint;

import com.pollosk.model.ItemVenta;
import com.pollosk.model.Venta;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReciboImagen {

    private static final int ANCHO = 800;
    private static final int MARGEN = 40;
    private static final int LINEA = 42;

    public static Uri generarYGuardar(Context ctx, Venta v, List<ItemVenta> items) {
        Bitmap bmp = dibujarRecibo(ctx, v, items);
        return guardarEnGaleria(ctx, bmp, "recibo-" + v.referencia);
    }

    public static Uri generarRecordatorio(Context ctx, Venta v) {
        Bitmap bmp = dibujarRecordatorio(ctx, v);
        return guardarEnGaleria(ctx, bmp, "recordatorio-" + v.referencia);
    }

    private static TextPaint paint(Context ctx, int tamano, boolean bold, Paint.Align align) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.parseColor("#240028"));
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

    // Convierte los pixeles casi blancos en transparentes para quitar el fondo de la foto
    private static Bitmap quitarFondoBlanco(Bitmap original) {
        if (original == null) return null;
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap bitmap = original.copy(Bitmap.Config.ARGB_8888, true);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Si el pixel es muy claro (cerca a blanco), lo hacemos transparente
                if (Color.red(pixel) > 235 && Color.green(pixel) > 235 && Color.blue(pixel) > 235) {
                    bitmap.setPixel(x, y, Color.TRANSPARENT);
                }
            }
        }
        return bitmap;
    }

    private static Bitmap cargarLogo(Context ctx) {
        try {
            Bitmap logoOriginal = BitmapFactory.decodeStream(ctx.getAssets().open("logo-recibo.png"));
            if (logoOriginal == null) return null;
            
            // Le quitamos el fondo blanco
            Bitmap logoSinFondo = quitarFondoBlanco(logoOriginal);
            
            // Lo escalamos a un tamaño adecuado (250 px de ancho)
            int nuevoAncho = 250;
            int nuevoAlto = (int) (((float) logoSinFondo.getHeight() / logoSinFondo.getWidth()) * nuevoAncho);
            return Bitmap.createScaledBitmap(logoSinFondo, nuevoAncho, nuevoAlto, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap dibujarRecibo(Context ctx, Venta v, List<ItemVenta> items) {
        int alto = 200 + LINEA * (5 + items.size() + 6);
        Bitmap bmp = Bitmap.createBitmap(ANCHO, alto, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        
        // CAMBIO: Fondo blanco como un recibo real
        c.drawColor(Color.WHITE);

        TextPaint ref        = paint(ctx, 34, false, Paint.Align.CENTER);
        TextPaint fecha      = paint(ctx, 26, false, Paint.Align.CENTER);
        TextPaint normal     = paint(ctx, 28, false, Paint.Align.LEFT);
        TextPaint derecho    = paint(ctx, 28, false, Paint.Align.RIGHT);
        TextPaint negrita    = paint(ctx, 30, true,  Paint.Align.LEFT);
        TextPaint negritaDer = paint(ctx, 32, true,  Paint.Align.RIGHT);

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
        int alto = 200 + LINEA * 9;
        Bitmap bmp = Bitmap.createBitmap(ANCHO, alto, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        
        // CAMBIO: Fondo blanco también para el recordatorio
        c.drawColor(Color.WHITE);

        TextPaint subtitulo  = paint(ctx, 28, false, Paint.Align.CENTER);
        TextPaint normal     = paint(ctx, 28, false, Paint.Align.LEFT);
        TextPaint monto      = paint(ctx, 44, true,  Paint.Align.CENTER);
        TextPaint pie        = paint(ctx, 24, false, Paint.Align.CENTER);

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
        c.drawText(formatoCOP(v.total), ANCHO / 2f, y, monto);
        y += 80;

        c.drawText("Por favor acercarse a cancelar.", ANCHO / 2f, y, pie);

        return bmp;
    }

    private static Uri guardarEnGaleria(Context ctx, Bitmap bmp, String prefijo) {
        ContentValues cv = new ContentValues();
        String nombre = prefijo + "-" +
                new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".png";
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, nombre);
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PollosK");

        ContentResolver cr = ctx.getContentResolver();
        Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        if (uri == null) return null;

        try {
            OutputStream os = cr.openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            if (os != null) os.close();
            return uri;
        } catch (Exception e) {
            return null;
        }
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
