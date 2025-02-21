package com.example.aplicacion;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.*;
import om.example.aplicacion.R;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int MAX_FPS = 5;  // Máximo de frames por segundo
    private boolean isSendingFrames = false;
    private Handler frameHandler;
    private Runnable frameRunnable;
    private PreviewView cameraView;
    private ImageView processedImageView;
    private Button sendFramesButton;
    private ExecutorService cameraExecutor;
    private boolean isCameraInitialized = false;
    private OkHttpClient client;  // Cliente HTTP optimizado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera_view);
        processedImageView = findViewById(R.id.processed_image);
        sendFramesButton = findViewById(R.id.send_frames_button);

        client = new OkHttpClient.Builder()
                .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)  // Tiempo límite por petición
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        frameHandler = new Handler();

        sendFramesButton.setOnClickListener(v -> toggleFrameSending());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                isCameraInitialized = true;
                runOnUiThread(() -> Toast.makeText(this, "Cámara iniciada correctamente", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleFrameSending() {
        if (isSendingFrames) {
            isSendingFrames = false;
            frameHandler.removeCallbacks(frameRunnable);
            runOnUiThread(() -> Toast.makeText(this, "Detenido el envío de frames", Toast.LENGTH_SHORT).show());
        } else {
            isSendingFrames = true;
            runOnUiThread(() -> Toast.makeText(this, "Enviando frames...", Toast.LENGTH_SHORT).show());
            startFrameSending();
        }
    }

    private void startFrameSending() {
        frameRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSendingFrames) {
                    captureFrame(bitmap -> {
                        if (bitmap != null) {
                            sendFrameToServer(bitmap);
                        }
                    });
                    frameHandler.postDelayed(this, 1000 / MAX_FPS);
                }
            }
        };
        frameHandler.post(frameRunnable);
    }

    private void captureFrame(OnBitmapCapturedListener listener) {
        Bitmap bitmap = cameraView.getBitmap();
        if (bitmap == null) {
            return;
        }
        listener.onBitmapCaptured(bitmap);
    }

    private void sendFrameToServer(Bitmap bitmap) {
        File imageFile = new File(getCacheDir(), "frame.jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);  // Reducir calidad para mejorar rendimiento
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RequestBody fileBody = RequestBody.create(imageFile, MediaType.get("image/jpeg"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "frame.jpg", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://172.16.213.20:5000/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show());
                } else {
                    byte[] imageData = response.body().bytes();
                    runOnUiThread(() -> {
                        Bitmap processedBitmap = ImageUtils.byteArrayToBitmap(imageData);
                        processedImageView.setImageBitmap(processedBitmap);
                    });
                }
                imageFile.delete();  // Eliminar archivo temporal después de enviarlo
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        frameHandler.removeCallbacks(frameRunnable);
    }

    public interface OnBitmapCapturedListener {
        void onBitmapCaptured(Bitmap bitmap);
    }
}