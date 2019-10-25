package com.example.zhangzd.order;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.zhangzd.zrouter_api.ParameterManager;
import com.example.zhangzd.zrouterannotation.Parameter;
import com.example.zhangzd.zrouterannotation.ZRouter;

@ZRouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter(name = "order")
    boolean isOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);
        ParameterManager.getInstance().loadParameter(this);
        Log.e("Order_MainActivity","isOrder:" + isOrder);
    }



    public void changeText() {
        TextView viewById = findViewById(R.id.tvText);
        viewById.setText("我被改变了");
    }
}
