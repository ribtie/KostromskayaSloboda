package com.example.kostromskayasloboda;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class Church1 extends DialogFragment {
    public static final String RESULT_KEY = "correct_answer_result";

    public interface OnCorrectAnswerListener {
        void onCorrectAnswer();
    }
    public static Runnable onCorrectAnswerGlobal;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.church1_test, container, false);

        LinearLayout rightAnswer = view.findViewById(R.id.right_answer);
        LinearLayout wrongAnswer1 = view.findViewById(R.id.wrong_answer1);
        LinearLayout wrongAnswer2 = view.findViewById(R.id.wrong_answer2);



        rightAnswer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "✅ Верно!", Toast.LENGTH_SHORT).show();
            if (onCorrectAnswerGlobal != null) {
                onCorrectAnswerGlobal.run();
            }
            dismiss();
        });


        View.OnClickListener wrongAnswerListener = v ->
                Toast.makeText(getContext(), "❌ Неверно", Toast.LENGTH_SHORT).show();

        wrongAnswer1.setOnClickListener(wrongAnswerListener);
        wrongAnswer2.setOnClickListener(wrongAnswerListener);

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



