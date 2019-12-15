package com.sempreahoras.app;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class StopwatchFragment extends Fragment implements UpdatableUi {
    private MainActivity a;
    private View v;

    private Button time;
    private Button startStop;
    private Button reset;

    private long timeInMillis = 0;

    // NOTE: 0 - before start, 1 - running, 2 - paused
    private int running = 0;

    private final long timerFinishTime = 1000*60*60*24*365;
    private CountDownTimer timer;

    public StopwatchFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_timer_stopwatch, container, false);

        a.findViewById(R.id.date_button).setVisibility(View.GONE);
        a.b.hide();

        time = v.findViewById(R.id.time);
        time.setClickable(false);

        startStop = v.findViewById(R.id.startStop);
        startStop.setOnClickListener(v -> {
            switch(running) {
                case 0: {
                    running = 1;
                    timeInMillis = 0;
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
        timer = new CountDownTimer(timerFinishTime-timeInMillis, 100) {
            @Override
            public void onTick(long remaining) {
                timeInMillis = timerFinishTime-remaining;
                updateUi();
            }

            @Override
            public void onFinish() {
                // NOTE: Should not happen... Achievement unlocked?
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
            time.setText("00:00:00.0");
            startStop.setText("Start");
            reset.setVisibility(View.GONE);
        }
        else {
            time.setText(String.format("%02d:%02d:%04.1f", timeInMillis/(60*60*1000), (timeInMillis/(60*1000)) % 60, ((timeInMillis/(100)) % 600) / 10.0f));
        }
    }
}
