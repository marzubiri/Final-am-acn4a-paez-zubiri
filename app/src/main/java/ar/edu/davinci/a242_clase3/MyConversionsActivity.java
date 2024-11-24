package ar.edu.davinci.a242_clase3;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyConversionsActivity extends AppCompatActivity {

    private RecyclerView conversionsRecyclerView;
    private ConversionsAdapter adapter;
    private ArrayList<String> conversionsList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_conversions);

        // Habilitar el botón de navegación (flecha de regreso)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Verificar si el usuario está autenticado
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus conversiones", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar DatabaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("conversions");

        // Inicializar RecyclerView y lista
        conversionsRecyclerView = findViewById(R.id.conversionsRecyclerView);
        conversionsList = new ArrayList<>();
        adapter = new ConversionsAdapter(conversionsList);

        conversionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversionsRecyclerView.setAdapter(adapter);

        // Cargar las conversiones del usuario actual
        loadUserConversions();
    }

    private void loadUserConversions() {
        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversionsList.clear();
                for (DataSnapshot conversionSnapshot : snapshot.getChildren()) {
                    String conversion = conversionSnapshot.getValue(String.class);
                    conversionsList.add(conversion);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyConversionsActivity.this, "Error al cargar conversiones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Manejar clic en la flecha de navegación
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Regresa a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
