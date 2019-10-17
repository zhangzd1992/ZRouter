package com.example.zhangzd.zrouter_api;

import com.example.zhangzd.zrouterannotation.bean.RouterBean;

import java.util.Map;

/**
 * @Description: 路由组Group对应的详细Path加载数据接口
 * @Author: zhangzd
 * @CreateDate: 2019-10-17 16:39
 */
public interface ZRouterLoadPath {
    /**
     * 加载路由组Group中的Path详细数据
     * 比如："app"分组下有这些信息：
     *
     * @return key:"/app/MainActivity", value:MainActivity信息封装到RouterBean对象中
     */
    Map<String, RouterBean> loadPath();
}
