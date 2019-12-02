package com.example.chickenrun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button buybtn = (Button)findViewById(R.id.buybtn);


        //사용자가 치킨을 살수 있도록 페이지름 넘김
        buybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this,BuyChicken.class);
                startActivity(intent);//액티비티 띄우기
            }
        });
    }
}
