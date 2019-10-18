package com.example.zhangzd.person;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.zhangzd.zrouterannotation.ZRouter;

@ZRouter(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_activity_main);
    }
}
