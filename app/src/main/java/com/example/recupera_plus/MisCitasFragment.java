package com.example.recupera_plus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MisCitasFragment extends Fragment {

    private Button btnAgendarCita, btnProximas, btnHistorial;
    private LinearLayout contenedorCitas;
    private DatabaseReference citasRef;
    private String userId;
    private boolean mostrandoProximas = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mis_citas, container, false);

        // Referencias
        btnAgendarCita = view.findViewById(R.id.btnAgendarCita);
        btnProximas = view.findViewById(R.id.btnProximas);
        btnHistorial = view.findViewById(R.id.btnHistorial);
        contenedorCitas = view.findViewById(R.id.contenedorCitas);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        citasRef = FirebaseDatabase.getInstance().getReference("citas").child(userId);

        // Mostrar las próximas citas por defecto
        mostrarCitas(true);

        // Botón “Próximas Citas”
        btnProximas.setOnClickListener(v -> {
            mostrandoProximas = true;
            mostrarCitas(true);
        });

        // Botón “Historial de Citas”
        btnHistorial.setOnClickListener(v -> {
            mostrandoProximas = false;
            mostrarCitas(false);
        });

        // Botón “Agendar Cita”
        btnAgendarCita.setOnClickListener(v -> {
            Fragment agendarCitaFragment = new AgendarCitaFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, agendarCitaFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void mostrarCitas(boolean mostrarProximas) {
        contenedorCitas.removeAllViews();

        citasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mostrarMensaje("No tienes citas registradas aún");
                    return;
                }

                boolean hayCitas = false;
                long ahora = System.currentTimeMillis();

                for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                    String especialista = citaSnapshot.child("especialista").getValue(String.class);
                    String descripcion = citaSnapshot.child("descripcion").getValue(String.class);
                    String estado = citaSnapshot.child("estado").getValue(String.class);
                    String fechaReserva = citaSnapshot.child("fechaReserva").getValue(String.class); // ← CORREGIDO

                    if (especialista == null || fechaReserva == null) continue;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        Date fechaCita = sdf.parse(fechaReserva);
                        if (fechaCita == null) continue;

                        boolean esFutura = fechaCita.getTime() >= ahora;

                        // Filtro: próximas o historial
                        if ((mostrarProximas && esFutura) || (!mostrarProximas && !esFutura)) {
                            hayCitas = true;
                            agregarCitaVista(especialista, fechaReserva, descripcion, estado);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!hayCitas) {
                    mostrarMensaje(mostrarProximas ?
                            "No tienes próximas citas" :
                            "No tienes historial de citas");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mostrarMensaje("Error al cargar citas");
            }
        });
    }

    private void agregarCitaVista(String especialista, String fecha, String descripcion, String estado) {
        TextView citaView = new TextView(getContext());
        citaView.setText(
                "👨‍⚕️ Especialista: " + especialista + "\n" +
                        "📅 Fecha: " + fecha + "\n" +
                        "📝 " + descripcion + "\n" +
                        "🚨 Estado: " + estado
        );
        citaView.setTextSize(15);
        citaView.setTextColor(getResources().getColor(android.R.color.black));
        citaView.setPadding(24, 24, 24, 24);
        citaView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 25);
        citaView.setLayoutParams(params);

        contenedorCitas.addView(citaView);
    }

    private void mostrarMensaje(String mensaje) {
        contenedorCitas.removeAllViews();
        TextView mensajeView = new TextView(getContext());
        mensajeView.setText(mensaje);
        mensajeView.setTextSize(15);
        mensajeView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mensajeView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mensajeView.setPadding(24, 24, 24, 24);
        contenedorCitas.addView(mensajeView);
    }
}
