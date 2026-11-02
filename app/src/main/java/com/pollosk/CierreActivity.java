package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.CierreDAO;
import com.pollosk.db.DBHelper;
import com.pollosk.model.Cierre;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CierreActivity extends Activity {

    private int cantidadPollos;
    private double pesoTotal;
    private double montoTotal;
    private String fechaCierre;
    private String textoResumen;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_cierre);
        setTitle("Cierre");

        calcularResumen();

        ((TextView) findViewById(R.id.tvCierreFecha)).setText(fechaCierre);
        ((TextView) findViewById(R.id.tvCierrePollos)).setText("Pollos vendidos: " + cantidadPollos);
        ((TextView) findViewById(R.id.tvCierrePeso)).setText(String.format(Locale.US, "Peso total: %.3f kg", pesoTotal));
        ((TextView) findViewById(R.id.tvCierreTotal)).setText("Monto total: " + formatoCOP(montoTotal));

        ((Button) findViewById(R.id.btnVerRecibos)).setOnClickListener(v ->
                startActivity(new Intent(this, HistorialActivity.class)));

        ((Button) findViewById(R.id.btnCerrarBorrar)).setOnClickListener(v -> confirmarCierre());

        ((Button) findViewById(R.id.btnCancelarCierre)).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    private void calcularResumen() {
        DBHelper helper = DBHelper.get(this);
        fechaCierre = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(new Date());

        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*), IFNULL(SUM(peso_kg),0) FROM pollos WHERE vendido = 1", null);
        if (c.moveToFirst()) {
            cantidadPollos = c.getInt(0);
            pesoTotal = c.getDouble(1);
        }
        c.close();

        Cursor c2 = helper.getReadableDatabase().rawQuery(
                "SELECT IFNULL(SUM(total),0) FROM ventas", null);
        if (c2.moveToFirst()) montoTotal = c2.getDouble(0);
        c2.close();

        textoResumen = "CIERRE DE JORNADA - CHICKEN\n" +
                "Fecha: " + fechaCierre + "\n" +
                "Pollos vendidos: " + cantidadPollos + "\n" +
                "Peso total: " + String.format(Locale.US, "%.3f", pesoTotal) + " kg\n" +
                "Monto total: " + formatoCOP(montoTotal) + "\n";
    }

    private void confirmarCierre() {
        new AlertDialog.Builder(this)
            .setTitle("Cerrar y borrar")
            .setMessage("Se guardará el resumen en el historial de cierres, se descargará el TXT y se borrarán TODOS los datos. ¿Continuar?")
            .setPositiveButton("Sí, cerrar", (d, w) -> cerrarYborrar())
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void cerrarYborrar() {
        // 1) Guardar TXT
        boolean txtOk = guardarTxt();

        // 2) Guardar en la nueva tabla de historial de cierres
        CierreDAO cierreDAO = new CierreDAO(this);
        Cierre cierre = new Cierre();
        cierre.fecha = fechaCierre;
        cierre.cantidadPollos = cantidadPollos;
        cierre.pesoTotal = pesoTotal;
        cierre.montoTotal = montoTotal;
        cierreDAO.insertar(cierre);

        // 3) Borrar todo
        DBHelper db = DBHelper.get(this);
        db.getWritableDatabase().delete("pollos", null, null);
        db.getWritableDatabase().delete("items_venta", null, null);
        db.getWritableDatabase().delete("ventas", null, null);

        String mensaje = txtOk ? "Jornada cerrada. Resumen guardado." : "Jornada cerrada. No se pudo guardar el TXT.";
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private boolean guardarTxt() {
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            String nombre = "cierre-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date()) + ".txt";
            File archivo = new File(dir, nombre);
            FileOutputStream fos = new FileOutputStream(archivo);
            fos.write(textoResumen.getBytes("UTF-8"));
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }
}
