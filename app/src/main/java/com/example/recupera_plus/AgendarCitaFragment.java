package com.example.recupera_plus;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AgendarCitaFragment extends Fragment {

    private TextView tvNombreUsuario, tvContadorPalabras;
    private EditText etDescripcion;
    private Spinner spinnerEspecialistas;
    private Button btnReservarCita;

    private String nombreCompletoUsuario = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agendar_cita, container, false);

        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
        tvContadorPalabras = view.findViewById(R.id.tvContadorPalabras);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        spinnerEspecialistas = view.findViewById(R.id.spinnerEspecialistas);
        btnReservarCita = view.findViewById(R.id.btnReservarCita);

        // 🔹 Obtener usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Cargar nombre desde la BD
            FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String nombres = snapshot.child("nombres").getValue(String.class);
                            String apellidos = snapshot.child("apellidos").getValue(String.class);
                            nombreCompletoUsuario = (nombres != null ? nombres : "") + " " +
                                    (apellidos != null ? apellidos : "");
                            tvNombreUsuario.setText(nombreCompletoUsuario.trim());
                        } else {
                            tvNombreUsuario.setText("Datos no encontrados");
                        }
                    })
                    .addOnFailureListener(e -> tvNombreUsuario.setText("Error al cargar datos"));
        } else {
            tvNombreUsuario.setText("No hay usuario activo");
        }

        // 🔹 Cargar lista de especialistas
        String[] especialistas = {"ROBERT VERGARA", "LESLI ARIAS", "MARÍA SALAZAR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, especialistas);
        spinnerEspecialistas.setAdapter(adapter);

        // 🔹 Contador de palabras (máx. 50)
        etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString().trim();
                int palabras = texto.isEmpty() ? 0 : texto.split("\\s+").length;
                tvContadorPalabras.setText(palabras + " / 50 palabras");
                if (palabras > 50) {
                    etDescripcion.setError("Máximo 50 palabras");
                }
            }
        });

        // 🔹 Acción del botón "Reservar Cita"
        btnReservarCita.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(requireContext(), "Debes iniciar sesión.", Toast.LENGTH_SHORT).show();
                return;
            }

            String especialista = spinnerEspecialistas.getSelectedItem().toString();
            String descripcion = etDescripcion.getText().toString().trim();

            if (descripcion.isEmpty()) {
                etDescripcion.setError("Describe tu situación");
                return;
            }

            // 🔹 Datos de la cita
            String uid = user.getUid();
            String fechaReserva = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            DatabaseReference citaRef = FirebaseDatabase.getInstance()
                    .getReference("citas")
                    .child(uid)
                    .push();

            // Guardar toda la cita en un solo objeto
            Map<String, Object> citaData = new HashMap<>();
            citaData.put("nombreUsuario", nombreCompletoUsuario);
            citaData.put("especialista", especialista);
            citaData.put("descripcion", descripcion);
            citaData.put("fechaReserva", fechaReserva);
            citaData.put("estado", "Pendiente");

            citaRef.setValue(citaData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "✅ Cita reservada con " + especialista, Toast.LENGTH_SHORT).show();

                        // 🔹 Regresar al fragmento MisCitas y actualizar automáticamente
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, new MisCitasFragment())
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "❌ Error al guardar cita", Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}
