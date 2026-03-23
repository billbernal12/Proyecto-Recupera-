package com.example.recupera_plus;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    LinearLayout navInicio, navTerapia, navCitas, navAsistencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 🔹 Cambiar color de la barra de estado manualmente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        navInicio = findViewById(R.id.navInicio);
        navTerapia = findViewById(R.id.navTerapia);
        navCitas = findViewById(R.id.navCitas);
        navAsistencia = findViewById(R.id.navAsistencia);

        // Fragmento inicial (Inicio)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new InicioFragment())
                    .commit();
        }

        // Navegación
        navInicio.setOnClickListener(v -> openFragment(new InicioFragment()));
        navTerapia.setOnClickListener(v -> openFragment(new TerapiaFragment()));
        navCitas.setOnClickListener(v -> openFragment(new MisCitasFragment()));
        navAsistencia.setOnClickListener(v -> openFragment(new AsistenciaFragment()));
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
