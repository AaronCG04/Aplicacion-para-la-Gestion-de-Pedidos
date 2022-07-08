package com.acgapp.qr_proyect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Inicio extends AppCompatActivity {
    Button c,a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        c=findViewById(R.id.soy_cliente);
        a=findViewById(R.id.soy_administrador);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ad=new Intent(Inicio.this,Admin.class);
                startActivity(ad);
            }
        });
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cl=new Intent(Inicio.this,MainActivity.class);
                startActivity(cl);
            }
        });
    }
}