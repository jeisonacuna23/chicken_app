package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.DBHelper;

public class MainActivity extends Activity {

    private static final long VENTANA_TOQUES_MS = 1500;
    private long primerToque = 0;
    private int toques = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.btnInventario)).setOnClickListener(v ->
                startActivity(new Intent(this, InventarioActivity.class)));
        ((Button) findViewById(R.id.btnVender)).setOnClickListener(v ->
                startActivity(new Intent(this, VenderActivity.class)));
        ((Button) findViewById(R.id.btnHistorial)).setOnClickListener(v ->
                startActivity(new Intent(this, HistorialActivity.class)));
        ((Button) findViewById(R.id.btnDeudas)).setOnClickListener(v ->
                startActivity(new Intent(this, DeudasActivity.class)));
        ((Button) findViewById(R.id.btnPagados)).setOnClickListener(v ->
                startActivity(new Intent(this, PagadosActivity.class)));
        ((Button) findViewById(R.id.btnResumen)).setOnClickListener(v ->
                startActivity(new Intent(this, ResumenActivity.class)));

        // Modo test oculto: 3 toques rápidos sobre el título
        TextView titulo = findViewById(R.id.tvTitulo);
        if (titulo != null) titulo.setOnClickListener(v -> registrarToque());
    }

    private void registrarToque() {
        long ahora = SystemClock.elapsedRealtime();
        if (toques == 0 || ahora - primerToque > VENTANA_TOQUES_MS) {
            toques = 1;
            primerToque = ahora;
            return;
        }
        toques++;
        if (toques >= 3) {
            toques = 0;
            confirmarBorrado();
        }
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
            .setTitle("Modo test")
            .setMessage("Se borrarán TODOS los datos: inventario, ventas, deudas y pagos. " +
                    "Esta acción no se puede deshacer. ¿Continuar?")
            .setPositiveButton("Sí, borrar todo", (d, w) -> {
                DBHelper db = DBHelper.get(this);
                db.getWritableDatabase().delete("pollos", null, null);
                db.getWritableDatabase().delete("items_venta", null, null);
                db.getWritableDatabase().delete("ventas", null, null);
                Toast.makeText(this, "Datos borrados", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
