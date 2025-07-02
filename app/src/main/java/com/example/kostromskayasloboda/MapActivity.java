package com.example.kostromskayasloboda;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements MapView.OnObjectClickListener {
    private MapView mapView;
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private ProgressBar progressBar;
    private TextView countTextView;
    private int foundCount = 0;
    private ArrayList<String> foundObjectsList = new ArrayList<>();

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

        ObjectInfoDialog dialog = ObjectInfoDialog.newInstance(
                objectResIds[objectIndex],
                titles[objectIndex],
                descriptions[objectIndex]
        );
        dialog.show(getSupportFragmentManager(), "info_dialog");
    }

    private void showTestForObject(int objectIndex) {
        int[] objectResIds = {
                R.drawable.melnitsa, R.drawable.melnitsa2, R.drawable.chasovna,
                R.drawable.domgar, R.drawable.dom, R.drawable.domic,
                R.drawable.domishe, R.drawable.usadba, R.drawable.konusna
        };

        Bitmap correctImage = BitmapFactory.decodeResource(getResources(), objectResIds[objectIndex]);
        String correctText = getObjectName(objectIndex);

        int wrongIndex1 = (objectIndex + 3) % 9;
        int wrongIndex2 = (objectIndex + 6) % 9;

        TestBox.onCorrectAnswerGlobal = () -> {
            mapView.setObjectFound(objectIndex, true);
            foundCount++;
            foundObjectsList.add(getObjectName(objectIndex));
            updateProgress();
            showObjectInfo(objectIndex);

            if (foundCount == 9) {
                showCompletionDialog();
            }
        };

        TestBox testBox = new TestBox(
                correctImage, correctText,
                BitmapFactory.decodeResource(getResources(), objectResIds[wrongIndex1]),
                getObjectName(wrongIndex1),
                BitmapFactory.decodeResource(getResources(), objectResIds[wrongIndex2]),
                getObjectName(wrongIndex2)
        );
        testBox.show(getSupportFragmentManager(), "test_dialog");
    }

    private void showCompletionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_completion, null);
        EditText nameInput = dialogView.findViewById(R.id.nameInput);

        new AlertDialog.Builder(this)
                .setTitle("Поздравляем!")
                .setView(dialogView)
                .setPositiveButton("Готово", (dialog, which) -> {
                    String userName = nameInput.getText().toString().trim();
                    if (!userName.isEmpty()) {
                        showCompletionScreen(userName);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showCompletionScreen(String userName) {
        Intent intent = new Intent(this, CompletionActivity.class);
        intent.putExtra("USER_NAME", userName);
        intent.putStringArrayListExtra("FOUND_OBJECTS", foundObjectsList);
        startActivity(intent);
        finish();
    }

    public void onFoundObjectsClick(View view) {
        if (!foundObjectsList.isEmpty()) {
            Intent intent = new Intent(this, FoundObjectsActivity.class);
            intent.putStringArrayListExtra("FOUND_OBJECTS", foundObjectsList);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Вы еще не нашли ни одного объекта", Toast.LENGTH_SHORT).show();
        }
    }
}