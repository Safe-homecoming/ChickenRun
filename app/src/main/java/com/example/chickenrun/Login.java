package com.example.chickenrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class Login extends AppCompatActivity {


    private Button loginbtn;
    private EditText textId, textPw;
    private String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginbtn = findViewById(R.id.login_button);  //로그인 버튼
        textId = findViewById(R.id.login_id);// id 입력
        textPw = findViewById(R.id.login_password);// pw 입력

        // 로그인 버튼  안심인지 시민인지 자동으로 여기서 구분
        loginbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                HashMap<String, Object> input = new HashMap<>();
                input.put("id", textId.getText().toString());
                input.put("password", textPw.getText().toString());
                input.put("title", "this is title");
                input.put("body", "this is body");

                // 로그인 버튼 클릭후
                ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                Call<Resultm> call = apiInterface.logincheck(input);

                call.enqueue(new Callback<Resultm>()
                {
                    @Override
                    public void onResponse(@NonNull Call<Resultm> call, Response<Resultm> response)
                    {
                        //정상 결과
                        Resultm result = response.body();
                        String memname = response.body().getName();

                        Log.e(TAG, "onResponse: result: " + result.getResult() );
                        Log.e(TAG, "onResponse: memname: " + memname );

                        if (response.body() != null)
                        {
                            if (result.getResult().equals("ok"))
                            {
                                Intent intent = new Intent(Login.this,MainMenu.class);
                                startActivity(intent);//액티비티 띄우기
                                Toast.makeText(Login.this, "로그인 완료 ", Toast.LENGTH_SHORT).show();
                                //회원 기본정보 쉐어드에 저장하기
                                SharedPreferences pref = getSharedPreferences("chmeminfo", MODE_PRIVATE);

                                // SharedPreferences 의 데이터를 저장/편집 하기위해 Editor 변수를 선언한다.
                                SharedPreferences.Editor editor = pref.edit();

                                // key값에 value값을 저장한다.
                                // String, boolean, int, float, long 값 모두 저장가능하다.
                                editor.putString("memId", response.body().getMemId());
                                editor.putString("name", memname);
                                editor.putInt("qty", response.body().getChicken_quantity());
                                // 메모리에 있는 데이터를 저장장치에 저장한다.
                                editor.commit();

                            }
                            else
                            {
                                Toast.makeText(Login.this, "로그인이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<Resultm> call, Throwable t)
                    {
                        //네트워크 문제
                        Toast.makeText(Login.this, "네트워크 실패", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: t: " + t.getMessage() );
                    }
                });
            }
        });
    }
}
