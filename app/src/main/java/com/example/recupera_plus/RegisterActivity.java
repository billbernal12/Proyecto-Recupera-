package com.example.recupera_plus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    // layouts (pasos)
    private LinearLayout layoutPaso1, layoutPaso2, layoutPaso3;

    // paso 1 widgets
    private Spinner spTipoDoc;
    private TextInputEditText etNumDoc, etFechaNac;
    private CheckBox cbTyC, cbPromo;
    private Button btnContinuar;

    // paso 2 widgets
    private EditText etPeso, etTalla;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etNombres, etApellidos, etCelular;
    private Spinner spGenero;
    private Button btnContinuar2;

    // paso 3 widgets
    private Button btnFinalizar;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    // Objeto usuario global
    private Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Authentication y Realtime Database
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Layouts
        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        layoutPaso3 = findViewById(R.id.layoutPaso3);

        spTipoDoc = findViewById(R.id.spTipoDoc);
        etNumDoc = findViewById(R.id.etNumDoc);
        etFechaNac = findViewById(R.id.etFechaNac);
        cbTyC = findViewById(R.id.cbTyC);
        cbPromo = findViewById(R.id.cbPromo);
        btnContinuar = findViewById(R.id.btnContinuar);

        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etCelular = findViewById(R.id.etCelular);
        etPeso = findViewById(R.id.etPeso);
        etTalla = findViewById(R.id.etTalla);

        spGenero = findViewById(R.id.spGenero);
        btnContinuar2 = findViewById(R.id.btnContinuar2);

        btnFinalizar = findViewById(R.id.btnFinalizar);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Mostrar solo paso 1 al inicio
        layoutPaso1.setVisibility(View.VISIBLE);
        layoutPaso2.setVisibility(View.GONE);
        layoutPaso3.setVisibility(View.GONE);

        // Poblar spinner tipo de documento
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_documento, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoDoc.setAdapter(adapter);

        // Poblar spinner género
        ArrayAdapter<CharSequence> adapterGenero = ArrayAdapter.createFromResource(
                this, R.array.generos, android.R.layout.simple_spinner_item);
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenero.setAdapter(adapterGenero);

        // DatePicker para la fecha de nacimiento
        etFechaNac.setOnClickListener(v -> showDatePicker());

        // Botón continuar paso 1
        btnContinuar.setOnClickListener(v -> validarPaso1());

        // Botón continuar paso 2
        btnContinuar2.setOnClickListener(v -> validarPaso2());

        // Botón finalizar paso 3 (guardar en Firebase)
        btnFinalizar.setOnClickListener(v -> guardarEnFirebase());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            etFechaNac.setText(fecha);
        }, yy, mm, dd);
        dp.show();
    }

    private void validarPaso1() {
        String tipo = spTipoDoc.getSelectedItem() != null ? spTipoDoc.getSelectedItem().toString() : "";
        String num = etNumDoc.getText() != null ? etNumDoc.getText().toString().trim() : "";
        String fnac = etFechaNac.getText() != null ? etFechaNac.getText().toString().trim() : "";

        if (TextUtils.isEmpty(num)) {
            mostrar("Ingresa el N° de documento");
            return;
        }
        if ("DNI".equals(tipo) && num.length() != 8) {
            mostrar("El DNI debe tener 8 dígitos");
            return;
        }
        if (TextUtils.isEmpty(fnac)) {
            mostrar("Selecciona tu fecha de nacimiento");
            return;
        }
        if (!cbTyC.isChecked()) {
            mostrar("Debes aceptar los Términos y Condiciones");
            return;
        }

        // Guardamos en objeto usuario
        usuario.tipoDoc = tipo;
        usuario.numDoc = num;
        usuario.fechaNac = fnac;
        usuario.aceptaTerminos = cbTyC.isChecked();
        usuario.aceptaPromociones = cbPromo.isChecked();

        // Pasar a Paso 2
        layoutPaso1.setVisibility(View.GONE);
        layoutPaso2.setVisibility(View.VISIBLE);
    }

    private void validarPaso2() {
        String nombres = etNombres.getText() != null ? etNombres.getText().toString().trim() : "";
        String apellidos = etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
        String celular = etCelular.getText() != null ? etCelular.getText().toString().trim() : "";
        String genero = spGenero.getSelectedItem() != null ? spGenero.getSelectedItem().toString() : "";
        String peso = etPeso.getText() != null ? etPeso.getText().toString().trim() : "";
        String talla = etTalla.getText() != null ? etTalla.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombres)) {
            mostrar("Ingresa tus nombres completos");
            return;
        }
        if (TextUtils.isEmpty(apellidos)) {
            mostrar("Ingresa tus apellidos completos");
            return;
        }
        if (!celular.matches("^9\\d{8}$")) {
            mostrar("Ingresa un número de celular válido");
            return;
        }
        if (TextUtils.isEmpty(peso)) {
            mostrar("Ingresa tu peso");
            return;
        }
        if (TextUtils.isEmpty(talla)) {
            mostrar("Ingresa tu talla");
            return;
        }

        // Guardar datos en objeto usuario
        usuario.nombres = nombres;
        usuario.apellidos = apellidos;
        usuario.celular = celular;
        usuario.genero = genero;
        usuario.peso = Integer.parseInt(peso);
        usuario.talla = Integer.parseInt(talla);

        mostrar("Paso 2 OK");

        // Ir a Paso 3
        layoutPaso2.setVisibility(View.GONE);
        layoutPaso3.setVisibility(View.VISIBLE);
    }

    private void guardarEnFirebase() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrar("Ingresa un correo electrónico válido");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&].*")) {
            mostrar("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
            return;
        }
        if (!password.equals(confirmPassword)) {
            mostrar("Las contraseñas no coinciden");
            return;
        }

        // Crear el usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();  // Usamos el UID del usuario creado

                        // Guardamos los demás datos del usuario en Firebase Realtime Database
                        usuariosRef.child(userId).setValue(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    mostrar("Usuario registrado exitosamente");
                                    startActivity(new Intent(this, LoginActivity.class));  // Redirigir a LoginActivity
                                    finish();
                                })
                                .addOnFailureListener(e -> mostrar("Error: " + e.getMessage()));
                    } else {
                        mostrar("Error al crear el usuario: " + task.getException().getMessage());
                    }
                });
    }

    private void mostrar(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}