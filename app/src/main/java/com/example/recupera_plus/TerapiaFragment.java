package com.example.recupera_plus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerapiaFragment extends Fragment implements PoseAnalyzer.OnPoseDetectedListener {

    private PreviewView previewView;
    private TextView tvFeedback;
    private PoseAnalyzer poseAnalyzer;
    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terapia, container, false);

        previewView = view.findViewById(R.id.camera_preview);
        tvFeedback = view.findViewById(R.id.tvFeedback);

        cameraExecutor = Executors.newSingleThreadExecutor();
        poseAnalyzer = new PoseAnalyzer(requireContext(), this);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }

        return view;
    }

    /** Convierte ImageProxy (YUV_420_888) a Bitmap */
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // Y plane
        yBuffer.get(nv21, 0, ySize);

        // V and U are swapped compared to NV21 ordering: Media uses U,V planes.
        // Para NV21 necesitamos V then U.
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, out);
        byte[] jpegBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview (para que se vea la cámara)
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, (ImageProxy image) -> {
                    try {
                        // Convierte ImageProxy -> Bitmap
                        Bitmap bitmap = imageProxyToBitmap(image);

                        if (bitmap != null) {
                            // Opcional: rotar/escala el bitmap si usas cámara front/back
                            // Bitmap rotated = rotateBitmapIfNeeded(bitmap, image);

                            // Crear MPImage desde Bitmap (MediaPipe)
                            MPImage mpImage = new BitmapImageBuilder(bitmap).build();

                            // Analizar (esto usa poseLandmarker.detectAsync internamente)
                            poseAnalyzer.analyzeFrame(mpImage);

                            // Si quieres liberar el bitmap explícitamente:
                            // bitmap.recycle();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        image.close(); // ¡siempre cerrar ImageProxy!
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }


    @Override
    public void onPoseDetected(PoseLandmarkerResult result) {
        requireActivity().runOnUiThread(() -> {
            if (result.landmarks().isEmpty()) {
                tvFeedback.setText("No se detecta el cuerpo.");
            } else {
                tvFeedback.setText("Cuerpo detectado. Analizando piernas...");
                // Aquí luego calcularás los ángulos de rodilla y cadera
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        poseAnalyzer.close();
        cameraExecutor.shutdown();
    }
}
