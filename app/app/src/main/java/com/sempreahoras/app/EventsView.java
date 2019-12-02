package com.sempreahoras.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import org.w3c.dom.Text;

public class EventsView extends View implements GestureDetector.OnGestureListener {
    private float density = getResources().getDisplayMetrics().density;

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
    private Paint textPaint = new Paint();

    private Handler handler;

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
        textPaint.setTextSize(15*density);

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GRAY);
        canvas.drawLine(hourTextWidth, 0, hourTextWidth, height, paint);
        canvas.translate(0, -scrollY);

        // Grid
        for(int hour = 1; hour < 24; ++hour) {
            int y = hourHeight*hour;
            canvas.drawLine(hourTextWidth, y, getWidth(), y, paint);
            canvas.drawText(String.format("%02d:00", hour), hourTextPadding, y+hourTextHeight/2, textPaint);
        }

        if(selectedHour >= 0) {
            paint.setColor(Color.rgb(224, 137, 29));
            canvas.drawRect(hourTextWidth, selectedHour*hourHeight, width, (selectedHour+1)*hourHeight, paint);
        }
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
    public boolean onSingleTapUp(MotionEvent e) {
        if(!scrolling) {
            selectedHour = (int)((scrollY + e.getY()) / hourHeight);
            if(selectedHour >= 24) {
                selectedHour = 23;
            }
        }

        invalidate();

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
}
