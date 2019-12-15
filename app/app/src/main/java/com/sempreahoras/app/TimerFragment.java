package com.sempreahoras.app;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

public class TimerFragment extends Fragment implements UpdatableUi {
    private MainActivity a;
    private View v;

    private Button time;
    private Button startStop;
    private Button reset;

    private int timeInMillis = 0;
    private int remainingMillis;

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
        v = inflater.inflate(R.layout.fragment_timer_stopwatch, container, false);

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

    private void startTimer() {
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

    private void cancelTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void updateUi() {
        if(running == 0) {
            time.setText(String.format("%02d:%02d:%04.1f", timeInMillis/(60*60*1000), (timeInMillis/(60*1000)) % 60, ((timeInMillis/(100)) % 600) / 10.0f));
            startStop.setText("Start");
            reset.setVisibility(View.GONE);
        }
        else {
            time.setText(String.format("%02d:%02d:%04.1f", remainingMillis/(60*60*1000), (remainingMillis/(60*1000)) % 60, ((remainingMillis/(100)) % 600) / 10.0f));
        }
    }
}
