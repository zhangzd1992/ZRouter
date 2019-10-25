package com.example.zhangzd.zrouter_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.LruCache;

import com.example.zhangzd.zrouter_api.core.ZRouterLoadGroup;
import com.example.zhangzd.zrouter_api.core.ZRouterLoadPath;
import com.example.zhangzd.zrouterannotation.bean.RouterBean;

import java.util.Map;

/**
 * @Description: 路由跳转帮助类
 * @Author: zhangzd
 * @CreateDate: 2019-10-24 16:05
 */
public class RouterManager {

    private static RouterManager instance;
    private LruCache<String, ZRouterLoadGroup> groupLruCache;
    private LruCache<String, ZRouterLoadPath> pathLruCache;
    private String group;
    private String path;
    private static final String GROUP_FILE_PREFIX_NAME = ".ZRouter$$Group$$";

    private RouterManager() {
        groupLruCache = new LruCache<>(163);
        pathLruCache = new LruCache<>(163);
    }


    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }


    public BundleManager build(String path) {
        group = getGroup(path);
        this.path = path;

        return new BundleManager();
    }

    private String getGroup(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("未按照/app/MainActivity的格式传递path");
        }

        if (!path.startsWith("/") || path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("未按照/app/MainActivity的格式传递path");
        }

        return path.substring(1, path.lastIndexOf("/"));
    }

    public Object navigation(Context context, int code, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + ".apt" + GROUP_FILE_PREFIX_NAME + group;


        try {
            ZRouterLoadGroup loadGroup = groupLruCache.get(group);
            if (loadGroup == null) {
                Class<?> groupClazz = Class.forName(groupClassName);
                loadGroup = (ZRouterLoadGroup) groupClazz.newInstance();
                groupLruCache.put(group, loadGroup);
            }

            Map<String, Class<? extends ZRouterLoadPath>> pathMap = loadGroup.loadGroup();
            if (pathMap.isEmpty()) {
                throw new RuntimeException("未按照/app/MainActivity的格式传递path");
            }
            ZRouterLoadPath zRouterLoadPath = pathLruCache.get(path);
            if (pathLruCache.get(path) == null) {
                Class<? extends ZRouterLoadPath> loadPathClazz = pathMap.get(group);
                if (loadPathClazz != null) {
                    zRouterLoadPath = loadPathClazz.newInstance();
                    pathLruCache.put(path,zRouterLoadPath);
                }
            }

            Map<String, RouterBean> routerBeanMap = zRouterLoadPath.loadPath();
            RouterBean routerBean = routerBeanMap.get(path);

            if (routerBean != null) {
                switch (routerBean.getType()) {

                    case ACTIVITY:
                        Intent i = new Intent(context,routerBean.getClazz());
                        if (bundleManager.isResult()) {
                            ((Activity)context).setResult(code);
                            ((Activity)context).finish();
                        }

                        if (code > 0) {
                            ((Activity)context).startActivityForResult(i,code,bundleManager.getBundle());
                        }else {
                            context.startActivity(i,bundleManager.getBundle());
                        }

                        break;

                    case CALL:
                        //返回的是Call接口实现类
                        return routerBean.getClazz().newInstance();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
