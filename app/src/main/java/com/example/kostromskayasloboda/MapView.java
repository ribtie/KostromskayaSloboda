package com.example.kostromskayasloboda;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MapView extends View {

    private Bitmap mapBitmap;
    private float userX = -1;
    private float userY = -1;
    private Paint userPaint;
    private Bitmap userMarker;
    private float animatedX = -1;
    private float animatedY = -1;

    private static final int PUZZLE_ROWS = 3;
    private static final int PUZZLE_COLS = 3;
    private Bitmap[] puzzlePieces = new Bitmap[PUZZLE_ROWS * PUZZLE_COLS];
    private int missingPieceIndex = 3; //
    public static Boolean showMissingPiece = false; // флаг, появился ли пропущенный кусок
    private int pieceWidth, pieceHeight;


    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
        userMarker = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker);
        splitMapIntoPieces();

    }
    private void splitMapIntoPieces() {
        pieceWidth = mapBitmap.getWidth() / PUZZLE_COLS;
        pieceHeight = mapBitmap.getHeight() / PUZZLE_ROWS;

        for (int row = 0; row < PUZZLE_ROWS; row++) {
            for (int col = 0; col < PUZZLE_COLS; col++) {
                int index = row * PUZZLE_COLS + col;
                if (index == missingPieceIndex && !showMissingPiece) {
                    puzzlePieces[index] = null; // пустой кусочек
                    continue;
                }
                puzzlePieces[index] = Bitmap.createBitmap(
                        mapBitmap,
                        col * pieceWidth,
                        row * pieceHeight,
                        pieceWidth,
                        pieceHeight
                );
            }
        }
    }

    private AppCompatActivity activity;

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }
    public void updateUserPosition(double lat, double lon) {
            float[] xy = gpsToPixel(lat, lon);
            float newX = xy[0];
            float newY = xy[1];

            // Если это первый запуск — сразу установить
            if (userX == -1 && userY == -1) {
                userX = newX;
                userY = newY;
                animatedX = newX;
                animatedY = newY;
                invalidate();
                return;
            }



            final float startX = userX;
            final float startY = userY;
            final float endX = newX;
            final float endY = newY;
            final long duration = 300;
            final long startTime = System.currentTimeMillis();

            post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = System.currentTimeMillis() - startTime;
                    float t = Math.min(1f, (float) elapsed / duration);

                    // Линейная интерполяция
                    animatedX = startX + (endX - startX) * t;
                    animatedY = startY + (endY - startY) * t;

                    invalidate();

                    if (t < 1f) {
                        postDelayed(this, 16);
                    } else {
                        userX = endX;
                        userY = endY;
                        checkInPosition(userX, userY);
                    }

                }


            });
        Log.d("ТРИППИ ТРОПА",userX + " UPDATE " + userY);

    }

    private void checkInPosition(float userX, float userY) {

        ///if (showMissingPiece) return; // уже показали

        Boolean school = false;
        if(school==false){ /// дом который ищем, координаты, отдельные методы
            float[] schoolXy = gpsToPixel(57.737786, 41.010429);
            float newX = schoolXy[0];
            float newY = schoolXy[1];
            Log.d("ТРИППИ ТРОПА",Math.abs(newX-userX) + " " + Math.abs(newY-userY)+ " " + userX + " " + userY + " " + newX + " " + newY);
            if(Math.abs(newX-userX)<=100 && Math.abs(newY-userY)<=100) {
                 // перерисовать с новым кусочком
                Toast.makeText(this.getContext(), showMissingPiece + " ", Toast.LENGTH_SHORT).show();
                Church1.onCorrectAnswerGlobal = () -> {
                    MapView.showMissingPiece = true;
                    splitMapIntoPieces();
                    invalidate();
                    Toast.makeText(activity, showMissingPiece + " ПОСЛЕ ВЕРНОГО", Toast.LENGTH_SHORT).show();
                };
                Church1 church1 = new Church1();
                church1.show(activity.getSupportFragmentManager(), "Church");

            }
        }
    }

    private float[] gpsToPixel(double lat, double lon) {
        // границы карты узнать!!!! ,
        double latTop = 57.739839;
        double lonLeft =  41.008746;
        double latBottom = 57.736478;
        double lonRight = 41.015191;

        int imgWidth = mapBitmap.getWidth();
        int imgHeight = mapBitmap.getHeight();

        float x = (float) ((lon - lonLeft) / (lonRight - lonLeft) * imgWidth);
        float y = (float) ((latTop - lat) / (latTop - latBottom) * imgHeight);

        return new float[]{x, y};
    }
    private float[] gpsToPixel(double[] latLon) {
        return gpsToPixel(latLon[0], latLon[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scaleX = (float) getWidth() / mapBitmap.getWidth();
        float scaleY = (float) getHeight() / mapBitmap.getHeight();

        // Рисуем кусочки
        for (int i = 0; i < puzzlePieces.length; i++) {
            if (i == missingPieceIndex && !showMissingPiece) {
                // Пропускаем пустой кусочек, если он ещё не показан
                continue;
            }

            Bitmap piece = puzzlePieces[i];
            if (piece != null) {
                int row = i / PUZZLE_COLS;
                int col = i % PUZZLE_COLS;

                float left = col * pieceWidth * scaleX;
                float top = row * pieceHeight * scaleY;

                Bitmap scaledPiece = Bitmap.createScaledBitmap(piece,
                        (int)(pieceWidth * scaleX),
                        (int)(pieceHeight * scaleY),
                        true);

                canvas.drawBitmap(scaledPiece, left, top, null);
            }
        }

        // Рисуем пользователя поверх
        if (userX >= 0 && userY >= 0) {
            float drawX = animatedX * scaleX - userMarker.getWidth() / 2f;
            float drawY = animatedY * scaleY - userMarker.getHeight() / 2f;
            Log.d("ТРИППИ ТРОПА",userX + " onDRAW " + userY);
            canvas.drawBitmap(userMarker, drawX, drawY, null);
        }
    }

}



