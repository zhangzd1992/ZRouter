package com.example.zhangzd.zroutercompile.utils;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-17 15:57
 */
public class Constants {
    public static final String AnnotationTypes = "com.example.zhangzd.zrouterannotation.ZRouter";
    public static final String MODULE_NAME = "moduleName";

    public static final String APT_PACKAGE = "packageNameForAPT";

    // Activity全类名
    public static final String ACTIVITY = "android.app.Activity";
    public static final String PATH_FILE_NAME = "ZRouter$$Path$$";
    public static final String GROUP_FILE_NAME ="ZRouter$$Group$$" ;

    // 包名前缀封装
    static final String BASE_PACKAGE = "com.example.zhangzd.zrouter_api";
    // 路由组Group加载接口
    public static final String ZROUTE_GROUP = BASE_PACKAGE + ".ZRouterLoadGroup";
    // 路由组Group对应的详细Path加载接口
    public static final String ZROUTE_PATH = BASE_PACKAGE + ".ZRouterLoadPath";

    // 路由组Group，参数名
    public static final String GROUP_PARAMETER_NAME = "groupMap";
    // 路由组Group，方法名
    public static final String GROUP_METHOD_NAME = "loadGroup";
    // 路由组Group对应的详细Path，参数名
    public static final String PATH_PARAMETER_NAME = "pathMap";
    // 路由组Group对应的详细Path，方法名
    public static final String PATH_METHOD_NAME = "loadPath";
}
