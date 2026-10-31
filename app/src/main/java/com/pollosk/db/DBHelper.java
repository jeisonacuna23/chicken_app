package com.pollosk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pollosk.db";
    private static final int DB_VERSION = 1;
    private static DBHelper instance;

    public static synchronized DBHelper get(Context ctx) {
        if (instance == null) instance = new DBHelper(ctx.getApplicationContext());
        return instance;
    }

    private DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pollos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "peso_kg REAL NOT NULL," +
                "vendido INTEGER NOT NULL DEFAULT 0," +
                "venta_id INTEGER)");

        db.execSQL("CREATE TABLE ventas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "referencia TEXT NOT NULL UNIQUE," +
                "cliente TEXT NOT NULL," +
                "precio_kg REAL NOT NULL," +
                "metodo_pago TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "total REAL NOT NULL)");

        db.execSQL("CREATE TABLE items_venta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "venta_id INTEGER NOT NULL," +
                "pollo_id INTEGER NOT NULL," +
                "peso_kg REAL NOT NULL," +
                "subtotal REAL NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v1, sin migraciones todavía
    }
}
