package com.example.zhangzd.zrouter;

import com.example.zhangzd.zrouter_api.core.IParameter;

/**
 * @Description: 参数获取示例类    必须与要赋值参数的activity在同一包下才可以直接赋值
 * @Author: zhangzd
 * @CreateDate: 2019-10-23 14:38
 */
public class XActivity$$Parameter implements IParameter {
    @Override
    public void loadParameter(Object target) {
        MainActivity t = (MainActivity) target;
        t.name = t.getIntent().getStringExtra("name");
        t.age = t.getIntent().getIntExtra("age",t.age);
    }
}
