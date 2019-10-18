package com.example.zhangzd.order;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.zhangzd.zrouterannotation.ZRouter;

@ZRouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);
    }
}
