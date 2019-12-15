package com.sempreahoras.app;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class TimerFragment extends Fragment implements UpdatableUi {
    private MainActivity a;
    private View v;

    Button time;
    Button startStop;
    Button reset;

    int timeInMillis = 0;
    int remainingMillis;

    // NOTE: 0 - before start, 1 - running, 2 - paused
    private int running = 0;

    private CountDownTimer timer;

    public TimerFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_timer, container, false);

        a.findViewById(R.id.date_button).setVisibility(View.GONE);
        a.b.hide();

        time = v.findViewById(R.id.time);
        time.setOnClickListener(v -> {
            if(running == 0) {
                MyTimePickerDialog picker = new MyTimePickerDialog(a, (view, h, m, s) -> {
                    timeInMillis = 1000*(s + 60*(m + 60*h));
                    updateUi();
                }, timeInMillis/(60*60*1000), (timeInMillis/(60*1000)) % 60, (timeInMillis/(1000)) % 60, true);
                picker.show();
            }
        });

        startStop = v.findViewById(R.id.startStop);
        startStop.setOnClickListener(v -> {
            switch(running) {
                case 0: {
                    running = 1;
                    if(timeInMillis == 0) {
                        timeInMillis = 20*60*1000;
                    }
                    remainingMillis = timeInMillis;
                    startTimer();

                    startStop.setText("Pause");
                    reset.setVisibility(View.VISIBLE);
                } break;

                case 1: {
                    running = 2;
                    cancelTimer();

                    startStop.setText("Continue");
                } break;

                case 2: {
                    running = 1;
                    startTimer();

                    startStop.setText("Pause");
                } break;
            }

            updateUi();
        });

        reset = v.findViewById(R.id.reset);
        reset.setOnClickListener(v -> {
            running = 0;
            cancelTimer();
            updateUi();
        });

        updateUi();

        return v;
    }

    void startTimer() {
        timer = new CountDownTimer(remainingMillis, 100) {
            @Override
            public void onTick(long remaining) {
                remainingMillis = (int)remaining;
                updateUi();
            }

            @Override
            public void onFinish() {
                running = 0;
                updateUi();
            }
        }.start();
    }

    void cancelTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void updateUi() {
        if(running == 0) {
            time.setText(String.format("%02d:%02d:%02d", timeInMillis/(60*60*1000), (timeInMillis/(60*1000)) % 60, (timeInMillis/(1000)) % 60));
            startStop.setText("Start");
            reset.setVisibility(View.GONE);
        }
        else {
            time.setText(String.format("%02d:%02d:%04.1f", remainingMillis/(60*60*1000), (remainingMillis/(60*1000)) % 60, ((remainingMillis/(100)) % 600) / 10.0f));
        }
    }
}
