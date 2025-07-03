package com.example.kostromskayasloboda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class EndGame extends AppCompatActivity {

    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endgame);
        name = findViewById(R.id.nameInput);
    }
    public void onClickGramota(View view) {
        if(!name.getText().toString().isEmpty()) {
            Intent intent = new Intent(EndGame.this, Gramota.class);
            intent.putExtra("name",name.getText().toString());
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"Пожалуйста, введи свое имя!",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override public void onBackPressed() {


    }


}
