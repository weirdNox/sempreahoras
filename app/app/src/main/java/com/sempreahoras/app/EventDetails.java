package com.sempreahoras.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //para preencher campos de detalhes, devemos receber informações do servidor a cerca dos eventos

        //Edit Button

        Button editButton = (Button)findViewById(R.id.editbutton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(getApplicationContext(),CrtEdtEvent.class);
                // passar informação
                startActivity(editIntent);
            }
        });
        // caso user decida editar, vamos para criar/editar evento(classe)

        //Delete Button

        Button deleteButton = (Button)findViewById(R.id.deletebutton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deleteIntent = new Intent(getApplicationContext(),CrtEdtEvent.class);
                // passar informação
                startActivity(deleteIntent);
            }
        });
        // caso user decida apagar, temos de informar servidor para remover evento da database
    }
}
