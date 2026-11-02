package com.pollosk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class LoginActivity extends FragmentActivity {

    private SessionManager session;
    private EditText etPassword, etConfirmPassword, etRespuesta;
    private Spinner spinnerPregunta;
    private boolean isSettingUp = false;

    // Preguntas disponibles
    private final String[] preguntas = {
            "¿Nombre de su primera mascota?",
            "¿Ciudad donde nació?",
            "¿Nombre de su mejor amigo(a) de infancia?",
            "¿Modelo de su primer carro/moto?"
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        setTitle("Seguridad");

        session = new SessionManager(this);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etRespuesta = findViewById(R.id.etRespuesta);
        spinnerPregunta = findViewById(R.id.spinnerPregunta);
        TextView tvTitle = findViewById(R.id.tvLoginTitle);
        Button btnHuella = findViewById(R.id.btnHuella);
        TextView tvOlvidePin = findViewById(R.id.tvOlvidePin);

        if (!session.hasPassword()) {
            isSettingUp = true;
            tvTitle.setText("Cree un PIN y una pregunta de seguridad");
            etConfirmPassword.setVisibility(View.VISIBLE);
            etRespuesta.setVisibility(View.VISIBLE);
            spinnerPregunta.setVisibility(View.VISIBLE);
            btnHuella.setVisibility(View.GONE);
            tvOlvidePin.setVisibility(View.GONE);

            // Llenar el Spinner de preguntas
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, preguntas);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPregunta.setAdapter(adapter);

        } else {
            BiometricManager biometricManager = BiometricManager.from(this);
            int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
            
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                btnHuella.setVisibility(View.VISIBLE);
            }
            tvOlvidePin.setVisibility(View.VISIBLE);
        }

        ((Button) findViewById(R.id.btnEntrar)).setOnClickListener(v -> procesarPIN());
        btnHuella.setOnClickListener(v -> mostrarDialogoHuella());
        tvOlvidePin.setOnClickListener(v -> mostrarRecuperacion());
    }

    private void procesarPIN() {
        String pass = etPassword.getText().toString().trim();
        if (pass.isEmpty()) {
            Toast.makeText(this, "Ingrese su PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSettingUp) {
            String confirm = etConfirmPassword.getText().toString().trim();
            String resp = etRespuesta.getText().toString().trim();
            String preg = spinnerPregunta.getSelectedItem().toString();

            if (pass.length() < 4) {
                Toast.makeText(this, "El PIN debe tener al menos 4 números", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Los PIN no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (resp.isEmpty()) {
                Toast.makeText(this, "Ingrese una respuesta secreta", Toast.LENGTH_SHORT).show();
                return;
            }

            session.setPassword(pass);
            session.setSecurityQA(preg, resp);
            session.setLogin(true);
            Toast.makeText(this, "PIN y seguridad creados exitosamente", Toast.LENGTH_SHORT).show();
            irAlMenu();
        } else {
            if (session.checkPassword(pass)) {
                session.setLogin(true);
                irAlMenu();
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show();
                etPassword.setText("");
            }
        }
    }

    private void mostrarRecuperacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar PIN");
        builder.setMessage("Pregunta de seguridad:\n" + session.getSecurityQuestion());

        final EditText input = new EditText(this);
        input.setHint("Escriba su respuesta");
        builder.setView(input);

        builder.setPositiveButton("Verificar", (d, w) -> {
            if (session.checkAnswer(input.getText().toString())) {
                mostrarResetPIN();
            } else {
                Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarResetPIN() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo PIN");
        builder.setMessage("Cree un nuevo PIN numérico:");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (d, w) -> {
            String nuevoPin = input.getText().toString().trim();
            if (nuevoPin.length() >= 4) {
                session.setPassword(nuevoPin);
                Toast.makeText(this, "PIN actualizado. Ingrese con su nuevo PIN.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "El PIN debe tener al menos 4 números", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoHuella() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                session.setLogin(true);
                irAlMenu();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Huella no reconocida", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de Sesión")
                .setSubtitle("Usa tu huella para entrar")
                .setNegativeButtonText("Usar PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
