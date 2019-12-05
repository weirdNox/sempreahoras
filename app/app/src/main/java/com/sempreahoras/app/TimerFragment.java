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

import java.util.Locale;

public class TimerFragment extends Fragment implements UpdatableUi {
    MainActivity a;
    View v;

    TextView counttempo;
    EditText selecttempo;
    EditText selecttempo2;
    Button startbutton;
    Button resetbutton;

    private boolean run;
    private boolean fin = false;

    private CountDownTimer countdown;

    private static final long START_TIME_IN_MILLIS = 10000;
    private long left_time_mili = START_TIME_IN_MILLIS;

    public TimerFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        a = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_timer, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        a.setSupportActionBar(toolbar);
        a.getSupportActionBar().setDisplayShowTitleEnabled(false);
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) a.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(a, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        counttempo   = v.findViewById(R.id.timeview);
        selecttempo  = v.findViewById(R.id.seltime);
        selecttempo2 = v.findViewById(R.id.seltime2);

        startbutton = v.findViewById(R.id.sbuttontime);
        resetbutton = v.findViewById(R.id.rbuttontime);

        startbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fin == false){
                        left_time_mili = readTime();
                        updtCounter();
                        fin = true;
                    }

                    if(run){
                        pauset();
                    }
                    else{
                        startt();
                    }
                }
            });

        resetbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resett();
                }
            });

        updtCounter();

        return v;
    }

    private void startt(){
        countdown = new CountDownTimer(left_time_mili, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    left_time_mili = millisUntilFinished;
                    updtCounter();
                }

                @Override
                public void onFinish() {
                    run = false; //ja n se encontra a correr
                    startbutton.setText("Start");
                    startbutton.setVisibility(View.INVISIBLE); //acabou o tempo desaparece
                    resetbutton.setVisibility(View.VISIBLE); //podemos dar reset dps de ter acabado o tempo
                }
            }.start();

        run = true;
        startbutton.setText("Pause");
        resetbutton.setVisibility(View.INVISIBLE);
    }

    private void pauset(){
        countdown.cancel(); //para o count
        run = false; // ja nao esta a correr, parado
        startbutton.setText("Start"); //altera nome do botao outra vez para start
        resetbutton.setVisibility(View.VISIBLE); //coloca botao reset visivel outra vez, pois timer encontra-se parado

    }

    private void resett(){
        left_time_mili = readTime(); //volta a ter o valor inicial
        updtCounter(); //update na string do counter
        resetbutton.setVisibility(View.INVISIBLE); //nao aparece pois acabamos de fazer reset
        startbutton.setVisibility(View.VISIBLE);
    }

    private void updtCounter(){
        int minutos = (int) (left_time_mili/1000)/60;
        int segundos = (int) (left_time_mili/1000)%60;

        String t_left = String.format(Locale.getDefault(),"%02d:%02d", minutos,segundos);

        counttempo.setText(t_left);
    }

    private long readTime(){
        if(selecttempo.getText().toString().equals("00") && selecttempo2.getText().toString().equals("00")){
            return START_TIME_IN_MILLIS;
        }
        int minutos = Integer.parseInt(selecttempo.getText().toString());
        int segundos = Integer.parseInt(selecttempo2.getText().toString());
        long resultado = minutos*1000*60 + segundos*1000;
        return resultado;
    }

    @Override
    public void updateUi() {}
}
