package com.example.kostromskayasloboda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ObjectInfoDialog extends DialogFragment {
    private static final String ARG_IMAGE_RES_ID = "image_res_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "description";

    // Единственный метод для создания диалога
    public static ObjectInfoDialog newInstance(int imageResId, String title, String description) {
        ObjectInfoDialog fragment = new ObjectInfoDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_object_info, container, false);

        ImageView imageView = view.findViewById(R.id.object_image);
        TextView titleView = view.findViewById(R.id.object_title);
        TextView descView = view.findViewById(R.id.object_description);

        Bundle args = getArguments();
        if (args != null) {
            // Устанавливаем изображение из ресурса
            imageView.setImageResource(args.getInt(ARG_IMAGE_RES_ID));

            // Устанавливаем текст
            titleView.setText(args.getString(ARG_TITLE));
            descView.setText(args.getString(ARG_DESC));
        }

        view.findViewById(R.id.ok_button).setOnClickListener(v -> dismiss());

        return view;
    }
}