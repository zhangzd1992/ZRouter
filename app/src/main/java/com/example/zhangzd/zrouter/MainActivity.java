package com.example.zhangzd.zrouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zhangzd.common.OrderCall;
import com.example.zhangzd.zrouter_api.ParameterManager;
import com.example.zhangzd.zrouter_api.RouterManager;
import com.example.zhangzd.zrouterannotation.Parameter;
import com.example.zhangzd.zrouterannotation.ZRouter;


@ZRouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter(name = "name")
    String name;
    @Parameter(name = "age")
    int age = 0;

    @Parameter(name = "/order/CallBackImpl")
    OrderCall orderCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParameterManager.getInstance().loadParameter(this);
        String oderNum = orderCall.getOderNum();
        Toast.makeText(this,"orderNum:"+ oderNum,Toast.LENGTH_SHORT).show();
    }

    public void jumpOrder(View view) {
//        ZRouter$$Group$$order zRouter$$Group$$order = new ZRouter$$Group$$order();
//        Map<String, Class<? extends ZRouterLoadPath>> loadGroup = zRouter$$Group$$order.loadGroup();
//        Class<? extends ZRouterLoadPath> aClass = loadGroup.get("order");
//        try {
//            ZRouterLoadPath zRouterLoadPath = aClass.newInstance();
//            Map<String, RouterBean> pathMap = zRouterLoadPath.loadPath();
//            RouterBean routerBean = pathMap.get("/order/Order_MainActivity");
//            Class<?> Oder_MainActivity_clazz = routerBean.getClazz();
//            Intent i = new Intent(this,Oder_MainActivity_clazz);
//            i.putExtra("order",true);
//            startActivity(i);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        RouterManager.getInstance().build("/order/Order_MainActivity")
                .withBoolean("order",true)
                .navigation(this,0);


    }

    public void jumpPersonal(View view) {
       Object  order =  RouterManager.getInstance().build("/order/CallBackImpl")
                .withBoolean("order", true)
                .navigation(this);
        try {
          OrderCall call = (OrderCall) order;
            Log.e("main","orderNum==" + call.getOderNum());
            Toast.makeText(this,"iejfjf::::" + call.getOderNum(),Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
