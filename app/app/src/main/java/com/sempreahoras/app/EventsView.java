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
import android.graphics.Typeface;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static java.lang.Math.floor;
import static java.lang.Math.max;

public class EventsView extends View implements GestureDetector.OnGestureListener {
    private float density = getResources().getDisplayMetrics().density;
    float hourTextSize = 15*density;
    private GestureDetectorCompat gestureDetector;

    private boolean scrolling = false;
    private float scrollY = 0;
    private float maxScrollY;
    private OverScroller scroller = new OverScroller(getContext());
    private AnimScroller animScroller = new AnimScroller();
    private boolean hasFling = false;

    private int hourTextPadding = 10;
    private int hourTextWidth;
    private int hourTextHeight;
    private int hourHeight = Math.round(75 * density);
    private int selectedHour = -1;

    private float headerHeight = 50*density;

    private float width;
    private float columWidth;
    private float height;

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();

    private Handler handler;

    private long firstDayStartMillis;
    private int numberOfDays = 1;
    private List<Event>[] events;
    private List<Event>[] allDayEvents;
    private int allDayMax;
    private final float allDayHeight = 20*density;
    private final float allDayExtra = 5;

    public FloatingActionButton floatingButton;

    DayFragment frag;

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

    void updateColumnWidth() {
        columWidth = (width - hourTextWidth)/numberOfDays;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        updateColumnWidth();
        height = h;

        updateMaxScroll();
        invalidate();
    }

    void updateMaxScroll() {
        maxScrollY = Math.round(24 * hourHeight - height + (numberOfDays > 1 ? headerHeight : 0) + (allDayMax > 0 ? allDayMax * allDayHeight + allDayExtra : 0));
        if(maxScrollY < 0) {
            maxScrollY = 0;
        }
    }

