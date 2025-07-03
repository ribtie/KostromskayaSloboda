package com.example.kostromskayasloboda;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.*;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Gramota extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    TextView nameGramota;
    AppCompatButton download;
    AppCompatButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gramota);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // до Android 10
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        nameGramota = findViewById(R.id.name);
        nameGramota.setText(name);
        download = findViewById(R.id.download);
        back=findViewById(R.id.backIn);
    }
    public Bitmap takeScreenshot(Activity activity) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bitmap;
    }
    public void saveScreenshotToGallery(Context context, Bitmap bitmap) {
        String filename = "screenshot_" + System.currentTimeMillis() + ".png";
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots");

            ContentResolver resolver = context.getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                fos = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // Для Android 9 и ниже
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
            ).toString() + "/Screenshots";

            File file = new File(imagesDir);
            if (!file.exists()) file.mkdirs();

            File image = new File(imagesDir, filename);
            try {
                fos = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                // Добавим в галерею
                MediaScannerConnection.scanFile(context,
                        new String[]{image.getAbsolutePath()}, null, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void download(View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
            Toast.makeText(this, "Требуется разрешение для сохранения", Toast.LENGTH_SHORT).show();
            return;
        }

        back.setVisibility(INVISIBLE);
        download.setVisibility(INVISIBLE);
        Bitmap screenshot = takeScreenshot(this);
        saveScreenshotToGallery(this, screenshot);
        Toast.makeText(this, "Грамота сохранена", Toast.LENGTH_SHORT).show();
        back.setVisibility(VISIBLE);
        download.setVisibility(VISIBLE);
    }

    @SuppressLint("MissingSuperCall")
    @Override public void onBackPressed() {


    }
    public void backToMainMenu(View v) {
        Intent main = new Intent(Gramota.this, MainActivity.class);
        startActivity(main);
    }

}
