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

@Target(ElementType.TYPE) // 该注解作用在类之上
@Retention(RetentionPolicy.CLASS) // 要在编译时进行一些预处理操作，注解会在class文件中存在
public @interface ZRouter {
    String path();
    String group() default "";
}
