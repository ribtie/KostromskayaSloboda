package com.example.kostromskayasloboda;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class MapView extends View {
    private Bitmap originalMap;
    private Bitmap scaledMap;
    private Bitmap[] objectBitmaps = new Bitmap[9];
    private Bitmap userMarker;

    private final float[] OBJECT_RELATIVE_SIZES = {0.1f, 0.1f, 0.08f, 0.09f, 0.1f, 0.09f, 0.1f, 0.12f, 0.1f};
    private final float MARKER_RELATIVE_SIZE = 0.03f;
    private final float USER_MARKER_RELATIVE_SIZE = 0.04f;

    private final PointF[] OBJECT_POSITIONS = {
            new PointF(57.737786F, 41.010429F),
            new PointF(57.737945F, 41.010621F),
            new PointF(57.738123F, 41.010345F),
            new PointF(57.738456F, 41.010876F),
            new PointF(57.738789F, 41.011210F),
            new PointF(57.739012F, 41.011543F),
            new PointF(57.739345F, 41.011876F),
            new PointF(57.739678F, 41.012210F),
            new PointF(57.740012F, 41.012543F)
    };

    private boolean[] foundObjects = new boolean[9];
    private Matrix transformMatrix = new Matrix();
    private RectF mapBounds = new RectF();
    private Paint markerPaint;
    private float userLat = -1f, userLon = -1f;
    private OnObjectClickListener objectClickListener;

    public interface OnObjectClickListener {
        void onObjectClick(int objectIndex);
    }

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        markerPaint = new Paint();
        markerPaint.setColor(Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);

        loadBitmaps();
    }

    private void loadBitmaps() {
        originalMap = BitmapFactory.decodeResource(getResources(), R.drawable.karta);

        int[] resIds = {
                R.drawable.melnitsa, R.drawable.melnitsa2, R.drawable.chasovna,
                R.drawable.domgar, R.drawable.dom, R.drawable.domic,
                R.drawable.domishe, R.drawable.usadba, R.drawable.konusna
        };

        for (int i = 0; i < objectBitmaps.length; i++) {
            objectBitmaps[i] = BitmapFactory.decodeResource(getResources(), resIds[i]);
        }

        userMarker = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateMapTransform();
    }

    private void calculateMapTransform() {
        if (originalMap == null || getWidth() == 0 || getHeight() == 0) return;

        // Растягиваем карту на весь контейнер
        float scaleX = (float)getWidth() / originalMap.getWidth();
        float scaleY = (float)getHeight() / originalMap.getHeight();

        transformMatrix.reset();
        transformMatrix.postScale(scaleX, scaleY);

        // Границы карты теперь совпадают с границами View
        mapBounds.set(0, 0, getWidth(), getHeight());

        scaledMap = Bitmap.createScaledBitmap(
                originalMap,
                getWidth(),
                getHeight(),
                true
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (scaledMap != null) {
            canvas.drawBitmap(scaledMap, 0, 0, null);
        }

        for (int i = 0; i < OBJECT_POSITIONS.length; i++) {
            PointF pos = convertGeoToScreen(OBJECT_POSITIONS[i]);

            if (foundObjects[i]) {
                drawObject(canvas, i, pos);
            } else {
                drawMarker(canvas, pos);
            }
        }

        if (userLat != -1f && userLon != -1f) {
            PointF userPos = convertGeoToScreen(new PointF(userLat, userLon));
            drawUserMarker(canvas, userPos);
        }
    }

    private PointF convertGeoToScreen(PointF geoPoint) {
        float minLon = 41.009f, maxLon = 41.013f;
        float minLat = 57.736f, maxLat = 57.741f;

        float x = mapBounds.left + (geoPoint.y - minLon) / (maxLon - minLon) * mapBounds.width();
        float y = mapBounds.top + (maxLat - geoPoint.x) / (maxLat - minLat) * mapBounds.height();

        return new PointF(x, y);
    }

    private void drawObject(Canvas canvas, int index, PointF position) {
        float width = getWidth() * OBJECT_RELATIVE_SIZES[index];
        float height = width * objectBitmaps[index].getHeight() / objectBitmaps[index].getWidth();

        RectF dstRect = new RectF(
                position.x - width/2,
                position.y - height/2,
                position.x + width/2,
                position.y + height/2
        );

        canvas.drawBitmap(objectBitmaps[index], null, dstRect, null);
    }

    private void drawMarker(Canvas canvas, PointF position) {
        float size = getWidth() * MARKER_RELATIVE_SIZE;
        canvas.drawCircle(position.x, position.y, size/2, markerPaint);
    }

    private void drawUserMarker(Canvas canvas, PointF position) {
        float size = getWidth() * USER_MARKER_RELATIVE_SIZE;

        RectF dstRect = new RectF(
                position.x - size/2,
                position.y - size/2,
                position.x + size/2,
                position.y + size/2
        );

        canvas.drawBitmap(userMarker, null, dstRect, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && objectClickListener != null) {
            PointF touchPoint = new PointF(event.getX(), event.getY());

            for (int i = 0; i < OBJECT_POSITIONS.length; i++) {
                PointF objPos = convertGeoToScreen(OBJECT_POSITIONS[i]);

                if (foundObjects[i]) {
                    float width = getWidth() * OBJECT_RELATIVE_SIZES[i];
                    float height = width * objectBitmaps[i].getHeight() / objectBitmaps[i].getWidth();

                    if (touchPoint.x >= objPos.x - width/2 && touchPoint.x <= objPos.x + width/2 &&
                            touchPoint.y >= objPos.y - height/2 && touchPoint.y <= objPos.y + height/2) {
                        objectClickListener.onObjectClick(i);
                        return true;
                    }
                } else {
                    float markerSize = getWidth() * MARKER_RELATIVE_SIZE;
                    if (Math.abs(touchPoint.x - objPos.x) <= markerSize &&
                            Math.abs(touchPoint.y - objPos.y) <= markerSize) {
                        objectClickListener.onObjectClick(i);
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void updateUserPosition(double lat, double lon) {
        userLat = (float)lat;
        userLon = (float)lon;
        invalidate();
    }

    public void setObjectFound(int index, boolean found) {
        if (index >= 0 && index < foundObjects.length) {
            foundObjects[index] = found;
            invalidate();
        }
    }

    public Bitmap getObjectBitmap(int index) {
        if (index >= 0 && index < objectBitmaps.length) {
            return objectBitmaps[index];
        }
        return null;
    }

    public void setOnObjectClickListener(OnObjectClickListener listener) {
        this.objectClickListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycleBitmaps();
    }

    private void recycleBitmaps() {
        if (originalMap != null) {
            originalMap.recycle();
            originalMap = null;
        }
        if (scaledMap != null) {
            scaledMap.recycle();
            scaledMap = null;
        }
        for (Bitmap bitmap : objectBitmaps) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        if (userMarker != null) {
            userMarker.recycle();
            userMarker = null;
        }
    }
}