package com.example.kostromskayasloboda;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class MapView extends View {

    private Bitmap mapBitmap;
    private float userX = -1;
    private float userY = -1;
    private Paint userPaint;
    private Bitmap userMarker;
    private float animatedX = -1;
    private float animatedY = -1;

    private static final int PUZZLE_ROWS = 5;
    private static final int PUZZLE_COLS = 10;
    private Bitmap[] puzzlePieces = new Bitmap[PUZZLE_ROWS * PUZZLE_COLS];

    private static Boolean[] booleanPiecesShow = new Boolean[PUZZLE_ROWS * PUZZLE_COLS];

    private float[][] coordinates = {{57.774723F, 40.889324F},{57.775179F, 40.893296F}}; /// массив координат точек, чтобы он сам определял области где ставить пропуски

    private int[] missingPieceIndex = new int[coordinates.length];;

    public static Boolean showMissingPiece = false; // флаг, появился ли пропущенный кусок
    private int pieceWidth, pieceHeight;

    TextView count;

    ProgressBar progressBar;

    String currentCount;

    public void makeMiss(float[][] coordinates, int[] missingPieceIndex) {
        for (int i = 0; i < coordinates.length; i++) {
            float[] xy = gpsToPixel(coordinates[i][0], coordinates[i][1]);
            float newX = xy[0];
            float newY = xy[1];
            int index = getUserPuzzleIndex(newX, newY);
            Log.e("Макароны", "Invalid puzzle index for coordinates: " + Arrays.toString(coordinates[i]) + " -> index: " + index + " длина " + coordinates.length);

            if (index < 0 || index >= PUZZLE_ROWS * PUZZLE_COLS) {
                Log.e("MapView", "Invalid puzzle index for coordinates: " + Arrays.toString(coordinates[i]) + " -> index: " + index);
                missingPieceIndex[i] = -1;
            } else {
                missingPieceIndex[i] = index;
            }
        }
    }



    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
        userMarker = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker);
        pieceWidth = mapBitmap.getWidth() / PUZZLE_COLS;
        pieceHeight = mapBitmap.getHeight() / PUZZLE_ROWS;

        for(int i=0; i<booleanPiecesShow.length; i++) {
            booleanPiecesShow[i]=true;
        }
        makeMiss(coordinates,missingPieceIndex);
        for(int i=0; i<missingPieceIndex.length; i++) {
            booleanPiecesShow[missingPieceIndex[i]] = false; /// заполняем фолс там где пропуски
        }


        splitMapIntoPieces();
    }

    public void initUI(TextView count, ProgressBar progressBar) {

        this.count = count;
        this.progressBar = progressBar;
        currentCount = count.getText().toString();
        if (progressBar != null) {
            progressBar.setMax(missingPieceIndex.length);
        }

        if (count != null) {
            String currentCount = count.getText().toString();
            if (!currentCount.isEmpty()) {
                String newText = currentCount.substring(0, currentCount.length() - 1) + missingPieceIndex.length;
                count.setText(newText);
            }
        }
    }
    private void changeCurrentCount() {
        if (count != null) {
            currentCount = count.getText().toString();
            if (!currentCount.isEmpty()) {
                int currentCountInt = countNullAndNotNullInMassive(missingPieceIndex)[0];
                String newText = currentCountInt + currentCount.substring(1, currentCount.length());
                count.setText(newText);
                progressBar.setProgress(currentCountInt, true);
            }
        }

    }
    private int[] countNullAndNotNullInMassive(int[] array) {
        int curCountNotNull=0;
        int curCountNull=0;
        for(int i=0; i<array.length; i++) {
            if (array[i] != -1) curCountNotNull += 1;
            else curCountNull+=1;
        }
        return new int[]{curCountNull,curCountNotNull};
    }
    private boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) return true;
        }
        return false;
    }

    public static void deleteContains(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                array[i] = -1;
            }
        }
    }
    private void splitMapIntoPieces() {
        pieceWidth = mapBitmap.getWidth() / PUZZLE_COLS;
        pieceHeight = mapBitmap.getHeight() / PUZZLE_ROWS;


        for (int row = 0; row < PUZZLE_ROWS; row++) {
            for (int col = 0; col < PUZZLE_COLS; col++) {
                int index = row * PUZZLE_COLS + col;
                if (contains(missingPieceIndex, index) && !booleanPiecesShow[index]) {
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


    }
    public int getUserPuzzleIndex(float X, float Y) { /// вычисляем в какой клетке по координатам мира
        if (X < 0 || Y < 0) return -1;

        int col = (int)(X / pieceWidth);
        int row = (int)(Y / pieceHeight);

        if (col < 0 || col >= PUZZLE_COLS || row < 0 || row >= PUZZLE_ROWS) {
            return -1;
        }

        return row * PUZZLE_COLS + col;
    }



    private void checkInPosition(float userX, float userY) {

        int index = getUserPuzzleIndex(userX, userY); // если придумать как сделать универсальным, наебка в том что в тесте разные ответы нужны
        if (contains(missingPieceIndex, index)) {
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.774723, 40.889324);
                float newX = schoolXy[0];
                float newY = schoolXy[1];
                if (Math.abs(newX - userX) <= 100 && Math.abs(newY - userY) <= 100) {
                    // перерисовать с новым кусочком
                    TestBox.onCorrectAnswerGlobal = () -> {
                        MapView.booleanPiecesShow[index] = true;
                        MapView.deleteContains(missingPieceIndex,index);
                        splitMapIntoPieces();
                        invalidate();
                        changeCurrentCount();
                        Toast.makeText(activity, "Молодец! Ты открыл новый кусочек карты ✅", Toast.LENGTH_SHORT).show();
                    };
                    TestBox testBox = new TestBox(
                            R.drawable.chasovna, "Скасская церковь", /// верный ответ
                            R.drawable.domgar, "Дом Лохиной", /// неверные ответы V
                            R.drawable.dom, "Кузница"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");

                }
            }
        }
        changeCurrentCount();
    }

    private float[] gpsToPixel(double lat, double lon) {
        // границы карты узнать!!!! ,
        double latTop = 57.776203;
        double lonLeft =   40.888835;
        double latBottom = 57.774043;
        double lonRight = 40.894864;

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
            if (contains(missingPieceIndex, i) && !booleanPiecesShow[i]) {
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
            canvas.drawBitmap(userMarker, drawX, drawY, null);
        }
    }

}



