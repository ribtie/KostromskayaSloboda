package com.example.kostromskayasloboda;
import static android.view.View.GONE;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

public class ObjectInfoDialog extends DialogFragment {
    public static Runnable onCorrectAnswerGlobal;

    private int image;      // Ресурсы картинок вариантов
    private String text;    // Тексты вариантов

    private String tech_var;
    private String decs;    // Тексты вариантов

    private String buttonTech;

    public ObjectInfoDialog(int correctImage, String correctText, String correctDesc, String tech, String button) {
        // Задаем исходные варианты
        image = correctImage;
        text = correctText;
        decs = correctDesc;
        tech_var = tech;
        buttonTech = button;
    }

    public ObjectInfoDialog(int correctImage, String correctText, String correctDesc) {
        // Задаем исходные варианты
        image = correctImage;
        text = correctText;
        decs = correctDesc;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_object_info, container, false);
        LinearLayout containerAnswers = view.findViewById(R.id.object_layout);

            ImageView imageView = containerAnswers.findViewById(R.id.object_image);
            TextView textView = containerAnswers.findViewById(R.id.object_title);
            TextView textView2 = containerAnswers.findViewById(R.id.object_description);
            TextView techvar = containerAnswers.findViewById(R.id.object_tech);
            AppCompatButton button = containerAnswers.findViewById(R.id.ok_button);
            imageView.setImageResource(image);
            textView.setText(text);
            textView2.setText(decs);
            if(tech_var.isEmpty()) techvar.setVisibility(GONE);
            else techvar.setText(tech_var);
            if(!buttonTech.isEmpty()) button.setText(buttonTech);
            button.setOnClickListener(v -> {

                    dismiss();

            });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Устанавливаем размеры диалога и позицию по центру
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }
}



