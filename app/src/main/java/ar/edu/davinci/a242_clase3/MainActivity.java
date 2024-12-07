package ar.edu.davinci.a242_clase3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText inputValue;
    private TextView resultText;
    private Button convertButton;
    private androidx.appcompat.widget.SwitchCompat darkModeSwitch;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private boolean isGuest; // Bandera para saber si es un usuario invitado
    private String guestId; // ID único para invitados
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();

        // Obtener la bandera de si es invitado
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        // Si es invitado, generar un ID único
        if (isGuest) {
            SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            guestId = preferences.getString("guestId", null);
            if (guestId == null) {
                guestId = UUID.randomUUID().toString();
                preferences.edit().putString("guestId", guestId).apply();
            }
        }

        // Inicializar componentes del menú lateral
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Actualizar el menú dinámicamente
        updateMenu();

        // Manejar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (isGuest) {
                if (id == R.id.nav_login) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else if (id == R.id.nav_register) {
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                    finish();
                }
            } else {
                if (id == R.id.nav_my_conversions) {
                    startActivity(new Intent(MainActivity.this, MyConversionsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, SecondActivity.class));
                } else if (id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Inicializar componentes de la UI
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        inputValue = findViewById(R.id.inputValue);
        resultText = findViewById(R.id.resultText);
        convertButton = findViewById(R.id.convertButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Configurar el adaptador de los Spinners
        String[] unitsArray = getResources().getStringArray(R.array.units_array);
        int[] images = {R.drawable.meter, R.drawable.kilometer, R.drawable.centimeter, R.drawable.inches, R.drawable.feet};
        CustomAdapter customAdapter = new CustomAdapter(this, unitsArray, images);
        spinnerFrom.setAdapter(customAdapter);
        spinnerTo.setAdapter(customAdapter);

        // Configurar modo oscuro
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        AppCompatDelegate.setDefaultNightMode(isDarkModeOn ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        darkModeSwitch.setChecked(isDarkModeOn);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("isDarkModeOn", isChecked).apply();
        });

        // Manejar clic en el botón de conversión
        convertButton.setOnClickListener(v -> {
            String fromUnit = spinnerFrom.getSelectedItem().toString();
            String toUnit = spinnerTo.getSelectedItem().toString();
            String inputValueStr = inputValue.getText().toString();

            if (!inputValueStr.isEmpty()) {
                double value = Double.parseDouble(inputValueStr);
                double result = convertUnits(value, fromUnit, toUnit);
                resultText.setText("Resultado: " + result + " " + toUnit);

                if (isGuest) {
                    saveConversionAsGuest(fromUnit, toUnit, value, result);
                } else {
                    saveConversionAsUser(fromUnit, toUnit, value, result);
                }
            } else {
                resultText.setText("Por favor, ingrese un valor.");
            }
        });
    }

    private void saveConversionAsGuest(String fromUnit, String toUnit, double inputValue, double resultValue) {
        Map<String, Object> conversionData = new HashMap<>();
        conversionData.put("fromUnit", fromUnit);
        conversionData.put("toUnit", toUnit);
        conversionData.put("inputValue", inputValue);
        conversionData.put("resultValue", resultValue);

        firestore.collection("guests").document(guestId)
                .collection("conversions")
                .add(conversionData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Conversión guardada como invitado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveConversionAsUser(String fromUnit, String toUnit, double inputValue, double resultValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> conversionData = new HashMap<>();
        conversionData.put("fromUnit", fromUnit);
        conversionData.put("toUnit", toUnit);
        conversionData.put("inputValue", inputValue);
        conversionData.put("resultValue", resultValue);

        firestore.collection("users").document(user.getUid())
                .collection("conversions")
                .add(conversionData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Conversión guardada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateMenu() {
        if (isGuest) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.guest_menu);
        } else {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer_menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class CustomAdapter extends ArrayAdapter<String> {
        Context context;
        String[] unitsArray;
        int[] images;

        public CustomAdapter(Context context, String[] unitsArray, int[] images) {
            super(context, R.layout.spinner_item, unitsArray);
            this.context = context;
            this.unitsArray = unitsArray;
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
            }
            ImageView spinnerImage = convertView.findViewById(R.id.spinnerImage);
            TextView spinnerText = convertView.findViewById(R.id.spinnerText);
            spinnerImage.setImageResource(images[position]);
            spinnerText.setText(unitsArray[position]);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    private double convertUnits(double value, String fromUnit, String toUnit) {
        java.util.Map<String, Double> conversionMap = new java.util.HashMap<>();
        conversionMap.put("Metros", 1.0);
        conversionMap.put("Kilómetros", 1000.0);
        conversionMap.put("Centímetros", 0.01);
        conversionMap.put("Pulgadas", 0.0254);
        conversionMap.put("Pies", 0.3048);

        return value * (conversionMap.get(fromUnit) / conversionMap.get(toUnit));
    }
}
