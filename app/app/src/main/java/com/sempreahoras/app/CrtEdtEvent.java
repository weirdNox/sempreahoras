package com.sempreahoras.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CrtEdtEvent extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crt_edt_event);

        //Save Button

        Button saveButton = (Button)findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // caso user decida guardar, então é necessário enviar objeto para servidor com userid, authid, detalhes evento (usar json), guardar temporariamente detalhes

        //Cancel Button

        Button cancelButton = (Button)findViewById(R.id.cancelbutton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // caso user decida cancelar, não envia nada ao servidor
    }
}
