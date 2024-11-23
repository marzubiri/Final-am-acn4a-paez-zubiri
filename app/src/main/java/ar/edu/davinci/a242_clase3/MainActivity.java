package ar.edu.davinci.a242_clase3;

import android.content.Context;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
import androidx.core.view.GravityCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    // Variables para los Spinners, EditText y TextView
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText inputValue;
    private TextView resultText;
    private Button convertButton;
    private androidx.appcompat.widget.SwitchCompat darkModeSwitch;

    // Variables para SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Array de imágenes para los Spinners
    private int[] images = {R.drawable.meter, R.drawable.kilometer, R.drawable.centimeter, R.drawable.inches, R.drawable.feet};

    // Variables para el DrawerLayout y el botón de hamburguesa
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicializar Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("conversions");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configure the ActionBarDrawerToggle for the hamburger button
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Show the hamburger button on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_new_page) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // Inicializar DrawerLayout y el NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar el ActionBarDrawerToggle para el botón de hamburguesa
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Mostrar el botón de hamburguesa en la barra de acción
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar los componentes de la UI
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        inputValue = findViewById(R.id.inputValue);
        resultText = findViewById(R.id.resultText);
        convertButton = findViewById(R.id.convertButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Configurar el adaptador de los Spinners con el array de unidades e imágenes
        String[] unitsArray = getResources().getStringArray(R.array.units_array);
        CustomAdapter customAdapter = new CustomAdapter(this, unitsArray, images);
        spinnerFrom.setAdapter(customAdapter);
        spinnerTo.setAdapter(customAdapter);

        // Inicializar SharedPreferences para guardar la preferencia del Dark Mode
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Verificar el estado del Dark Mode al iniciar
        boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeSwitch.setChecked(true); // Mantener el switch activado
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Listener para el Switch que alterna el modo oscuro
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkModeOn", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkModeOn", false);
            }
            editor.apply(); // Guardar la preferencia del usuario
        });

        // Manejo del evento de clic en el botón de conversión
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromUnit = spinnerFrom.getSelectedItem().toString();
                String toUnit = spinnerTo.getSelectedItem().toString();
                String inputValueStr = inputValue.getText().toString();

                if (!inputValueStr.isEmpty()) {
                    double value = Double.parseDouble(inputValueStr);
                    double result = convertUnits(value, fromUnit, toUnit);
                    resultText.setText("Resultado: " + result + " " + toUnit);
                    // Guardar la conversión en Firebase
                    String conversion = value + " " + fromUnit + " = " + result + " " + toUnit;
                    ref.push().setValue(conversion);
                } else {
                    resultText.setText("Por favor, ingrese un valor.");
                }
            }
        });
    }

    // Método para manejar clics en el botón de hamburguesa
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // CustomAdapter para agregar imágenes al Spinner
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

    // Lógica de conversión en Java
    private double convertUnits(double value, String fromUnit, String toUnit) {
        java.util.Map<String, Double> conversionMap = new java.util.HashMap<>();
        conversionMap.put("Metros", 1.0);
        conversionMap.put("Kilómetros", 1000.0);
        conversionMap.put("Centímetros", 0.01);
        conversionMap.put("Pulgadas", 0.0254);
        conversionMap.put("Pies", 0.3048);

        double fromFactor = conversionMap.get(fromUnit);
        double toFactor = conversionMap.get(toUnit);

        return value * (fromFactor / toFactor);
    }


}

