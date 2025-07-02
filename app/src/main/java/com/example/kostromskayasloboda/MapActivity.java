package com.example.kostromskayasloboda;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MapActivity extends AppCompatActivity implements MapView.OnObjectClickListener {
    private MapView mapView;
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private ProgressBar progressBar;
    private TextView countTextView;
    private int foundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        countTextView = findViewById(R.id.countTextView);
        progressBar = findViewById(R.id.progressBar);

        mapView.setOnObjectClickListener(this);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        // Для тестирования установим фиктивное местоположение
        mapView.updateUserPosition(57.737f, 41.010f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Для работы приложения нужны разрешения на местоположение",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onObjectClick(int objectIndex) {
        if (isUserNearObject(objectIndex)) {
            showTestForObject(objectIndex);
        } else {
            Toast.makeText(this, "Подойдите ближе к этому объекту", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUserNearObject(int objectIndex) {
        // В реальном приложении здесь нужно проверять расстояние до объекта
        // Для тестирования всегда возвращаем true
        return true;
    }

    private void showTestForObject(int objectIndex) {
        // Получаем Bitmap из ресурсов вместо MapView
        int[] objectResIds = {
                R.drawable.melnitsa, R.drawable.melnitsa2, R.drawable.chasovna,
                R.drawable.domgar, R.drawable.dom, R.drawable.domic,
                R.drawable.domishe, R.drawable.usadba, R.drawable.konusna
        };

        Bitmap correctImage = BitmapFactory.decodeResource(getResources(), objectResIds[objectIndex]);
        String correctText = getObjectName(objectIndex);

        int wrongIndex1 = (objectIndex + 3) % 9;
        int wrongIndex2 = (objectIndex + 6) % 9;

        Bitmap wrongImage1 = BitmapFactory.decodeResource(getResources(), objectResIds[wrongIndex1]);
        Bitmap wrongImage2 = BitmapFactory.decodeResource(getResources(), objectResIds[wrongIndex2]);

        String wrongText1 = getObjectName(wrongIndex1);
        String wrongText2 = getObjectName(wrongIndex2);

        TestBox.onCorrectAnswerGlobal = () -> {
            mapView.setObjectFound(objectIndex, true);
            foundCount++;
            updateProgress();
            showObjectInfo(objectIndex);
        };

        TestBox testBox = new TestBox(
                correctImage, correctText,
                wrongImage1, wrongText1,
                wrongImage2, wrongText2
        );
        testBox.show(getSupportFragmentManager(), "test_dialog");
    }

    private String getObjectName(int index) {
        String[] names = getResources().getStringArray(R.array.object_names);
        return names[index];
    }

    private void updateProgress() {
        countTextView.setText(foundCount + " из 9");
        progressBar.setProgress(foundCount);
    }

    private void showObjectInfo(int objectIndex) {
        String[] titles = getResources().getStringArray(R.array.object_titles);
        String[] descriptions = getResources().getStringArray(R.array.object_descriptions);
        int[] objectResIds = {
                R.drawable.melnitsa, R.drawable.melnitsa2, R.drawable.chasovna,
                R.drawable.domgar, R.drawable.dom, R.drawable.domic,
                R.drawable.domishe, R.drawable.usadba, R.drawable.konusna
        };

        // Теперь передаем только resource ID изображения
        ObjectInfoDialog dialog = ObjectInfoDialog.newInstance(
                objectResIds[objectIndex],
                titles[objectIndex],
                descriptions[objectIndex]
        );
        dialog.show(getSupportFragmentManager(), "info_dialog");
    }
}