package com.example.bonebuddies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Log_Screen extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_screen);
    }
    public void LetsGoBTN(View view) {
        Intent intent = new Intent(Log_Screen.this, SignInUpActivity.class);
        startActivity(intent);
        finish();
    }
}