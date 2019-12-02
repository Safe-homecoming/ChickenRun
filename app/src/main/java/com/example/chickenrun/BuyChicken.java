package com.example.chickenrun;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_chicken);

        Button plusbtn = (Button) findViewById(R.id.plusbtn);
        Button minusbtn = (Button) findViewById(R.id.minusbtn);
        final EditText ChQty = (EditText) findViewById(R.id.ChQty);
        final TextView pricetotla = (TextView) findViewById(R.id.totalprice);
        Button chicken_payment = (Button) findViewById(R.id.chicken_payment);

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
                int totalPrice = mchichenqty * 17000;
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
                                } else
                                {
                                    Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                                }
                                Log.e(TAG, "confirm: confirm");
                                Log.e("confirm", message);

                                Toast.makeText(BuyChicken.this, "결제 완료", Toast.LENGTH_SHORT).show();
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
}
