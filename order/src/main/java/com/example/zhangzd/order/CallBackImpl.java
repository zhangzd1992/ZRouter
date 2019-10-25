package com.example.zhangzd.order;


import com.example.zhangzd.common.OrderCall;
import com.example.zhangzd.zrouterannotation.ZRouter;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-25 14:37
 */
@ZRouter(path = "/order/CallBackImpl")
public class CallBackImpl implements OrderCall {


    @Override
    public String getOderNum() {
        return "order";
    }
}
