package com.example.zhangzd.zrouter_api;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @Description: 参数传递帮助类
 * @Author: zhangzd
 * @CreateDate: 2019-10-24 16:05
 */
public class BundleManager {

    private Bundle bundle = new Bundle();
    // 是否回调setResult()
    private boolean isResult;
    // @NonNull不允许传null，@Nullable可以传null
    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this;
    }

    // 示例代码，需要拓展
    public BundleManager withResultString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        isResult = true;
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBundle(@NonNull Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public Object navigation(Context context,int code){

        return RouterManager.getInstance().navigation(context,code,this);
    }


}
