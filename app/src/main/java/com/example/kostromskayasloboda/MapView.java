package com.example.kostromskayasloboda;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MapView extends View {

    private Bitmap mapBitmap;
    private float userX = -1;
    private float userY = -1;
    private Paint userPaint;

    private float previousX = -1;
    private float previousY = -1;
    private Bitmap userMarker;
    private float animatedX = -1;
    private float animatedY = -1;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
        userMarker = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker);

        userPaint = new Paint();
        userPaint.setColor(Color.BLUE);
        userPaint.setStyle(Paint.Style.FILL);
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
            previousX = userX;
            previousY = userY;
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
                    }
                }
            });


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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap scaledMap = Bitmap.createScaledBitmap(
                mapBitmap,
                getWidth(),
                getHeight(),
                true
        );

        canvas.drawBitmap(scaledMap, 0, 0, null);

        if (userX >= 0 && userY >= 0) {
            float scaleX = (float) getWidth() / (mapBitmap.getWidth());
            float scaleY = (float) getHeight() / (mapBitmap.getHeight());
            float drawX = animatedX * scaleX - userMarker.getWidth() / 2f;
            float drawY = animatedY * scaleY - userMarker.getHeight() / 2f;
            // 1. Вычисляем угол направления (в градусах)
            float dx = animatedX - previousX;
            float dy = animatedY - previousY;
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            // 2. Создаём матрицу поворота и трансляции
            Matrix matrix = new Matrix();
            matrix.postTranslate(-userMarker.getWidth() / 2f, -userMarker.getHeight() / 2f); // Центрируем
            matrix.postRotate(angle); // Поворачиваем
            matrix.postTranslate(drawX, drawY); // Перемещаем в нужную позицию

            // 3. Рисуем повернутый маркер
            canvas.drawBitmap(userMarker, matrix, null);

        }
    }


}
