package com.example.zhangzd.zrouter.debug;

import com.example.zhangzd.zrouter.MainActivity;
import com.example.zhangzd.zrouter_api.ZRouterLoadPath;
import com.example.zhangzd.zrouterannotation.bean.RouterBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-18 10:49
 */
public class ZRouter$$Path$$App implements ZRouterLoadPath {
    /**
     * 加载路由组Group中的Path详细数据
     * 比如："app"分组下有这些信息：
     *
     * @return key:"/app/MainActivity", value:MainActivity信息封装到RouterBean对象中
     */
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String,RouterBean> pathMap = new HashMap<>();

        //判断所有组是app的注解，生成RouterBean对象添加到集合,生成文件时需要循环

        pathMap.put("app",RouterBean.create(RouterBean.Type.ACTIVITY, MainActivity.class,"/app/MainActivity","app"));

        return pathMap;
    }
}
