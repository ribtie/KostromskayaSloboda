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

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map);
        userPaint = new Paint();
        userPaint.setColor(Color.BLUE);
        userPaint.setStyle(Paint.Style.FILL);
    }


    public void updateUserPosition(double lat, double lon) {
        float[] xy = gpsToPixel(lat, lon);
        userX = xy[0];
        userY = xy[1];
        invalidate();
        Log.d("MAP", "Pixel: x=" + userX + ", y=" + userY);
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
            float scaleX = (float) getWidth() / mapBitmap.getWidth();
            float scaleY = (float) getHeight() / mapBitmap.getHeight();

            canvas.drawCircle(userX * scaleX, userY * scaleY, 20, userPaint);

        }
    }


}