    float getEffectiveScroll() {
        return -scrollY + (numberOfDays > 1 ? headerHeight : 0) + (allDayMax > 0 ? allDayMax * allDayHeight + allDayExtra : 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(ContextCompat.getColor(getContext(), android.R.color.background_light));

        canvas.translate(0, getEffectiveScroll());

        paint.setStrokeWidth(0.5f);
        paint.setColor(Color.GRAY);
        textPaint.setTextSize(hourTextSize);
        for(int hour = 1; hour < 24; ++hour) {
            int y = hourHeight*hour;
            canvas.drawLine(hourTextWidth, y, width, y, paint);
            canvas.drawText(String.format("%02d:00", hour), hourTextPadding, y+hourTextHeight/2, textPaint);
        }

        textPaint.setTextSize(10*density);

        long nowMillis = Calendar.getInstance().getTimeInMillis();
        int dayIdx = 0;
        for(List<Event> eventsForDay : events) {
            long dayStartMillis = firstDayStartMillis + dayIdx*24*60*60*1000;
            for(Event e : eventsForDay) {
                RectF rect = getEventRect(e, dayIdx, dayStartMillis);

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(5);
                canvas.drawRect(rect, paint);

                paint.setStyle(Paint.Style.FILL);
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

            if(firstDayStartMillis + dayIdx*24*60*60*1000 <= nowMillis && firstDayStartMillis + (dayIdx+1)*24*60*60*1000 > nowMillis) {
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(5);

                float left = hourTextWidth + dayIdx*columWidth;
                float y = (float) (nowMillis/1000 % (24*60*60)) / (60*60) * hourHeight - 1;
                canvas.drawLine(left, y, left+columWidth, y, paint);
            }

            dayIdx++;
        }

        if(selectedHour >= 0) {
            paint.setColor(Color.argb(100, 224, 137, 29));
            canvas.drawRect(hourTextWidth, selectedHour*hourHeight, width, (selectedHour+1)*hourHeight, paint);
        }

        canvas.translate(0, -getEffectiveScroll());

        if(numberOfDays > 1) {
            Calendar day = Calendar.getInstance();

            paint.setColor(ContextCompat.getColor(getContext(), android.R.color.background_light));
            canvas.drawRect(0, 0, width, headerHeight, paint);

            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(5);
            canvas.drawLine(0, headerHeight, width, headerHeight, paint);

            for(dayIdx = 0; dayIdx < numberOfDays; ++dayIdx) {
                canvas.save();

                day.setTimeInMillis(firstDayStartMillis + dayIdx*24*60*60*1000 + 12*60*60*1000);

                RectF rect = new RectF(hourTextWidth + dayIdx*columWidth, 0, hourTextWidth + (dayIdx + 1)*columWidth, headerHeight);
                canvas.clipRect(rect);
                canvas.translate(rect.left, 0);

                if(nowMillis >= firstDayStartMillis + dayIdx*24*60*60*1000 && nowMillis < firstDayStartMillis + (dayIdx + 1)*24*60*60*1000) {
                    paint.setColor(Color.rgb(117, 191, 209));
                    canvas.drawRect(0, 0, columWidth, headerHeight, paint);
                }

                Rect textBounds = new Rect();
                textPaint.setColor(Color.BLACK);

                String text = ""+day.get(Calendar.DAY_OF_MONTH);
                textPaint.setTextSize(30*density);
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                canvas.drawText(text, (columWidth-textBounds.width())/2.0f, headerHeight-10, textPaint);
                textPaint.setTypeface(Typeface.DEFAULT);

                text = new SimpleDateFormat("EE").format(day.getTime()).toUpperCase();
                textPaint.setTextSize(12*density);
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                canvas.drawText(text, 10, textBounds.height()+10, textPaint);

                canvas.restore();
            }
        }

        if(allDayMax > 0) {
            canvas.save();
            canvas.translate(hourTextWidth, numberOfDays > 1 ? headerHeight : 0);

            paint.setColor(ContextCompat.getColor(getContext(), android.R.color.background_light));
            canvas.drawRect(-hourTextWidth, 0, width, allDayMax*allDayHeight, paint);

            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(allDayExtra);
            float y = allDayMax*allDayHeight + allDayExtra/2;
            canvas.drawLine(-hourTextWidth, y, width, y, paint);

            for(dayIdx = 0; dayIdx < numberOfDays; ++dayIdx) {
                List<Event> allDayForDay = allDayEvents[dayIdx];
                for(int allDayIdx = 0; allDayIdx < allDayForDay.size(); ++allDayIdx) {
                    Event e = allDayForDay.get(allDayIdx);
                    RectF rect = getAllDayRect(dayIdx, allDayIdx);

                    paint.setStyle(Paint.Style.FILL);
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

            canvas.restore();
        }

        paint.setStrokeWidth(1);
        paint.setColor(Color.GRAY);
        for(int idx = 0; idx < numberOfDays; ++idx) {
            canvas.drawLine(hourTextWidth+columWidth*idx, 0, hourTextWidth+columWidth*idx, height, paint);
        }
    }

    private RectF getEventRect(Event e, int dayIdx, long dayStartMillis) {
        float top, bottom;

        if(e.startMillis < dayStartMillis) {
            top = 0;
        }
        else {
            top = (float) (e.startMillis - dayStartMillis) / (1000*60*60) * hourHeight - 1;
        }

        if(e.endMillis > (dayStartMillis + 24*60*60*1000)) {
            bottom = 24*hourHeight;
        }
        else {
            bottom = (float) (e.endMillis - dayStartMillis) / (1000*60*60) * hourHeight - 1;
        }

        float w = columWidth/e.numColumns;
        float left = hourTextWidth + dayIdx*columWidth + w*e.columnIdx + 4;
        float right = left + w - 8;

        return new RectF(left, top, right, bottom);
    }

    private RectF getAllDayRect(int dayIdx, int allDayIdx) {
        float left  = columWidth* dayIdx;
        float right = columWidth*(dayIdx+1);
        float top    = allDayHeight* allDayIdx;
        float bottom = allDayHeight*(allDayIdx+1);

        return new RectF(left + 2, top + 2, right - 2, bottom - 2);
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

        float x = event.getX();
        float y = event.getY();

        int dayIdx = (int)floor((x-hourTextWidth)/columWidth);
        if(dayIdx >= 0 && dayIdx < numberOfDays) {
            if(numberOfDays > 1 && y < headerHeight) {
                handled = true;
                frag.gotoDay(dayIdx);
            }
            else if(y < allDayMax*allDayHeight + (numberOfDays > 1 ? headerHeight : 0)) {
                y -= (numberOfDays > 1 ? headerHeight : 0);

                for(int allDayIdx = 0; allDayIdx < allDayEvents[dayIdx].size(); ++allDayIdx) {
                    RectF rect = getAllDayRect(dayIdx, allDayIdx);
                    if(rect.contains(x - hourTextWidth, y)) {
                        ((MainActivity)getContext()).viewEvent(allDayEvents[dayIdx].get(allDayIdx));
                        handled = true;
                        break;
                    }
                }
            }
            else {
                for(Event e : events[dayIdx]) {
                    RectF rect = getEventRect(e, dayIdx, firstDayStartMillis + dayIdx*24*60*60*1000);
                    if(rect.contains(x, -getEffectiveScroll() + y)) {
                        ((MainActivity)getContext()).viewEvent(e);
                        handled = true;
                        break;
                    }
                }
            }
        }

        if(!handled && !scrolling) {
            selectedHour = (int)((-getEffectiveScroll() + event.getY()) / hourHeight);
            if(selectedHour >= 24) {
                selectedHour = 23;
             }

            invalidate();
        }

        return handled;
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

    public void setEvents(List<Event>[] eventArray, List<Event>[] allDayEvents, long firstDayStartMillis) {
        assert(eventArray.length == allDayEvents.length);

        this.events = eventArray;
        this.allDayEvents = allDayEvents;
        this.firstDayStartMillis = firstDayStartMillis;

        allDayMax = 0;

        for(int idx = 0; idx < eventArray.length; ++idx) {
            List<Event> eventsForDay = eventArray[idx];
            if(eventsForDay != null && !eventsForDay.isEmpty()) {
                Collections.sort(eventsForDay, Event::compareTo);

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

            allDayMax = max(allDayEvents[idx].size(), allDayMax);
        }

        numberOfDays = eventArray.length;
        updateMaxScroll();
        updateColumnWidth();

        invalidate();
    }
}
