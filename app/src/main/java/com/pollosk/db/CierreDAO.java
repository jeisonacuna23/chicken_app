package com.pollosk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.pollosk.model.Cierre;

import java.util.ArrayList;
import java.util.List;

public class CierreDAO {
    private final DBHelper helper;

    public CierreDAO(Context ctx) {
        this.helper = DBHelper.get(ctx);
    }

    public void insertar(Cierre c) {
        ContentValues cv = new ContentValues();
        cv.put("fecha", c.fecha);
        cv.put("cantidad_pollos", c.cantidadPollos);
        cv.put("peso_total", c.pesoTotal);
        cv.put("monto_total", c.montoTotal);
        helper.getWritableDatabase().insert("cierres", null, cv);
    }

    public List<Cierre> listarTodos() {
        List<Cierre> out = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, fecha, cantidad_pollos, peso_total, monto_total FROM cierres ORDER BY id DESC", null);
        while (c.moveToNext()) {
            Cierre cierre = new Cierre();
            cierre.id = c.getLong(0);
            cierre.fecha = c.getString(1);
            cierre.cantidadPollos = c.getInt(2);
            cierre.pesoTotal = c.getDouble(3);
            cierre.montoTotal = c.getDouble(4);
            out.add(cierre);
        }
        c.close();
        return out;
    }
}
