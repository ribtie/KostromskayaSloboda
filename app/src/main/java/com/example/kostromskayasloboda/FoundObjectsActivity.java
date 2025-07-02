package com.example.kostromskayasloboda;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class FoundObjectsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_objects);

        ArrayList<String> foundObjects = getIntent().getStringArrayListExtra("FOUND_OBJECTS");
        LinearLayout container = findViewById(R.id.objectsContainer);

        for (String object : foundObjects) {
            TextView textView = new TextView(this);
            textView.setText("• " + object);
            textView.setTextSize(18);
            textView.setPadding(0, 16, 0, 16);
            container.addView(textView);
        }

        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> finish());
    }
}
