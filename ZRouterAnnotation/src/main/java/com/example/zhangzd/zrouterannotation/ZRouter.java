package com.example.zhangzd.zrouterannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-17 15:49
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ZRouter {
    String path();
    String group() default "";
}
