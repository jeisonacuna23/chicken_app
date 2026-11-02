package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.PolloDAO;
import com.pollosk.db.VentaDAO;
import com.pollosk.model.Venta;

import java.util.Locale;

public class ResumenActivity extends Activity {

    private VentaDAO ventaDAO;
    private PolloDAO polloDAO;
    private TextView tvEfectivo, tvNequi, tvFiado, tvRecaudado, tvCantidad;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_resumen);
        setTitle("Resumen");

        ventaDAO = new VentaDAO(this);
        polloDAO = new PolloDAO(this);

        tvEfectivo = findViewById(R.id.tvResumenEfectivo);
        tvNequi = findViewById(R.id.tvResumenNequi);
        tvFiado = findViewById(R.id.tvResumenFiado);
        tvRecaudado = findViewById(R.id.tvResumenRecaudado);
        tvCantidad = findViewById(R.id.tvResumenCantidad);

        ((Button) findViewById(R.id.btnFinalizar)).setOnClickListener(v -> intentarFinalizar());
        ((Button) findViewById(R.id.btnVerCierres)).setOnClickListener(v -> 
                startActivity(new Intent(this, HistorialCierresActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar();
    }

    private void refrescar() {
        double efectivo = ventaDAO.sumaPorMetodo(Venta.METODO_EFECTIVO);
        double nequi = ventaDAO.sumaPorMetodo(Venta.METODO_NEQUI);
        double fiado = ventaDAO.sumaPorMetodo(Venta.METODO_FIADO);
        int cantidad = ventaDAO.contarTodas();

        tvEfectivo.setText("Efectivo: " + formatoCOP(efectivo));
        tvNequi.setText("Nequi: " + formatoCOP(nequi));
        tvFiado.setText("Fiado (pendiente): " + formatoCOP(fiado));
        tvRecaudado.setText("Recaudado: " + formatoCOP(efectivo + nequi));
        tvCantidad.setText("Ventas registradas: " + cantidad);
    }

    private void intentarFinalizar() {
        int disponibles = polloDAO.contarDisponibles();
        int deudas = ventaDAO.listarDeudas().size();
        int ventas = ventaDAO.contarTodas();

        if (disponibles > 0) { toast("Aún quedan " + disponibles + " pollo(s) sin vender."); return; }
        if (deudas > 0) { toast("Hay " + deudas + " deuda(s) pendiente(s)."); return; }
        if (ventas == 0) { toast("No hay ventas registradas."); return; }

        new AlertDialog.Builder(this)
            .setTitle("Finalizar jornada")
            .setMessage("Se generará el resumen final y se reiniciarán los recibos. ¿Continuar?")
            .setPositiveButton("Sí, finalizar", (d, w) -> {
                startActivity(new Intent(this, CierreActivity.class));
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private String formatoCOP(double n) {
        return String.format(Locale.US, "$%,.0f", n).replace(',', '.');
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}
