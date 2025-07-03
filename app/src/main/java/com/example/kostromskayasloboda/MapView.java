package com.example.kostromskayasloboda;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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

    private Bitmap backgroundMapBitmap;
    private static final int PUZZLE_ROWS = 15;
    private static final int PUZZLE_COLS = 10;
    private Bitmap[] puzzlePieces = new Bitmap[PUZZLE_ROWS * PUZZLE_COLS];

    private static Boolean[] booleanPiecesShow = new Boolean[PUZZLE_ROWS * PUZZLE_COLS];

    /// спас чапыг chas loh lip ilya mel kolod
    private float[][] coordinates = {{57.774723F, 40.889324F},{57.775092F, 40.89061F},{57.77479F, 40.891921F},{57.775083F, 40.892619F},{57.775193F, 40.893743F},{57.775908F, 40.891404F},{57.773393F, 40.891761F},{57.776145F, 40.890866F}}; /// массив координат точек, чтобы он сам определял области где ставить пропуски

    private int[] missingPieceIndex = new int[coordinates.length];;
    private int pieceWidth, pieceHeight;
    private static float offsetX = 0;
    private float offsetY = 0;
    private float lastTouchX;
    private float lastTouchY;
    TextView count;
    String currentCount;

    private float scaleFactor = 1;
    private final float minScale = 1;
    private final float maxScale = 1.5f;
    private float userRotation = 0f; // угол поворота в градусах

    public void zoomIn() {
        scaleFactor = Math.min(scaleFactor + 0.2f, maxScale);
        float scaledHeight = getHeight()*scaleFactor;
        float minOffsetY = (getHeight() - scaledHeight)/3f; // максимально вниз
        float maxOffsetY = Math.abs((getHeight() - scaledHeight)/3f);   // максимально вверх
        offsetY = Math.min(Math.max(offsetY, minOffsetY), maxOffsetY);
        invalidate();
    }

    public void zoomOut() {
        scaleFactor = Math.max(scaleFactor - 0.2f, minScale);
        float scaledWidth = getWidth()*scaleFactor;
        float minOffsetX = (getWidth() - scaledWidth)/ 3f; // максимально вправо
        float maxOffsetX = Math.abs((getWidth() - scaledWidth)/ 3f);                        // максимально влево
        offsetX = Math.min(Math.max(offsetX, minOffsetX), maxOffsetX);
        invalidate();

    }

    public void makeMiss(float[][] coordinates, int[] missingPieceIndex) {
        for (int i = 0; i < coordinates.length; i++) {
            float[] xy = gpsToPixel(coordinates[i][0], coordinates[i][1]);
            float newX = xy[0];
            float newY = xy[1];
            int index = getUserPuzzleIndex(newX, newY);

            if (index < 0 || index >= PUZZLE_ROWS * PUZZLE_COLS) {
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
        backgroundMapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.museum_map_podlozhka);

        for(int i=0; i<booleanPiecesShow.length; i++) {
            booleanPiecesShow[i]=true;
        }
        makeMiss(coordinates,missingPieceIndex);
        for(int i=0; i<missingPieceIndex.length; i++) {
            booleanPiecesShow[missingPieceIndex[i]] = false; /// заполняем фолс там где пропуски
        }


        splitMapIntoPieces();
    }

    public void initUI(TextView count) {

        this.count = count;
        currentCount = count.getText().toString();
        if (count != null) {
            String currentCount = count.getText().toString();
            if (!currentCount.isEmpty()) {
                String newText = currentCount.substring(0, currentCount.length() - 1) + missingPieceIndex.length;
                count.setText(newText);
            }
        }
    }

    public void endGame() {
        Intent endGame = new Intent(activity, EndGame.class);
        activity.startActivity(endGame);
    }

    private void changeCurrentCount() {
        if (count != null) {
            currentCount = count.getText().toString();
            if (!currentCount.isEmpty()) {
                int currentCountInt = countNullAndNotNullInMassive(missingPieceIndex)[0];
                String newText = currentCountInt + currentCount.substring(1, currentCount.length());
                count.setText(newText);
            }

            if(countNullAndNotNullInMassive(missingPieceIndex)[1]==0) endGame();
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
            userRotation = (float) Math.toDegrees(Math.atan2(endY - startY, endX - startX)) - 90f;

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
                float[] schoolXy = gpsToPixel(57.774723, 40.889324); //spas
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
                        R.drawable.spas, "Спасская церковь", "В храме сохранились подлинные резные клиросы, лавки и, частично, иконостас, солея.",/// верный ответ
                        R.drawable.loh, "Дом Лоховой", "Памятник перевезен в музей и восстановлен в 1972 г.", /// неверные ответы V
                        R.drawable.kolod, "Говорящий колодец","Говорящий колодец – интерактивная программа в музее",
                        R.drawable.podkazka, "Обрати внимание на резные окна"
                );
                testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            /// lip ilya mel kolod
          //,{},{},{},{},{57.775908F, 40.891404F},{57.776145F, 40.890866F}}; /// массив координат точек, чтобы он сам определял области где ставить пропуски
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.775092F, 40.89061F); //chap
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
                            R.drawable.chap, "Дом Чапыгиной", "Земляные полы, лавки, русская печь, соломенная кровля.  ",/// верный ответ
                            R.drawable.loh, "Дом Лоховой", "Двухрядная поволжская постройка «на два коня»", /// неверные ответы V
                            R.drawable.kolod, "Говорящий колодец","Говорящий колодец – интерактивная программа музея",
                            R.drawable.podkazka, "Обрати внимание на резные окна"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.77479F, 40.891921F); //chas
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
                            R.drawable.chasov, "Часовня Казанской иконы", "Освящена в честь Казанской иконы.  ",/// верный ответ
                            R.drawable.spas, "Спасская церковь", "Древняя деревянная церковь клетского типа.", /// неверные ответы V
                            R.drawable.lipat, "Дом Липатова","Двухэтажный дом-брус с черным двором, баней и резьбой ",
                            R.drawable.podkazka, "Обрати внимание на резные окна"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.775083F, 40.892619F); //лох
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
                            R.drawable.loh, "Дом Лоховой", "Двухрядная поволжская постройка «на два коня»",/// верный ответ
                            R.drawable.spas, "Спасская церковь", "Древняя деревянная церковь клетского типа.", /// неверные ответы V
                            R.drawable.lipat, "Дом Липатова","Двухэтажный дом-брус с черным двором, баней и резьбой ",
                            R.drawable.podkazka, "Обрати внимание на крышу"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.775193F, 40.893743F); //дlip
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
                            R.drawable.lipat, "Дом Липатова","Двухэтажный дом-брус с черным двором, баней и резьбой ",
                            R.drawable.loh, "Дом Лоховой", "Двухрядная поволжская постройка «на два коня»",/// верный ответ
                            R.drawable.kolod, "Говорящий колодец", "Интерактивная программа музея", /// неверные ответы V
                            R.drawable.podkazka, "Обрати внимание на крышу"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.775908F, 40.891404F); //ilya
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
                            R.drawable.ilya, "Ильинская церковь", "Двухъярусный деревянный храм с Покровской церковью",
                            R.drawable.lipat, "Дом Липатова","Двухэтажный дом-брус с черным двором, баней и резьбой ",
                            R.drawable.loh, "Дом Лоховой", "Двухрядная поволжская постройка «на два коня»",/// верный ответ
                   R.drawable.podkazka, "Обрати внимание на крышу"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.773393, 40.891761); //ilya
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
                            R.drawable.mel,"Мельница", "Трехъярусная мельница с вращающейся «шапкой» и крыльями.",
                            R.drawable.ilya, "Ильинская церковь", "Двухъярусный деревянный храм с Покровской церковью",
                            R.drawable.lipat, "Дом Липатова","Двухэтажный дом-брус с черным двором, баней и резьбой ",
                  R.drawable.podkazka, "Обрати внимание на крышу"
                    );
                    testBox.show(activity.getSupportFragmentManager(), "Диалог");
                }
            }
            if (!booleanPiecesShow[index]) { /// дом который ищем, координаты, отдельные методы индекс - смотри значение в массиве
                float[] schoolXy = gpsToPixel(57.776145F, 40.890866F); //kolod
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
                            R.drawable.kolod, "Говорящий колодец", "Интерактивная программа музея",
                            R.drawable.mel,"Мельница", "Трехъярусная мельница с вращающейся «шапкой» и крыльями.",
                            R.drawable.ilya, "Ильинская церковь", "Двухъярусный деревянный храм с Покровской церковью",
                           R.drawable.podkazka, "Обрати внимание на крышу"
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

    private void clampOffsets() {
        float scaledWidth = getWidth() * scaleFactor;
        float scaledHeight = getHeight() * scaleFactor;


        Log.d("СНЮСИНКИ", scaledWidth + " scale " + getWidth() + " width" + mapBitmap.getWidth() + " mapBitmap" + " " + (getWidth() - scaledWidth));

        // Горизонтальное ограничение
        if (scaledWidth <= getWidth()) {
            // Если карта уже меньше ширины экрана — центрируем по горизонтали
            offsetX = (getWidth() - scaledWidth) / 2;
        } else {
            // Иначе ограничиваем смещение, чтобы карта не "улетала" за левый и правый края
            float minOffsetX = (getWidth() - scaledWidth)/ 3f; // максимально вправо
            float maxOffsetX = Math.abs((getWidth() - scaledWidth)/ 3f);                        // максимально влево
            offsetX = Math.min(Math.max(offsetX, minOffsetX), maxOffsetX);
        }

        // Вертикальное ограничение
        if (scaledHeight <= getHeight()) {
            // Центрируем по вертикали, если карта меньше по высоте
            offsetY = (getHeight() - scaledHeight) / 2;
        } else {
            // Ограничиваем смещение по вертикали
            float minOffsetY = (getHeight() - scaledHeight)/3f; // максимально вниз
            float maxOffsetY = Math.abs((getHeight() - scaledHeight)/3f);   // максимально вверх
            offsetY = Math.min(Math.max(offsetY, minOffsetY), maxOffsetY);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;

                offsetX += dx;
                offsetY += dy;

                clampOffsets();  // <<< ВАЖНО: добавляем после движения

                lastTouchX = event.getX();
                lastTouchY = event.getY();

                invalidate();
                break;


            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2f - offsetX, getHeight() / 2f - offsetY);

        float bgScaleX = (float) getWidth() / backgroundMapBitmap.getWidth();
        float bgScaleY = (float) getHeight() / backgroundMapBitmap.getHeight();
        Bitmap scaledBackground = Bitmap.createScaledBitmap(backgroundMapBitmap,
                (int)(backgroundMapBitmap.getWidth() * bgScaleX),
                (int)(backgroundMapBitmap.getHeight() * bgScaleY),
                true);
        canvas.drawBitmap(scaledBackground, 0, 0, null);

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

        if (userX >= 0 && userY >= 0) {
            float drawX = animatedX * scaleX;
            float drawY = animatedY * scaleY;

            canvas.save();
            canvas.translate(drawX, drawY);
            canvas.rotate(userRotation); // вращаем
            canvas.drawBitmap(
                    userMarker,
                    -userMarker.getWidth() / 2f,
                    -userMarker.getHeight() / 2f,
                    null
            );
            canvas.restore();
        }


        canvas.restore();
    }

}



