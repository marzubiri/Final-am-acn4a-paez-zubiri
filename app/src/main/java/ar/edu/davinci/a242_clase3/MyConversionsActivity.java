package ar.edu.davinci.a242_clase3;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyConversionsActivity extends AppCompatActivity {

    private RecyclerView conversionsRecyclerView;
    private ConversionsAdapter adapter;
    private ArrayList<String> conversionsList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private boolean isGuest;
    private String guestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_conversions);

        // Habilitar el botón de navegación (flecha de regreso)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Verificar si el usuario es invitado
        isGuest = getIntent().getBooleanExtra("isGuest", false);
        if (isGuest) {
            guestId = getSharedPreferences("AppPreferences", MODE_PRIVATE).getString("guestId", null);
        }

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
        if (isGuest) {
            loadGuestConversions();
        } else {
            loadUserConversionsFromFirestore();
        }
    }

    private void loadGuestConversions() {
        if (guestId == null) {
            Toast.makeText(this, "No se encontraron conversiones para el invitado.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference guestConversionsRef = firestore.collection("guests").document(guestId).collection("conversions");
        guestConversionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                conversionsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String conversion = document.getString("fromUnit") + " -> " + document.getString("toUnit") +
                            ": " + document.getDouble("inputValue") + " = " + document.getDouble("resultValue");
                    conversionsList.add(conversion);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error al cargar conversiones para el invitado.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserConversionsFromFirestore() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference userConversionsRef = firestore.collection("users").document(auth.getCurrentUser().getUid()).collection("conversions");
        userConversionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                conversionsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String conversion = document.getString("fromUnit") + " -> " + document.getString("toUnit") +
                            ": " + document.getDouble("inputValue") + " = " + document.getDouble("resultValue");
                    conversionsList.add(conversion);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error al cargar conversiones del usuario.", Toast.LENGTH_SHORT).show();
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
