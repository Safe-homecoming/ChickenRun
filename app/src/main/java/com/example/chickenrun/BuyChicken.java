package com.example.chickenrun;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.enums.Method;
import kr.co.bootpay.enums.UX;
import kr.co.bootpay.listener.CancelListener;
import kr.co.bootpay.listener.CloseListener;
import kr.co.bootpay.listener.ConfirmListener;
import kr.co.bootpay.listener.DoneListener;
import kr.co.bootpay.listener.ErrorListener;
import kr.co.bootpay.listener.ReadyListener;
import kr.co.bootpay.model.BootExtra;
import kr.co.bootpay.model.BootUser;

public class BuyChicken extends AppCompatActivity
{
    String TAG = "BuyChicken";

    int mchichenqty  =1;
    //금액 콤마 찍기
    DecimalFormat myFormatter = new DecimalFormat("###,###");

    private int stuck = 10;
    // Application ID: 5de4c14402f57e0022423501

    boolean isPay = false;

    String memId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_chicken);

        SharedPreferences sf = getSharedPreferences("chmeminfo", MODE_PRIVATE);
        memId = sf.getString("memId", null);

        Log.e(TAG, "onCreate: memId: " + memId );

        Button plusbtn = (Button) findViewById(R.id.plusbtn);
        Button minusbtn = (Button) findViewById(R.id.minusbtn);
        final EditText ChQty = (EditText) findViewById(R.id.ChQty);
        final TextView pricetotla = (TextView) findViewById(R.id.totalprice);
        Button chicken_payment = (Button) findViewById(R.id.chicken_payment);

        ChQty.setCursorVisible(false); // setInputType(EditorInfo.TYPE_NULL); // setCursorVisible(false); 도 가능하다.

        pricetotla.setText("17,000 원");
        ChQty.setText(String.valueOf(mchichenqty)); // 기본 수량

        // 수량 더하기
        plusbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mchichenqty++;
                ChQty.setText(String.valueOf(mchichenqty));
                pricetotla.setText(myFormatter.format(mchichenqty * 17000));
            }
        });

        // 수량 빼기
        minusbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mchichenqty != 0)
                {
                    mchichenqty--;
                    ChQty.setText(String.valueOf(mchichenqty));
                    pricetotla.setText(myFormatter.format(mchichenqty * 17000));
                }
            }
        });

        // 결제하기
        chicken_payment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final int totalPrice = mchichenqty * 17000;
                Log.e(TAG, "onClick: totalPrice: " + totalPrice );

                BootUser bootUser = new BootUser().setPhone("010-1234-1234");
                BootExtra bootExtra = new BootExtra().setQuotas(new int[]{0, 2, 3});

                Bootpay.init(getFragmentManager())
                        .setApplicationId("5de4c14402f57e0022423502") // 해당 프로젝트(안드로이드)의 application id 값
                        .setMethod(Method.EASY) // 결제수단
                        .setContext(BuyChicken.this)
                        .setBootUser(bootUser)
                        .setBootExtra(bootExtra)
                        .setUX(UX.PG_DIALOG)
                        .setName("치킨런 기프티콘") // 결제할 상품명
                        .setOrderId("12346") // 결제 고유번호 expire_month
                        .setPrice(totalPrice) // 결제할 금액
                        .onConfirm(new ConfirmListener()
                        {
                            @Override
                            public void onConfirm(@Nullable String message)
                            {
                                // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                                if (0 < stuck)
                                {
                                    Bootpay.confirm(message); // 재고가 있을 경우.
                                    Log.e(TAG, "onConfirm: confirm" );
                                } else
                                {
                                    //
                                    Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                                    Log.e(TAG, "onConfirm: removePaymentWindow" );
                                }

                                Toast.makeText(BuyChicken.this, "결제 완료", Toast.LENGTH_SHORT).show();

                                // todo: 구매 이력 추가하기
                                addPaymentHistory(memId, String.valueOf(mchichenqty), String.valueOf(totalPrice));

                                //                                Log.e(TAG, "confirm: confirm");
//                                Log.e("confirm", message);
                            }
                        })
                        .onDone(new DoneListener()
                        { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                            @Override
                            public void onDone(@Nullable String message)
                            {
                                Log.e(TAG, "confirm");
                                Log.e("done", message);
                            }
                        })
                        .onReady(new ReadyListener()
                        { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                            @Override
                            public void onReady(@Nullable String message)
                            {
                                Log.e(TAG, "onReady");
                                Log.e("Ready", message);
                            }
                        })
                        .onCancel(new CancelListener()
                        { // 결제 취소시 호출
                            @Override
                            public void onCancel(@Nullable String message)
                            {
                                Log.e(TAG, "onCancel");
                                Log.e("Cancel", message);
                            }
                        })
                        .onError(new ErrorListener()
                        { // 에러가 났을때 호출되는 부분
                            @Override
                            public void onError(@Nullable String message)
                            {
                                Log.e("Error", message);
                                Log.e(TAG, "onError");
                            }
                        })
                        .onClose(
                                new CloseListener()
                                { //결제창이 닫힐때 실행되는 부분
                                    @Override
                                    public void onClose(String message)
                                    {
                                        Log.e("Close", message);
                                        Log.e(TAG, "close");
                                    }
                                })
                        .request();
            }
        });
    }

    void addPaymentHistory(final String userId, final String quantity, final String totalPrice)
    {
        Log.e(TAG, "addPaymentHistory(): 구매이력 증가 / 보유 수량 업데이트");

        StringRequest stringRequest
                = new StringRequest(Request.Method.POST,
                "http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com/chicken/addChickenHistory.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "addPaymentHistory onResponse: " + response.trim());

                        if (response.trim().equals("success"))
                        {
//                            finish();
                            isPay = true;
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();

                params.put("userId", userId);
                params.put("quantity", quantity);
                params.put("totalPrice", totalPrice);

                Log.e(TAG, "getParams: userId: " + userId );
                Log.e(TAG, "getParams: quantity: " + quantity );
                Log.e(TAG, "getParams: totalPrice: " + totalPrice );

                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(BuyChicken.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // 구매 완료 후 액티비티 종료하기
        if (isPay)
        {
            isPay = false;

            Intent intent = new Intent(BuyChicken.this, MainMenu.class);
            startActivity(intent);
            finish();
        }
    }
}
