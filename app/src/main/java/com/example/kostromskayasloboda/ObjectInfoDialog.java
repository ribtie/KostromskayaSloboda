package com.example.kostromskayasloboda;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private static final String ARG_IMAGE = "image_name";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "description";

    public static ObjectInfoDialog newInstance(String imageName, String title, String description) {
        ObjectInfoDialog fragment = new ObjectInfoDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, imageName);
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

        if (getArguments() != null) {
            String imageName = getArguments().getString(ARG_IMAGE);
            int resId = getResources().getIdentifier(imageName, "drawable",
                    requireContext().getPackageName());

            if (resId != 0) {
                imageView.setImageResource(resId);
            }

            titleView.setText(getArguments().getString(ARG_TITLE));
            descView.setText(getArguments().getString(ARG_DESC));
        }

        view.findViewById(R.id.ok_button).setOnClickListener(v -> dismiss());

        return view;
    }
}