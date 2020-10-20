package com.example.gpsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button Something;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Something = findViewById(R.id.btnSomething);
        Something.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSomething();

            }
        });
    }
    public void openSomething(){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);

    }
    }

