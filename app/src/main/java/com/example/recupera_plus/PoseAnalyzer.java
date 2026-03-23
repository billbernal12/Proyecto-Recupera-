package com.example.recupera_plus;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

public class PoseAnalyzer {
    private PoseLandmarker poseLandmarker;
    private OnPoseDetectedListener listener;

    public interface OnPoseDetectedListener {
        void onPoseDetected(PoseLandmarkerResult result);
    }

    public PoseAnalyzer(Context context, OnPoseDetectedListener listener) {
        this.listener = listener;

        // Configurar el modelo base
        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task") // Modelo descargado
                .build();

        // Configurar opciones del landmarker
        PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener((PoseLandmarkerResult result, MPImage input) -> {
                    if (this.listener != null) {
                        this.listener.onPoseDetected(result);
                    }
                })
                .build();

        // Crear instancia del detector
        poseLandmarker = PoseLandmarker.createFromOptions(context, options);
        Log.i("PoseAnalyzer", "Modelo cargado correctamente");

    }

    public void analyzeFrame(MPImage image) {
        if (poseLandmarker != null) {
            poseLandmarker.detectAsync(image, System.currentTimeMillis());
        }
    }

    public void close() {
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
    }
}
