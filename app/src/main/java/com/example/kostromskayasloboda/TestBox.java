package com.example.kostromskayasloboda;
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

public class TestBox extends DialogFragment {
    public static Runnable onCorrectAnswerGlobal;

    private int[] images;      // Ресурсы картинок вариантов
    private String[] texts;    // Тексты вариантов

    private String[] decs;    // Тексты вариантов
    private int correctIndex;  // Индекс правильного варианта после перемешивания


    private int podskazkiImage;

    private String podskazkaText;
    public TestBox(int correctImage, String correctText, String correctDesc,
                   int wrongImage1, String wrongText1, String wrongDesc1,
                   int wrongImage2, String wrongText2, String wrongDesc2,
                   int podskazkiImage, String podskazkaText) {
        // Задаем исходные варианты
        images = new int[]{correctImage, wrongImage1, wrongImage2};
        texts = new String[]{correctText, wrongText1, wrongText2};
        decs = new String[]{correctDesc, wrongDesc1, wrongDesc2};
        this.podskazkiImage = podskazkiImage;
        this.podskazkaText = podskazkaText;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_box_dialog, container, false);
        LinearLayout containerAnswers = view.findViewById(R.id.container_answers);

        // Перемешаем индексы вариантов случайно
        List<Integer> order = Arrays.asList(0, 1, 2);
        Collections.shuffle(order);

        for (int i = 0; i < order.size(); i++) {
            int idx = order.get(i);
            View item = inflater.inflate(R.layout.item_test, containerAnswers, false);
            ImageView imageView = item.findViewById(R.id.image_answer);
            TextView textView = item.findViewById(R.id.text_answer);
            TextView textView2 = item.findViewById(R.id.text_desc);
            imageView.setImageResource(images[idx]);
            textView.setText(texts[idx]);
            textView2.setText(decs[idx]);
            // Запомнили, какой индекс правильного варианта после перемешивания
            if (idx == 0) { // 0 — это правильный вариант в исходных данных
                correctIndex = i;
            }

            final int finalI = i;
            item.setOnClickListener(v -> {
                if (finalI == correctIndex) {
                    Toast.makeText(getContext(), "✅ Верно!", Toast.LENGTH_SHORT).show();
                    if (onCorrectAnswerGlobal != null) {
                        onCorrectAnswerGlobal.run();
                    }
                    ObjectInfoDialog right = new ObjectInfoDialog(images[0],texts[0],decs[0],"Верно!","Отлично");
                    right.show(this.getParentFragmentManager(), "Верный диалог");
                    dismiss();
                } else {
                    ObjectInfoDialog right = new ObjectInfoDialog(podskazkiImage,"Подсказка",podskazkaText,"Попробуй еще раз!","Выбрать другой");
                    right.show(this.getParentFragmentManager(), "Неверный диалог диалог");
                }
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



