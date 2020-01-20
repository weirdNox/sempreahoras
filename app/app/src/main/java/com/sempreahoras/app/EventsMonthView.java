package com.sempreahoras.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventsMonthView extends View implements GestureDetector.OnGestureListener {
    private float density = getResources().getDisplayMetrics().density;
    private GestureDetectorCompat gestureDetector;

    private boolean scrolling = false;
    private float scrollY = 0;
    private float maxScrollY;
    private OverScroller scroller = new OverScroller(getContext());
    private AnimScroller animScroller = new AnimScroller();
    private boolean hasFling = false;

    private int dayHeight = Math.round(80 * density);
    private float width;
    private float columWidth;
    private float height;

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();

    private Handler handler;

    int numberOfDays = 30;
    int numberOfWeeks;
    int firstDayOfWeek = 1;
    int monthFirstWeekDay = 1;
    private boolean[] hasEvents;
    String[] weekStr = {
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };

    public FloatingActionButton floatingButton;

    MonthFragment frag;

    public EventsMonthView(Context context) {
        super(context);
        init();
    }

    public EventsMonthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EventsMonthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        gestureDetector.setIsLongpressEnabled(false);

        scroller.setFriction(0.1f);

        paint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(handler == null) {
            handler = getHandler();
        }
    }

    void updateColumnWidth() {
        columWidth = (width)/7;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        dayHeight = h/6;

        updateColumnWidth();
        updateMaxScroll();
        invalidate();
    }

    void updateMaxScroll() {
        maxScrollY = Math.round(numberOfWeeks*dayHeight - height);
        if(maxScrollY < 0) {
            maxScrollY = 0;
        }

        if(scrollY > maxScrollY) {
            scrollY = maxScrollY;
        }
    }

    /**
     * @return amount that should be translated when rendering
     */
    float getEffectiveScroll() {
        return -scrollY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(ContextCompat.getColor(getContext(), android.R.color.background_light));

        canvas.translate(0, getEffectiveScroll());

        if(hasEvents != null) {
            int weekIdx = 0;

            int weekDay = monthFirstWeekDay;
            int weekDayIdxRelative = weekDay - firstDayOfWeek;
            if(weekDayIdxRelative < 0) {
                weekDayIdxRelative += 7;
            }

            for(int idx = 0; idx < numberOfDays; ++idx) {
                RectF rect = getDayRect(weekIdx, weekDayIdxRelative);

                if(hasEvents[idx]) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.rgb(252, 186, 3));
                    canvas.drawRect(rect, paint);
                }

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1f);
                canvas.drawRect(rect, paint);

                String text = ""+(idx+1);
                textPaint.setTextSize(30*density);
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);
                Rect textBounds = new Rect();
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                canvas.drawText(text, rect.right-textBounds.width()-5*density, rect.bottom-10, textPaint);

                text = weekStr[weekDay-1];
                textPaint.setTextSize(12*density);
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                canvas.drawText(text, rect.left + 5*density, rect.top + textBounds.height() + 5*density, textPaint);

                ++weekDay;
                if(weekDay > 7) {
                    weekDay -= 7;
                }

                ++weekDayIdxRelative;
                if(weekDayIdxRelative >= 7) {
                    weekDayIdxRelative -= 7;
                    weekIdx++;
                }
            }
        }
    }

    /**
     * Create day rectangle
     * @param weekIdx which week? starting from 0
     * @param weekDay which day of the week? from 0 to 6
     * @return the created rectangle
     */
    RectF getDayRect(int weekIdx, int weekDay) {
        float top = weekIdx*dayHeight;
        float left = weekDay*columWidth;
        return new RectF(left, top, left+columWidth, top+dayHeight);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        hasFling = false;
        handler.removeCallbacks(animScroller);

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        float y = event.getY();
        int weekIdx = (int)(y / dayHeight);
        if(weekIdx < numberOfWeeks) {
            frag.viewWeek(weekIdx);
            return true;
        }

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrolling = true;

        scrollY += Math.round(distanceY);
        if(scrollY < 0) {
            scrollY = 0;
        }
        else if(scrollY > maxScrollY) {
            scrollY = maxScrollY;
        }

        if(distanceY > 0) {
            floatingButton.hide();
        }
        else {
            floatingButton.show();
        }

        invalidate();

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(Math.abs(velocityX) > Math.abs(2*velocityY)) {
            frag.changeTo(-velocityX);
        }
        else {
            hasFling = true;
            scroller.fling(0, Math.round(scrollY), 0, -Math.round(velocityY), 0, 0,0, Math.round(maxScrollY), 0, 0);
            handler.post(animScroller);
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);

        if(event.getAction() == MotionEvent.ACTION_UP && !hasFling) {
            scrolling = false;
        }

        return result;
    }

    private class AnimScroller implements Runnable {
        @Override
        public void run() {
            scrolling = scrolling && scroller.computeScrollOffset();
            if(!scrolling) {
                return;
            }

            scrollY = scroller.getCurrY();
            invalidate();

            handler.post(this);
        }
    }

    /**
     * Update event data
     * @param hasEvents list of booleans (one per day) that should be true when there is at least
     *                  one event in that day
     * @param firstDayStartMillis millis (since epoch) of the start of the first day of the month
     */
    public void setEvents(boolean[] hasEvents, long firstDayStartMillis) {
        this.hasEvents = hasEvents;

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(firstDayStartMillis);
        numberOfWeeks = c.getActualMaximum(Calendar.WEEK_OF_MONTH);
        monthFirstWeekDay = c.get(Calendar.DAY_OF_WEEK);
        firstDayOfWeek = (int)c.getFirstDayOfWeek();

        numberOfDays = hasEvents.length;

        updateMaxScroll();
        updateColumnWidth();

        invalidate();
    }
}
