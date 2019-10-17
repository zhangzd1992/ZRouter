package com.example.zhangzd.zrouter_api;

import java.util.Map;

/**
 * @Description: 路由组Group加载数据接口
 * @Author: zhangzd
 * @CreateDate: 2019-10-17 16:40
 */
public interface ZRouterLoadGroup {
    /**
     * 加载路由组Group数据
     * 比如："app", ARouter$$Path$$app.class（实现了ARouterLoadPath接口）
     *
     * @return key:"app", value:"app"分组对应的路由详细对象类
     */
    Map<String,Class<? extends ZRouterLoadPath>> loadGroup();
}
