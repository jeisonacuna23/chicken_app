package com.pollosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private SessionManager session;
    private EditText etPassword, etConfirmPassword;
    private boolean isSettingUp = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        setTitle("Seguridad");

        session = new SessionManager(this);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        TextView tvTitle = findViewById(R.id.tvLoginTitle);

        // Si es la primera vez, pedimos crear contraseña
        if (!session.hasPassword()) {
            isSettingUp = true;
            tvTitle.setText("Cree una contraseña para proteger sus datos");
            etConfirmPassword.setVisibility(android.view.View.VISIBLE);
        }

        ((Button) findViewById(R.id.btnEntrar)).setOnClickListener(v -> procesar());
    }

    private void procesar() {
        String pass = etPassword.getText().toString().trim();
        if (pass.isEmpty()) {
            Toast.makeText(this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSettingUp) {
            String confirm = etConfirmPassword.getText().toString().trim();
            if (pass.length() < 4) {
                Toast.makeText(this, "Debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            session.setPassword(pass);
            session.setLogin(true);
            Toast.makeText(this, "Contraseña creada", Toast.LENGTH_SHORT).show();
            irAlMenu();
        } else {
            if (session.checkPassword(pass)) {
                session.setLogin(true);
                irAlMenu();
            } else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                etPassword.setText("");
            }
        }
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
