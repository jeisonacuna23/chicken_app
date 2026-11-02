package com.pollosk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pollosk.model.Pollo;

import java.util.ArrayList;
import java.util.List;

public class PolloDAO {
    private final DBHelper helper;

    public PolloDAO(Context ctx) {
        this.helper = DBHelper.get(ctx);
    }

    public long insertar(double pesoKg) {
        ContentValues cv = new ContentValues();
        cv.put("peso_kg", pesoKg);
        cv.put("vendido", 0);
        cv.putNull("venta_id");
        return helper.getWritableDatabase().insert("pollos", null, cv);
    }

    public Pollo buscarPorId(long id) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, peso_kg, vendido, venta_id FROM pollos WHERE id = ?",
                new String[]{String.valueOf(id)});
        Pollo p = null;
        if (c.moveToFirst()) p = leer(c);
        c.close();
        return p;
    }

    public List<Pollo> listarDisponibles() {
        return listar("SELECT id, peso_kg, vendido, venta_id FROM pollos " +
                "WHERE vendido = 0 ORDER BY peso_kg DESC");
    }

    public List<Pollo> listarVendidos() {
        return listar("SELECT id, peso_kg, vendido, venta_id FROM pollos " +
                "WHERE vendido = 1 ORDER BY id DESC");
    }

    public List<Pollo> listarPorVenta(long ventaId) {
        return listar("SELECT id, peso_kg, vendido, venta_id FROM pollos " +
                "WHERE venta_id = " + ventaId + " ORDER BY id");
    }

    private List<Pollo> listar(String sql) {
        List<Pollo> out = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) out.add(leer(c));
        c.close();
        return out;
    }

    private Pollo leer(Cursor c) {
        Pollo p = new Pollo();
        p.id = c.getLong(0);
        p.pesoKg = c.getDouble(1);
        p.vendido = c.getInt(2) == 1;
        p.ventaId = c.isNull(3) ? null : c.getLong(3);
        return p;
    }

    // NUEVO MÉTODO: Para editar el peso
    public void actualizarPeso(long id, double nuevoPeso) {
        ContentValues cv = new ContentValues();
        cv.put("peso_kg", nuevoPeso);
        helper.getWritableDatabase().update("pollos", cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public void marcarVendidos(List<Long> ids, long ventaId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (long id : ids) {
                ContentValues cv = new ContentValues();
                cv.put("vendido", 1);
                cv.put("venta_id", ventaId);
                db.update("pollos", cv, "id = ?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void devolverAlInventario(long ventaId) {
        ContentValues cv = new ContentValues();
        cv.put("vendido", 0);
        cv.putNull("venta_id");
        helper.getWritableDatabase().update(
                "pollos", cv, "venta_id = ?", new String[]{String.valueOf(ventaId)});
    }

    public int contarDisponibles() {
        return contar("vendido = 0");
    }

    public int contarVendidos() {
        return contar("vendido = 1");
    }

    private int contar(String where) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM pollos WHERE " + where, null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public void eliminar(long id) {
        helper.getWritableDatabase().delete("pollos", "id = ?", new String[]{String.valueOf(id)});
    }
}
