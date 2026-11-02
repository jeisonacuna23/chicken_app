package com.pollosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pollosk.db.PolloDAO;
import com.pollosk.model.Pollo;

import java.util.List;
import java.util.Locale;

public class InventarioActivity extends Activity {

    private PolloDAO dao;
    private EditText etPeso, etCantidad;
    private ListView lvDisponibles, lvVendidos;
    private TextView tvDisponibles, tvVendidos;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_inventario);
        setTitle("Inventario");

        dao = new PolloDAO(this);
        etPeso = findViewById(R.id.etPeso);
        etCantidad = findViewById(R.id.etCantidad);
        lvDisponibles = findViewById(R.id.lvDisponibles);
        lvVendidos = findViewById(R.id.lvVendidos);
        tvDisponibles = findViewById(R.id.tvDisponibles);
        tvVendidos = findViewById(R.id.tvVendidos);

        ((Button) findViewById(R.id.btnAgregar)).setOnClickListener(v -> agregar());

        // Mantener pulsado para eliminar, clic normal para editar
        lvDisponibles.setOnItemClickListener((parent, view, pos, id) -> {
            Pollo p = (Pollo) parent.getItemAtPosition(pos);
            editarPeso(p);
        });
        lvDisponibles.setOnItemLongClickListener((parent, view, pos, id) -> {
            Pollo p = (Pollo) parent.getItemAtPosition(pos);
            confirmarEliminar(p);
            return true;
        });

        refrescar();
    }

    private void agregar() {
        String txtPeso = etPeso.getText().toString().trim().replace(',', '.');
        String txtCant = etCantidad.getText().toString().trim();
        
        if (txtPeso.isEmpty()) { toast("Ingresa el peso en kilos"); return; }
        if (txtCant.isEmpty()) { txtCant = "1"; }

        double peso;
        int cantidad;
        try {
            peso = Double.parseDouble(txtPeso);
            cantidad = Integer.parseInt(txtCant);
        } catch (NumberFormatException e) {
            toast("Peso o cantidad inválidos");
            return;
        }

        if (peso <= 0 || cantidad <= 0) {
            toast("El peso y la cantidad deben ser mayores a 0");
            return;
        }

        for (int i = 0; i < cantidad; i++) {
            dao.insertar(peso);
        }
        
        etPeso.setText("");
        etCantidad.setText("1");
        toast(cantidad + " pollo(s) agregado(s) de " + String.format(Locale.US, "%.3f", peso) + " kg");
        refrescar();
    }

    private void editarPeso(final Pollo p) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.format(Locale.US, "%.3f", p.pesoKg));

        new AlertDialog.Builder(this)
            .setTitle("Editar Peso")
            .setMessage("Modifica el peso de este pollo:")
            .setView(input)
            .setPositiveButton("Guardar", (d, w) -> {
                String val = input.getText().toString().trim().replace(',', '.');
                try {
                    double nuevoPeso = Double.parseDouble(val);
                    if (nuevoPeso > 0) {
                        dao.actualizarPeso(p.id, nuevoPeso);
                        refrescar();
                        toast("Peso actualizado");
                    } else {
                        toast("El peso debe ser mayor a 0");
                    }
                } catch (Exception e) {
                    toast("Peso inválido");
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void confirmarEliminar(final Pollo p) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar pollo")
            .setMessage(String.format(Locale.US, "Se eliminará el pollo de %.3f kg. ¿Continuar?", p.pesoKg))
            .setPositiveButton("Sí, eliminar", (d, w) -> {
                dao.eliminar(p.id);
                toast("Pollo eliminado");
                refrescar();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void refrescar() {
        List<Pollo> disp = dao.listarDisponibles();
        List<Pollo> vend = dao.listarVendidos();

        tvDisponibles.setText("Disponibles (" + disp.size() + ")");
        tvVendidos.setText("Vendidos (" + vend.size() + ")");

        lvDisponibles.setAdapter(new Adaptador(disp, false));
        lvVendidos.setAdapter(new Adaptador(vend, true));
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    private class Adaptador extends ArrayAdapter<Pollo> {
        private final boolean esVendido;

        Adaptador(List<Pollo> lista, boolean esVendido) {
            super(InventarioActivity.this, R.layout.item_pollo, lista);
            this.esVendido = esVendido;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_pollo, parent, false);
            }
            Pollo p = getItem(pos);
            TextView tvPeso = convertView.findViewById(R.id.tvPesoItem);
            TextView tvEstado = convertView.findViewById(R.id.tvEstadoItem);

            tvPeso.setText(String.format(Locale.US, "%.3f kg", p.pesoKg));
            tvEstado.setText(esVendido ? "Vendido" : "Disponible");
            return convertView;
        }
    }
}
