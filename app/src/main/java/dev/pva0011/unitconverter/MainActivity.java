package dev.pva0011.unitconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_FILE_NAME = "userPreferences";
    private static final String SAVED_CONVERSIONS_OBJ_KEY = "savedConversions";

    private EditText userInput;
    private RadioGroup conversionRadioGroup;
    private LinearLayout resultLayout;
    private LinearLayout savedLayout;
    private TextView resultText;

    private Button clearBtn;
    private Button submitBtn;
    private Button saveBtn;
    private Button showBtn;

    private String outputUnitSymbol;
    private String inputUnitSymbol;
    private String currentInput;
    private String currentOutput;

    private LocalDateTime lastConversionTimestamp;
    private List<Conversion> savedConversions;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Definir variables
        inputUnitSymbol = "";
        outputUnitSymbol = "";
        savedConversions = new ArrayList<>();

        // Definir handler para introducción de cantidad
        userInput = findViewById(R.id.user_input);
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            // Mostrar botón de borrar si el EditText no esta vacio
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    clearBtn.setVisibility(View.VISIBLE);
                } else {
                    clearBtn.setVisibility(View.GONE);
                }
            }
        });

        // Definir handler para selección de tipo de conversión
        // Mostrar boton de borrar si algún RadioButton ha sido seleccionado
        conversionRadioGroup = findViewById(R.id.conversion_radio_group);
        conversionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) {
                clearBtn.setVisibility(View.GONE);
            } else {
                clearBtn.setVisibility(View.VISIBLE);
            }
        });

        // Especificar handler para pulsación del botón convertir
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(btnView  -> submitBtnHandler());

        // Especificar handler para pulsación del botón borrar
        clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(btnView -> clearBtnHandler());

        // Especificar handler para pulsación del botón guardar
        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(btnView -> saveBtnHandler());

        // Especificar handler para pulsación del botón mostrar resultados guardados
        showBtn = findViewById(R.id.showBtn);
        showBtn.setOnClickListener(btnView -> showBtnHandler());

        // Ocultar layout de resultado por defecto
        resultLayout = findViewById(R.id.resultLayout);
        resultLayout.setVisibility(View.GONE);
        resultText = findViewById(R.id.resultText);

        // Ocultar botones de borrar y mostrar conversiones guardadas por defecto
        clearBtn.setVisibility(View.GONE);
        showBtn.setVisibility(View.GONE);

        // Ocultar layout de conversiones guardadas por defecto
        savedLayout = findViewById(R.id.savedLayout);
        savedLayout.setVisibility(View.GONE);


        // Recuperar conversiones guardadas anteriormente en SharedPreferences
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        if (sp.contains(SAVED_CONVERSIONS_OBJ_KEY)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Conversion>>(){}.getType();
            savedConversions = gson.fromJson(sp.getString(SAVED_CONVERSIONS_OBJ_KEY, ""), type);
            showBtn.setVisibility(View.VISIBLE);
            savedLayout.setVisibility(View.VISIBLE);
        }
        if (savedConversions == null) {
            savedConversions = new ArrayList<>();
        }
        // Recycler view encargado de listar el historial de conversiones
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Adapter para RecyclerView, que especifica como se debe adaptar
        // cada elemento en la lista de conversiones guardadas a una View
        adapter = new MyAdapter(savedConversions);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Almacenar conversiones guardadas en SharedPreferences
        SharedPreferences sp = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        editor.putString(SAVED_CONVERSIONS_OBJ_KEY, gson.toJson(savedConversions));
        editor.apply();
    }

    /**
     * Handler para pulsación del botón calcular.
     * Si el valor introducido por el usuario es válido, y si ha seleccionado
     * el tipo de conversión, convertir y mostrar resultado.
     */
    private void submitBtnHandler() {
        Double validNumberFromQty = getValidDoubleFromInput();
        if (validNumberFromQty != null) {
            // getConvertFunction() define inputUnitSymbol y outputUnitSymbol
            // en cada ejecución.
            Double result = getConvertFunction().apply(validNumberFromQty);
            currentInput = validNumberFromQty + inputUnitSymbol;
            System.out.println(inputUnitSymbol + outputUnitSymbol);
            currentOutput = result + outputUnitSymbol;
            if (outputUnitSymbol.isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.radio_must_select),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            lastConversionTimestamp = LocalDateTime.now();
            resultLayout.setVisibility(View.VISIBLE);
            resultText.setText(currentOutput);
        }
    }

    /**
     * Handler para pulsación del botón borrar.
     * Elimina el input del usuario y el tipo de conversión seleccionada.
     */
    private void clearBtnHandler() {
        userInput.setText("");
        conversionRadioGroup.clearCheck();
        resultLayout.setVisibility(View.GONE);
    }

    /**
     * Handler para pulsación del botón guardar.
     * Añade la última conversión al listado de conversiones guardadas.
     */
    private void saveBtnHandler() {
        // El usuario debe realizar al menos una conversión
        if (currentInput == null || currentOutput == null || lastConversionTimestamp == null) {
            Toast.makeText(this, R.string.no_conversion_to_save, Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println(currentInput + currentOutput + lastConversionTimestamp);
        // Hacer botón de mostrar/ocultar conversiones guardadas visible
        if (showBtn.getVisibility() == View.GONE) {
            showBtn.setVisibility(View.VISIBLE);
            if (showBtn.getText().equals(getString(R.string.hide))) {
                showBtn.setText(getResources().getString(R.string.show));
            }
        }
        // Crear instancia de Conversion y añadirla al listado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeStr = lastConversionTimestamp.format(formatter);
        Conversion conversion = new Conversion(currentInput, currentOutput, dateTimeStr);
        savedConversions.add(0, conversion);
        // Notificar al adapter
        adapter.notifyItemInserted(0);
        Toast.makeText(this, R. string.saved_value, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handler para pulsación del botón mostrar/ocultar.
     * Alterna entre mostrar y ocultar layout de resultados guardados.
     */
    private void showBtnHandler() {
        if (savedConversions.isEmpty()) {
            showBtn.setVisibility(View.GONE);
            savedLayout.setVisibility(View.GONE);
            return;
        }
        if (showBtn.getText().equals(getString(R.string.show))) {
            showBtn.setText(getResources().getString(R.string.hide));
            savedLayout.setVisibility(View.VISIBLE);
        } else {
            showBtn.setText(getResources().getString(R.string.show));
            savedLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Helper method encargado de parsear el valor introducido por el usuario.
     * Devuelve un double en caso de ser un valor numérico correcto.
     * Muestra un Toast de error en caso de ser incorrecto.
     */
    private Double getValidDoubleFromInput() {
        String qtyStr = userInput.getText().toString();
        try {
            return Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getResources().getString(R.string.qty_nan), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Helper method encargado de devolver una funcion lambda que aplique
     * la conversion correspondiente al RadioButton seleccionado por el usuario.
     */
    private Function<Double, Double> getConvertFunction() {
        int checkedId = conversionRadioGroup.getCheckedRadioButtonId();

        if (checkedId == R.id.radio_m_km) {
            inputUnitSymbol = "m";
            outputUnitSymbol = "km";
            return (n) -> Math.round((n / 1000.0) * 100.0) / 100.0;
        } else if (checkedId == R.id.radio_g_kg) {
            inputUnitSymbol = "g";
            outputUnitSymbol = "kg";
            return (n) -> Math.round((n / 1000.0) * 100.0) / 100.0;
        } else if (checkedId == R.id.radio_cels_fahr) {
            inputUnitSymbol = "°C";
            outputUnitSymbol = "°F";
            return (n) -> Math.round(((n * 1.8) + 32) * 100.0) / 100.0;
        } else {
            inputUnitSymbol = "";
            outputUnitSymbol = "";
            return (n) -> n;
        }
    }
}