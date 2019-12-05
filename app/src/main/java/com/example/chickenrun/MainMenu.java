package com.example.chickenrun;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chickenrun.Lobby.Activity_Lobby;

public class MainMenu extends AppCompatActivity {


    String username, userid; //쉐어드 프리퍼런스

    private TextView Idtext,chtotalcnt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        chtotalcnt = (TextView)findViewById(R.id.totalchicken);//치킨갯수
        Idtext = (TextView)findViewById(R.id.idText);// 사용자 id
        Button startbtn = (Button)findViewById(R.id.startbtn);//  게임 시작 버튼
        Button buybtn = (Button)findViewById(R.id.buybtn);
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        //test 소스
        ImageView testbtn = (ImageView)findViewById(R.id.testbtn);


        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, GameStart.class);
                startActivity(intent);
                finish();
            }
        });
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        SharedPreferences pref = getSharedPreferences("chmeminfo", MODE_PRIVATE);

        // key에 해당한 value를 불러온다.
        // 두번째 매개변수는 , key에 해당하는 value값이 없을 때에는 이 값으로 대체한다.
        userid = pref.getString("memId", "");
        username = pref.getString("name", "");


        //쉐어드에 저장된 유저 name 가져오기
        Idtext.setText(username.trim());

        //사용자 치킨 수량 가져오기
        finishsuccess(userid);
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

    // 안심이 귀가 완료 버튼
    private  void finishsuccess(String userids){


        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<Resultm> call = apiInterface.memvbercnt(userids.trim());



        call.enqueue(new Callback<Resultm>() {
            @Override
            public void onResponse(Call<Resultm> call, Response<Resultm> response) {

                //정상 결과
                Resultm result = response.body();
                String resultd = response.body().getResult();
                String cnt = String.valueOf(response.body().getChicken_quantity());


                if (response.body() != null) {
                    // if (result.getResult().equals("ok")) {

                    Log.i("TESTLOG.....","     "+cnt);
                    if (resultd.equals("ok")) {
                        Log.i("MainMenu-치킨수량", "킨수량  "+response.body().getChicken_quantity());
                        chtotalcnt.setText(cnt);

                    } else if (resultd.equals("error")) {
                        //실패
                        Toast.makeText(getApplicationContext(), "불러오기 실패하였습니다. 확인 부탁드립니다.", Toast.LENGTH_SHORT).show();

                    }

                }
            }

            @Override
            public void onFailure(Call<Resultm> call, Throwable t) {
                //네트워크 문제
                Toast.makeText(getApplicationContext(), "서비스 연결이 원활하지 않습니다", Toast.LENGTH_SHORT).show();
                Log.e(" 에러 발생 Log ", t.getMessage());
            }
        });
    }

}
