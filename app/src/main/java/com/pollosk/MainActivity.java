package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
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
        
        ImageView logo = findViewById(R.id.ivMainLogo);
        logo.setOnClickListener(v -> registrarToque());

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

    // Crea el botón de salir en la barra de arriba
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Cerrar Sesión")
            .setIcon(R.drawable.ic_logout)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    // Escucha el clic en el botón de salir
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Desea salir de la aplicación?")
                .setPositiveButton("Sí", (d, w) -> {
                    SessionManager session = new SessionManager(this);
                    session.logout();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            .setMessage("Se borrarán TODOS los datos. ¿Continuar?")
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
