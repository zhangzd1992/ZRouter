package com.example.zhangzd.zrouter_api;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.LruCache;

import com.example.zhangzd.zrouter_api.core.IParameter;

/**
 * @Description: 参数处理类
 * @Author: zhangzd
 * @CreateDate: 2019-10-24 11:31
 */
public class ParameterManager {
    //缓存参数接口实现
    private LruCache<String, IParameter> cache;
    private static ParameterManager instance;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }

        return instance;
    }



    private ParameterManager() {
        cache = new LruCache<>(163);
    }



    public void loadParameter(@NonNull Activity activity) {
        String activityName = activity.getClass().getName();
        IParameter iParameter = cache.get(activityName);
        String FILE_SUFFIX_NAME = "$$Parameter";
        String className = activityName + FILE_SUFFIX_NAME;
        try {
            if (iParameter == null) {
                Class<?> clazz = Class.forName(className);
                iParameter = (IParameter) clazz.newInstance();
                cache.put(activityName,iParameter);
            }

            iParameter.loadParameter(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
