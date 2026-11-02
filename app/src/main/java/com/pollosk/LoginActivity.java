package com.pollosk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class LoginActivity extends FragmentActivity {

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
        Button btnHuella = findViewById(R.id.btnHuella);

        if (!session.hasPassword()) {
            isSettingUp = true;
            tvTitle.setText("Cree un PIN para proteger sus datos");
            etConfirmPassword.setVisibility(View.VISIBLE);
            btnHuella.setVisibility(View.GONE);
        } else {
            BiometricManager biometricManager = BiometricManager.from(this);
            int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
            
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                btnHuella.setVisibility(View.VISIBLE);
            }
        }

        ((Button) findViewById(R.id.btnEntrar)).setOnClickListener(v -> procesarPIN());
        btnHuella.setOnClickListener(v -> mostrarDialogoHuella());
    }

    private void procesarPIN() {
        String pass = etPassword.getText().toString().trim();
        if (pass.isEmpty()) {
            Toast.makeText(this, "Ingrese su PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSettingUp) {
            String confirm = etConfirmPassword.getText().toString().trim();
            if (pass.length() < 4) {
                Toast.makeText(this, "El PIN debe tener al menos 4 números", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Los PIN no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            session.setPassword(pass);
            session.setLogin(true);
            Toast.makeText(this, "PIN creado exitosamente", Toast.LENGTH_SHORT).show();
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

    private void mostrarDialogoHuella() {
        Executor executor = ContextCompat.getMainExecutor(this);
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                session.setLogin(true);
                Toast.makeText(getApplicationContext(), "Huella reconocida", Toast.LENGTH_SHORT).show();
                irAlMenu();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Huella no reconocida, intente de nuevo", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de Sesión")
                .setSubtitle("Usa tu huella dactilar para entrar")
                .setNegativeButtonText("Usar PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
