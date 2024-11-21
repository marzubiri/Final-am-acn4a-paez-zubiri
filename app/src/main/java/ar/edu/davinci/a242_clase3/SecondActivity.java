package ar.edu.davinci.a242_clase3;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class SecondActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // inicializo components UI
        TextView textViewName = findViewById(R.id.textViewName);
        TextView textViewEmail = findViewById(R.id.textViewEmail);
        Button buttonEditProfile = findViewById(R.id.buttonEditProfile);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Asignar eventos
        buttonEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Editar perfil presionado", Toast.LENGTH_SHORT).show()
        );

        buttonLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            // Navegar a la pantalla principal o cerrar sesión
            Intent intent = new Intent(SecondActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}