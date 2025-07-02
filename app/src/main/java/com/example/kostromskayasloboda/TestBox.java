package com.example.kostromskayasloboda;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestBox extends DialogFragment {
    public static Runnable onCorrectAnswerGlobal;

    private Bitmap correctImage;
    private String correctText;
    private Bitmap[] wrongImages = new Bitmap[2];
    private String[] wrongTexts = new String[2];
    private int correctIndex;

    public TestBox(Bitmap correctImage, String correctText,
                   Bitmap wrongImage1, String wrongText1,
                   Bitmap wrongImage2, String wrongText2) {
        this.correctImage = correctImage;
        this.correctText = correctText;
        this.wrongImages[0] = wrongImage1;
        this.wrongImages[1] = wrongImage2;
        this.wrongTexts[0] = wrongText1;
        this.wrongTexts[1] = wrongText2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_test, container, false);
        ViewGroup containerAnswers = view.findViewById(R.id.container_answers);

        List<AnswerOption> options = Arrays.asList(
                new AnswerOption(correctImage, correctText, true),
                new AnswerOption(wrongImages[0], wrongTexts[0], false),
                new AnswerOption(wrongImages[1], wrongTexts[1], false)
        );
        Collections.shuffle(options);

        for (int i = 0; i < options.size(); i++) {
            AnswerOption option = options.get(i);
            View item = inflater.inflate(R.layout.item_test_option, containerAnswers, false);

            ImageView imageView = item.findViewById(R.id.option_image);
            TextView textView = item.findViewById(R.id.option_text);

            imageView.setImageBitmap(option.image);
            textView.setText(option.text);

            if (option.isCorrect) correctIndex = i;

            final int position = i;
            item.setOnClickListener(v -> {
                if (position == correctIndex) {
                    Toast.makeText(getContext(), "Верно!", Toast.LENGTH_SHORT).show();
                    if (onCorrectAnswerGlobal != null) onCorrectAnswerGlobal.run();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Неверно! Попробуйте еще", Toast.LENGTH_SHORT).show();
                }
            });

            containerAnswers.addView(item);
        }

        return view;
    }

    private static class AnswerOption {
        Bitmap image;
        String text;
        boolean isCorrect;

        AnswerOption(Bitmap image, String text, boolean isCorrect) {
            this.image = image;
            this.text = text;
            this.isCorrect = isCorrect;
        }
    }
}



