package com.example.chickenrun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class BuyChicken extends AppCompatActivity {


    int mchichenqty  =1;
    //금액 콤마 찍기
    DecimalFormat myFormatter = new DecimalFormat("###,###");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_chicken);

        Button  plusbtn = (Button)findViewById(R.id.plusbtn);
        Button  minusbtn = (Button)findViewById(R.id.minusbtn);
        final EditText ChQty = (EditText)findViewById(R.id.ChQty);
        final TextView pricetotla = (TextView)findViewById(R.id.totalprice);

        pricetotla.setText("17,000 원");
        ChQty.setText(mchichenqty); // 기본 수량




        // 수량 더하기
        plusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mchichenqty++;
                ChQty.setText(mchichenqty);
                pricetotla.setText(myFormatter.format(mchichenqty*1700));
            }
        });

        // 수량 빼기
        minusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mchichenqty != 0) {
                    mchichenqty--;
                    ChQty.setText(mchichenqty);
                    pricetotla.setText(myFormatter.format(mchichenqty * 17000));
                }
            }
        });
    }
}
