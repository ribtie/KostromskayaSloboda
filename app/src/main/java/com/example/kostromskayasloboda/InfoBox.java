package com.example.kostromskayasloboda;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InfoBox extends DialogFragment {
    public static Runnable onCorrectAnswerGlobal;

    private int[] images = {R.drawable.spas,R.drawable.loh,R.drawable.chap,R.drawable.chasov,R.drawable.ilya,R.drawable.lipat,R.drawable.mel,R.drawable.kolod};      // Ресурсы картинок вариантов
    private String[] texts = {"Спасская церковь","Изба Лоховой","Изба Чапыгиной","Часовня Казанской иконы","Ильинская церковь","Дом Липатова","Мельница","Говорящий колодец"};    // Тексты вариантов

    private String[] decs = {"Древняя деревянная церковь клетского типа с шатровой колокольней и тремя луковичными главками, использовавшаяся летом.","Богато украшенный поволжский дом конца XVIII века, сочетающий жилое пространство и хлев под одной крышей.","Скромная крестьянская изба с земляным полом и соломенной крышей, отражающая быт беднейших слоёв деревни.","Крытая деревянная часовня с гульбищем, посвящённая Казанской иконе Божией Матери.","Двухъярусный храм с зимней и летней церковью, образец северного деревянного зодчества XVIII–XIX веков.","Двухэтажный дом-брус с черным двором и резным декором, принадлежавший пароходчику и грузоотправителю.","Деревянная ветряная «шатровка» с вращающейся шапкой и жерновами, использовавшаяся для помола зерна.","Реконструкция деревенского колодца с «голосом», рассказывающим истории музейной деревни."};
    public InfoBox() {
        // Задаем исходные варианты

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_box_dialog, container, false);
        LinearLayout containerAnswers = view.findViewById(R.id.container_answers);
        TextView text = view.findViewById(R.id.nazn);
        text.setText("Информация");
        for (int i = 0; i < images.length; i++) {
            View item = inflater.inflate(R.layout.item_test, containerAnswers, false);
            ImageView imageView = item.findViewById(R.id.image_answer);
            TextView textView = item.findViewById(R.id.text_answer);
            TextView textView2 = item.findViewById(R.id.text_desc);
            imageView.setImageResource(images[i]);
            int widthInDp = 100;
            int heightInDp = 75;

            float scale = getResources().getDisplayMetrics().density;
            int widthInPx = (int) (widthInDp * scale + 0.5f);
            int heightInPx = (int) (heightInDp * scale + 0.5f);

            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = widthInPx;
            params.height = heightInPx;
            imageView.setLayoutParams(params);
            textView.setText(texts[i]);
            textView2.setText(decs[i]);
            textView2.setVisibility(GONE);

            int finalI = i;
            item.setOnClickListener(v -> {

                    if (onCorrectAnswerGlobal != null) {
                        onCorrectAnswerGlobal.run();
                    }
                    ObjectInfoDialog right = new ObjectInfoDialog(images[finalI],texts[finalI],decs[finalI]);
                    right.show(this.getParentFragmentManager(), "Описание");

            });

            containerAnswers.addView(item);
        }

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





