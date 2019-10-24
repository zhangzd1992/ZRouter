package com.example.zhangzd.zrouter.debug;


import com.example.zhangzd.zrouter_api.core.ZRouterLoadGroup;
import com.example.zhangzd.zrouter_api.core.ZRouterLoadPath;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-18 10:53
 */
public class ZRouter$$Group$$App implements ZRouterLoadGroup {
    /**
     * 加载路由组Group数据
     * 比如："app", ARouter$$Path$$app.class（实现了ARouterLoadPath接口）
     *
     * @return key:"app", value:"app"分组对应的路由详细对象类
     */
    @Override
    public Map<String, Class<? extends ZRouterLoadPath>> loadGroup() {
        Map<String,Class<? extends ZRouterLoadPath>> groupMap = new HashMap<>();

        //
        groupMap.put("app", ZRouter$$Path$$App.class);



        return groupMap;
    }
}
