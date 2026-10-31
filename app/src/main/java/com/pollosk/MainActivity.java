package com.pollosk;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.ImageView;
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
        
        configurarActionBar();

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
    }

    private void configurarActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // Quita el texto "Chicken" o "Pollos K"
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar_custom);
            
            ImageView logoView = actionBar.getCustomView().findViewById(R.id.ivActionBarLogo);
            try {
                Drawable d = Drawable.createFromStream(getAssets().open("logo-recibo.png"), null);
                logoView.setImageDrawable(d);
            } catch (Exception e) {
                // Si falla, no pasa nada
            }
            
            // El truco de los 3 toques ahora va en el logo
            logoView.setOnClickListener(v -> registrarToque());
        }
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
            .setMessage("Se borrarán TODOS los datos: inventario, ventas, deudas y pagos. ¿Continuar?")
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
