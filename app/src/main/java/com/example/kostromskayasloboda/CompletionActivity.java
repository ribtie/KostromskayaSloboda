//package com.example.kostromskayasloboda;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class CompletionActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_endgame);
//
//        String userName = getIntent().getStringExtra("USER_NAME");
//        TextView congratsText = findViewById(R.id.congratsText);
//        congratsText.setText(String.format("Поздравляем, %s! Вы прошли наш квест!", userName));
//
//        Button mainMenuButton = findViewById(R.id.mainMenuButton);
//        mainMenuButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//        });
//
//        Button objectsButton = findViewById(R.id.objectsButton);
//        objectsButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, FoundObjectsActivity.class);
//            intent.putStringArrayListExtra("FOUND_OBJECTS",
//                    getIntent().getStringArrayListExtra("FOUND_OBJECTS"));
//            startActivity(intent);
//        });
//    }
//}
