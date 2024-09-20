package ar.edu.davinci.a242_clase3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar los componentes de la UI
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        inputValue = findViewById(R.id.inputValue);
        resultText = findViewById(R.id.resultText);
        convertButton = findViewById(R.id.convertButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Configurar el adaptador de los Spinners con el array de unidades
        String[] unitsArray = getResources().getStringArray(R.array.units_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitsArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Manejo del evento de clic en el botón de conversión
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromUnit = spinnerFrom.getSelectedItem().toString();
                String toUnit = spinnerTo.getSelectedItem().toString();
                String inputValueStr = inputValue.getText().toString();

                // Verificar que el valor de entrada no esté vacío
                if (!inputValueStr.isEmpty()) {
                    double value = Double.parseDouble(inputValueStr);
                    double result = convertUnits(value, fromUnit, toUnit);
                    resultText.setText("Resultado: " + result + " " + toUnit);
                } else {
                    resultText.setText("Por favor, ingrese un valor.");
                }
            }
        });
        // Inicializar SharedPreferences para guardar la preferencia del Dark Mode
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Verificar el estado del Dark Mode al iniciar
        boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        if (isDarkModeOn) {
            // Si el modo oscuro está activado, aplicarlo
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeSwitch.setChecked(true); // Mantener el switch activado
        } else {
            // Si el modo claro está activado
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Listener para el Switch que alterna el modo oscuro
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Activar el modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkModeOn", true);
            } else {
                // Desactivar el modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkModeOn", false);
            }
            editor.apply(); // Guardar la preferencia del usuario
        });
    }

    // Lógica de conversión en Java
    private double convertUnits(double value, String fromUnit, String toUnit) {
        // Mapa de factores de conversión
        java.util.Map<String, Double> conversionMap = new java.util.HashMap<>();
        conversionMap.put("Metros", 1.0);
        conversionMap.put("Kilómetros", 1000.0);
        conversionMap.put("Centímetros", 0.01);
        conversionMap.put("Pulgadas", 0.0254);
        conversionMap.put("Pies", 0.3048);

        double fromFactor = conversionMap.get(fromUnit) != null ? conversionMap.get(fromUnit) : 1.0;
        double toFactor = conversionMap.get(toUnit) != null ? conversionMap.get(toUnit) : 1.0;

        return value * (fromFactor / toFactor);
    }
}
