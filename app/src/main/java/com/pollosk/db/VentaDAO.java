package com.pollosk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pollosk.model.Venta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VentaDAO {
    private final DBHelper helper;

    public VentaDAO(Context ctx) {
        this.helper = DBHelper.get(ctx);
    }

    public long insertar(Venta v) {
        ContentValues cv = new ContentValues();
        cv.put("referencia", v.referencia);
        cv.put("cliente", v.cliente);
        cv.put("precio_kg", v.precioKg);
        cv.put("metodo_pago", v.metodoPago);
        cv.put("fecha", v.fecha);
        cv.put("total", v.total);
        return helper.getWritableDatabase().insert("ventas", null, cv);
    }

    public Venta buscarPorId(long id) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total " +
                "FROM ventas WHERE id = ?", new String[]{String.valueOf(id)});
        Venta v = null;
        if (c.moveToFirst()) v = leer(c);
        c.close();
        return v;
    }

    public List<Venta> listarTodas() {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas ORDER BY id DESC");
    }
    
    // NUEVO: Buscar en historial
    public List<Venta> buscarTodas(String cliente) {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas WHERE cliente LIKE '%" + cliente + "%' ORDER BY id DESC");
    }

    public List<Venta> listarDeudas() {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas WHERE metodo_pago = '" + Venta.METODO_FIADO + "' ORDER BY id DESC");
    }
    
    // NUEVO: Buscar en deudas
    public List<Venta> buscarDeudas(String cliente) {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas WHERE metodo_pago = '" + Venta.METODO_FIADO + "' AND cliente LIKE '%" + cliente + "%' ORDER BY id DESC");
    }

    public List<Venta> listarPagadas() {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas WHERE metodo_pago != '" + Venta.METODO_FIADO + "' ORDER BY id DESC");
    }
    
    // NUEVO: Buscar en pagados
    public List<Venta> buscarPagadas(String cliente) {
        return listar("SELECT id, referencia, cliente, precio_kg, metodo_pago, fecha, total FROM ventas WHERE metodo_pago != '" + Venta.METODO_FIADO + "' AND cliente LIKE '%" + cliente + "%' ORDER BY id DESC");
    }

    private List<Venta> listar(String sql) {
        List<Venta> out = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) out.add(leer(c));
        c.close();
        return out;
    }

    private Venta leer(Cursor c) {
        Venta v = new Venta();
        v.id = c.getLong(0);
        v.referencia = c.getString(1);
        v.cliente = c.getString(2);
        v.precioKg = c.getDouble(3);
        v.metodoPago = c.getString(4);
        v.fecha = c.getString(5);
        v.total = c.getDouble(6);
        return v;
    }

    public void actualizar(Venta v) {
        ContentValues cv = new ContentValues();
        cv.put("cliente", v.cliente);
        cv.put("precio_kg", v.precioKg);
        cv.put("metodo_pago", v.metodoPago);
        cv.put("fecha", v.fecha);
        cv.put("total", v.total);
        helper.getWritableDatabase().update("ventas", cv, "id = ?", new String[]{String.valueOf(v.id)});
    }

    public void eliminar(long id) {
        helper.getWritableDatabase().delete("ventas", "id = ?", new String[]{String.valueOf(id)});
    }

    public void marcarComoPagada(long id, String metodo) {
        ContentValues cv = new ContentValues();
        cv.put("metodo_pago", metodo);
        helper.getWritableDatabase().update("ventas", cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public int contarTodas() {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM ventas", null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public double sumaPorMetodo(String metodo) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT IFNULL(SUM(total),0) FROM ventas WHERE metodo_pago = ?", new String[]{metodo});
        double n = 0;
        if (c.moveToFirst()) n = c.getDouble(0);
        c.close();
        return n;
    }

    public String generarReferencia() {
        TimeZone tz = TimeZone.getTimeZone("America/Caracas");
        SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.US);
        sdf.setTimeZone(tz);
        String yy = sdf.format(new Date());
        String prefijo = "K" + yy + "-";
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT MAX(referencia) FROM ventas WHERE referencia LIKE ?", new String[]{prefijo + "%"});
        int siguiente = 1;
        if (c.moveToFirst() && !c.isNull(0)) {
            String max = c.getString(0);
            try { siguiente = Integer.parseInt(max.substring(prefijo.length())) + 1; } catch (Exception ignored) {}
        }
        c.close();
        return prefijo + String.format(Locale.US, "%04d", siguiente);
    }

    public void reiniciarContador() {
        TimeZone tz = TimeZone.getTimeZone("America/Caracas");
        SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.US);
        sdf.setTimeZone(tz);
        String yy = sdf.format(new Date());
        String prefijo = "K" + yy + "-";
        helper.getWritableDatabase().delete("ventas", "referencia LIKE ?", new String[]{prefijo + "%"});
    }
}
