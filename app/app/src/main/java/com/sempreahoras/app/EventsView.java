package com.sempreahoras.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class EventsView extends View implements GestureDetector.OnGestureListener {
    private float density = getResources().getDisplayMetrics().density;
    float hourTextSize = 15*density;

    private float width;
    private float height;
    private GestureDetectorCompat gestureDetector;

    private boolean scrolling = false;
    private int scrollY = 0;
    private int maxScrollY;
    private OverScroller scroller = new OverScroller(getContext());
    private AnimScroller animScroller = new AnimScroller();
    private boolean hasFling = false;

    private int hourTextPadding = 10;
    private int hourTextWidth;
    private int hourTextHeight;
    private int hourHeight = Math.round(75 * density);
    private int selectedHour = -1;

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();

    private Handler handler;

    private long firstDayStartMillis;
    private List<Event>[] events;

    public FloatingActionButton floatingButton;

    public EventsView(Context context) {
        super(context);
        init();
    }

    public EventsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EventsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        gestureDetector.setIsLongpressEnabled(false);

        scroller.setFriction(0.1f);

        paint.setAntiAlias(true);

        textPaint.setAntiAlias(true);
        textPaint.setTextSize(hourTextSize);

        Rect hourBounds = new Rect();
        textPaint.getTextBounds("00:00", 0, 5, hourBounds);
        hourTextWidth = (int) Math.ceil(hourBounds.width()) + 2*hourTextPadding;
        hourTextHeight = Math.round(hourBounds.height());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(handler == null) {
            handler = getHandler();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        maxScrollY = Math.round(24 * hourHeight - height);
        if(maxScrollY < 0) {
            maxScrollY = 0;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GRAY);
        canvas.drawLine(hourTextWidth, 0, hourTextWidth, height, paint);
        canvas.translate(0, -scrollY);

        textPaint.setTextSize(hourTextSize);
        for(int hour = 1; hour < 24; ++hour) {
            int y = hourHeight*hour;
            canvas.drawLine(hourTextWidth, y, width, y, paint);
            canvas.drawText(String.format("%02d:00", hour), hourTextPadding, y+hourTextHeight/2, textPaint);
        }

        textPaint.setTextSize(10*density);
        if(events != null) {
            // TODO multiday
            for(Event e : events[0]) {
                RectF rect = getEventRect(e, firstDayStartMillis);

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(5);
                canvas.drawRect(rect, paint);

                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                paint.setColor(e.color);
                canvas.drawRect(rect, paint);

                if(rect.width() >= 22.5*density) {
                    canvas.save();

                    rect.top    += 5;
                    rect.bottom -= 5;
                    rect.left   += 5;
                    rect.right  -= 5;
                    canvas.clipRect(rect);

                    canvas.translate(rect.left, rect.top);

                    StaticLayout textLayout = new StaticLayout(e.title, textPaint, Math.round(rect.width()), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
                    textLayout.draw(canvas);

                    canvas.restore();
                }
            }
        }

        if(MainActivity.getDayNumber(date) == MainActivity.getDayNumber(Calendar.getInstance())) {
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(5);
            Calendar now = Calendar.getInstance();
            int nowTime = now.get(Calendar.HOUR_OF_DAY)*60*60 + now.get(Calendar.MINUTE)*60 + now.get(Calendar.SECOND);
            float y = (float)nowTime /  (24*60*60) * 24*hourHeight - 1;
            canvas.drawLine(0, y, width, y, paint);
            paint.setStrokeWidth(1);
        }

        if(selectedHour >= 0) {
            paint.setColor(Color.argb(100, 224, 137, 29));
            canvas.drawRect(hourTextWidth, selectedHour*hourHeight, width, (selectedHour+1)*hourHeight, paint);
        }
    }

    private RectF getEventRect(Event e, long dayStartMillis) {
        float top, bottom;

        if(e.startMillis < dayStartMillis) {
            top = 0;
        }
        else {
            top = (float) (e.startMillis/1000 % (24*60*60)) / (60*60) * hourHeight - 1;
        }

        if(e.endMillis > (dayStartMillis + 24*60*60*1000)) {
            bottom = 24*hourHeight;
        }
        else {
            bottom = (float) (e.endMillis/1000 % (24*60*60)) / (60*60) * hourHeight - 1;
        }

        float availWidth = width-hourTextWidth;
        float w = availWidth/e.numColumns;
        float left = hourTextWidth + w*e.columnIdx + 4;
        float right = left + w - 8;

        return new RectF(left, top, right, bottom);
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
        boolean handled = false;

        // TODO Multiday
        for(Event e : events[0]) {
            RectF rect = getEventRect(e, firstDayStartMillis);
            if(rect.contains(event.getX(), scrollY + event.getY())) {
                // TODO
                //((DayActivity)getContext()).editEvent(e);
                handled = true;
                break;
            }
        }

        if(!handled && !scrolling) {
            selectedHour = (int)((scrollY + event.getY()) / hourHeight);
            if(selectedHour >= 24) {
                selectedHour = 23;
             }

            invalidate();
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrolling = true;
        selectedHour = -1;

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
        hasFling = true;
        scroller.fling(0, scrollY, 0, -Math.round(velocityY), 0, 0,0, maxScrollY, 0, 0);
        handler.post(animScroller);

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

    public void setEvents(List<Event> eventArray[], long firstDayStartMillis) {
        this.events = eventArray;
        this.firstDayStartMillis = firstDayStartMillis;

        for(List<Event> eventsForDay : eventArray) {
            if(eventsForDay != null && !eventsForDay.isEmpty()) {
                int max = 1;
                ArrayList<Event> group = new ArrayList<>();
                ArrayList<Event> active = new ArrayList<>();
                for(Event e : eventsForDay) {
                    if(!group.isEmpty()) {
                        boolean hasIdx = false;
                        ListIterator<Event> iter = active.listIterator();
                        while(iter.hasNext()) {
                            Event test = iter.next();
                            if(e.startMillis >= test.endMillis) {
                                iter.remove();
                                if(!hasIdx || test.columnIdx < e.columnIdx) {
                                    e.columnIdx = test.columnIdx;
                                    hasIdx = true;
                                }
                            }
                        }

                        if(active.isEmpty()) {
                            for(Event ev : group) {
                                ev.numColumns = max;
                            }

                            group.clear();
                            max = 1;
                            e.columnIdx = 0;
                        }
                        else if(!hasIdx) {
                            e.columnIdx = max;
                            max++;
                        }
                    }

                    group.add(e);
                    active.add(e);
                }

                for(Event ev : group) {
                    ev.numColumns = max;
                }
            }

            invalidate();
        }
    }

    private Calendar date;
    public void setCal(Calendar cal) {
        date = cal;
    }
}
