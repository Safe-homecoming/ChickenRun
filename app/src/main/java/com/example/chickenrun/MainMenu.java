package com.example.chickenrun;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chickenrun.Lobby.Activity_Lobby;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final TextView chtotalcnt = (TextView)findViewById(R.id.totalchicken);//치킨갯수
        Button startbtn = (Button)findViewById(R.id.startbtn);//  게임 시작 버튼
        Button buybtn = (Button)findViewById(R.id.buybtn);


        //게임시작 버튼
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //구매한 치킨이 없을때 다이얼로그를 띄워줌
                if( chtotalcnt.getText().equals("0")){
                    new AlertDialog.Builder(MainMenu.this)
                            //.setTitle("알람 팝업")
                            .setMessage("치킨을 구매 해주세요.")
                            .setNeutralButton("닫기", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dlg, int sumthin) {

                                }

                            })

                            .show(); // 팝업창 보여줌

                }

                else
                {
                    Intent intent = new Intent(MainMenu.this, Activity_Lobby.class);
                    startActivity(intent);
                }

            }
        });


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
