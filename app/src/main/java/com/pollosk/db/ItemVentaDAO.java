package com.pollosk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.pollosk.model.ItemVenta;

import java.util.ArrayList;
import java.util.List;

public class ItemVentaDAO {
    private final DBHelper helper;

    public ItemVentaDAO(Context ctx) {
        this.helper = DBHelper.get(ctx);
    }

    public long insertar(ItemVenta it) {
        ContentValues cv = new ContentValues();
        cv.put("venta_id", it.ventaId);
        cv.put("pollo_id", it.polloId);
        cv.put("peso_kg", it.pesoKg);
        cv.put("subtotal", it.subtotal);
        return helper.getWritableDatabase().insert("items_venta", null, cv);
    }

    public List<ItemVenta> listarPorVenta(long ventaId) {
        List<ItemVenta> out = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, venta_id, pollo_id, peso_kg, subtotal FROM items_venta " +
                "WHERE venta_id = ? ORDER BY id",
                new String[]{String.valueOf(ventaId)});
        while (c.moveToNext()) {
            ItemVenta it = new ItemVenta();
            it.id = c.getLong(0);
            it.ventaId = c.getLong(1);
            it.polloId = c.getLong(2);
            it.pesoKg = c.getDouble(3);
            it.subtotal = c.getDouble(4);
            out.add(it);
        }
        c.close();
        return out;
    }

    public void eliminarPorVenta(long ventaId) {
        helper.getWritableDatabase().delete("items_venta", "venta_id = ?",
                new String[]{String.valueOf(ventaId)});
    }
}
