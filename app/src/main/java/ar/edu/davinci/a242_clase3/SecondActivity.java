package ar.edu.davinci.a242_clase3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecondActivity extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private TextView userEmailTextView; // Campo para mostrar el correo del usuario
    private Button changePasswordButton, logoutButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Habilitar la flecha de navegación
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Perfil");
        }

        auth = FirebaseAuth.getInstance();

        // Inicializar vistas
        userEmailTextView = findViewById(R.id.userEmailTextView);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Obtener el correo del usuario actual y mostrarlo
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                userEmailTextView.setText("Correo: " + email);
            } else {
                userEmailTextView.setText("Correo no disponible");
            }
        } else {
            userEmailTextView.setText("No autenticado");
        }

        // Manejar cambio de contraseña
        changePasswordButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

            if (newPassword.isEmpty() || confirmNewPassword.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user != null) {
                user.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            }
        });

        // Manejar cierre de sesión
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            // Redirigir a la pantalla de inicio de sesión
            Intent intent = new Intent(SecondActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Manejar el clic en la flecha de navegación
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Regresa a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
