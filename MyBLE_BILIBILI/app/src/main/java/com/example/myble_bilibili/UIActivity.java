package com.example.myble_bilibili;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class UIActivity extends AppCompatActivity {
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private String message1, message2, message3, message4, message5, message6, message7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uiactivity);
        textView1 = (TextView) findViewById(R.id.text_1);
        textView2 = (TextView) findViewById(R.id.text_2);
        textView3 = (TextView) findViewById(R.id.text_3);
        textView4 = (TextView) findViewById(R.id.text_4);
        textView5 = (TextView) findViewById(R.id.text_5);
        textView6 = (TextView) findViewById(R.id.text_6);
        Intent intent = getIntent();
        message1 = intent.getStringExtra("message1");
        message2 = intent.getStringExtra("message2");
        message3 = intent.getStringExtra("message3");
        message4 = intent.getStringExtra("message4");
        message5 = intent.getStringExtra("message5");
        message6 = intent.getStringExtra("message6");
        message7 = intent.getStringExtra("message7");
        textView1.setText(message1);
        textView2.setText(message2);
        textView3.setText(message3);
        textView4.setText(message4);
        textView5.setText(message5);
        textView6.setText(message6);


    }
}