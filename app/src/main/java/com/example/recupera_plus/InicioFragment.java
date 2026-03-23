package com.example.recupera_plus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class InicioFragment extends Fragment {
    private ImageView imgLogout;

    public InicioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        imgLogout = view.findViewById(R.id.imgLogout);

        imgLogout.setOnClickListener(v -> cerrarSesion());

        TextView txtHola = view.findViewById(R.id.txtHola);
        Button btnVerTutorial = view.findViewById(R.id.btnVerTutorial);
        Button btnSaltar = view.findViewById(R.id.btnSaltar);

        btnVerTutorial.setOnClickListener(v ->
                txtHola.setText("Cargando tutorial... (aquí abrirías otra pantalla)")
        );

        btnSaltar.setOnClickListener(v ->
                txtHola.setText("¡Bienvenido de nuevo!")
        );

        return view;
    }

    private void cerrarSesion() {
        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Ir al LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
