package com.example.chickenrun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView chtotalcnt = (TextView)findViewById(R.id.totalchicken);//치킨갯수
        Button startbtn = (Button)findViewById(R.id.startbtn);//  게임 시작 버튼

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( chtotalcnt.getText().equals("0")){

                }

            }
        });
    }
}
