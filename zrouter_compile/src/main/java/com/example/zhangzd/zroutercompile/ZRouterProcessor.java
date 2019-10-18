package com.example.zhangzd.zroutercompile;

import com.example.zhangzd.zrouterannotation.ZRouter;
import com.example.zhangzd.zrouterannotation.bean.RouterBean;
import com.example.zhangzd.zroutercompile.utils.Constants;
import com.example.zhangzd.zroutercompile.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-17 15:54
 */

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Constants.AnnotationTypes})
public class ZRouterProcessor extends AbstractProcessor {
    // 操作Element工具类 (类、函数、属性都是Element)
    private Elements elementUtils;

    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;

    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;

    // 文件生成器 类/资源，Filter用来创建新的源文件，class文件以及辅助文件
    private Filer filer;
    //模块名
    private String moduleName;
    //包名
    private String packageNameForAPT;


    // 临时map存储，用来存放路由组Group对应的详细Path类对象，生成路由路径类文件时遍历
    // key:组名"app", value:"app"组的路由路径"ARouter$$Path$$app.class"
    private Map<String, List<RouterBean>> tempPathMap = new HashMap<>();
    // 临时map存储，用来存放路由Group信息，生成路由组类文件时遍历
    // key:组名"app", value:类名"ZRouter$$Path$$app.class"
    private Map<String, String> tempGroupMap = new HashMap<>();


    // 该方法主要用于一些初始化的操作，通过该方法的参数ProcessingEnvironment可以获取一些列有用的工具类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        Map<String, String> options = processingEnvironment.getOptions();

        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
            packageNameForAPT = options.get(Constants.APT_PACKAGE);
            // 有坑：Diagnostic.Kind.ERROR，异常会自动结束，不像安卓中Log.e
            messager.printMessage(Diagnostic.Kind.NOTE, "moduleName >>> " + moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageNameForAPT >>> " + packageNameForAPT);

        }

        // 必传参数判空（乱码问题：添加java控制台输出中文乱码）
        if (EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForAPT)) {
            throw new RuntimeException("注解处理器需要的参数moduleName或者packageName为空，请在对应build.gradle配置参数");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ZRouter.class);
        if (!EmptyUtils.isEmpty(elements)) {
            try {
                parseElement(elements);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    // 解析所有被 @ARouter 注解的 类元素集合
    private void parseElement(Set<? extends Element> elements) throws IOException {
        // 通过Element工具类，获取Activity、Callback类型
        TypeElement activityType = elementUtils.getTypeElement(Constants.ACTIVITY);
        // 显示类信息（获取被注解节点，类节点）这里也叫自描述 Mirror
        TypeMirror activityTypeMirror = activityType.asType();
        for (Element element : elements) {
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            TypeMirror typeMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "typeMirror >>> " + typeMirror.toString());
            messager.printMessage(Diagnostic.Kind.NOTE, "packageName >>> " + packageName);
            messager.printMessage(Diagnostic.Kind.NOTE, "className >>> " + className);
            ZRouter annotation = element.getAnnotation(ZRouter.class);
            String path = annotation.path();
            messager.printMessage(Diagnostic.Kind.NOTE, "path >>> " + path);

            //创建RouterBean对象
            RouterBean routerBean = new RouterBean.Builder()
                    .setElement(element)
                    .setGroup(annotation.group())
                    .setPath(annotation.path())
                    .build();

            // 高级判断：ZRouter注解仅能用在类之上，并且是规定的Activity
            // 类型工具类方法isSubtype，相当于instance一样
            if (typeUtils.isSubtype(typeMirror, activityTypeMirror)) {
                routerBean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("@ZRouter注解目前仅限用于Activity类之上");

            }
            valueOfPathMap(routerBean);

            TypeElement groupLoadType = elementUtils.getTypeElement(Constants.ZROUTE_GROUP);
            TypeElement pathLoadType = elementUtils.getTypeElement(Constants.ZROUTE_PATH);

            //// 第一步：生成路由组Group对应详细Path类文件，如：ZRouter$$Path$$app
            createPathFile(pathLoadType);

            //// 第二步：生成路由组Group类文件（没有第一步，取不到类文件），如：ZRouter$$Group$$app
            createGroupFile(groupLoadType, pathLoadType);


        }
    }

    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {

        // 判断是否有需要生成的类文件
        if (EmptyUtils.isEmpty(tempGroupMap) || EmptyUtils.isEmpty(tempPathMap)) return;
        /**
         * @Override
         * public Map<String, Class<? extends ZRouterLoadPath>> loadGroup() {
         *    Map<String,Class<? extends ZRouterLoadPath>> groupMap = new HashMap<>();
         *    groupMap.put("app", ZRouter$$Path$$App.class);
         *    return groupMap;
         * }
         */
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType)))
        );

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);


        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.GROUP_PARAMETER_NAME,
                ClassName.get(HashMap.class));


        for (Map.Entry<String,String> entry : tempGroupMap.entrySet()) {
            String className = entry.getValue();
            String group = entry.getKey();
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    group,
                    // 类文件在指定包名下
                    ClassName.get(packageNameForAPT,className));


        }
        methodBuilder.addStatement("return $N",Constants.GROUP_PARAMETER_NAME);
        String finalClassName = Constants.GROUP_FILE_NAME + moduleName;

        JavaFile.builder(packageNameForAPT,
                TypeSpec.classBuilder(finalClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(groupLoadType))
                        .addMethod(methodBuilder.build())
                        .build())
                .build()
                .writeTo(filer);
    }

    /**
     * 生成路由组Group对应详细Path，如：ARouter$$Path$$app
     *
     * @param pathLoadType ARouterLoadPath接口信息
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempPathMap)) {
            return;
        }

        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));


        for (Map.Entry<String, List<RouterBean>> entry : tempPathMap.entrySet()) {
            String group = entry.getKey();
            List<RouterBean> pathList = entry.getValue();

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);

            /**
             * Map<String,RouterBean> pathMap = new HashMap<>();
             */
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    ClassName.get(HashMap.class));

            /**
             * 判断所有组是app的注解，生成RouterBean对象添加到集合,生成文件时需要循环
             *
             * pathMap.put("app",RouterBean.create(RouterBean.Type.ACTIVITY, MainActivity.class,"/app/MainActivity","app"));
             * return pathMap;
             */

            for (RouterBean routerBean : pathList) {
                methodBuilder.addStatement(
                        "$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        Constants.PATH_PARAMETER_NAME, // pathMap.put
                        routerBean.getPath(),          // "/app/MainActivity"
                        ClassName.get(RouterBean.class),// RouterBean
                        ClassName.get(RouterBean.Type.class), // RouterBean.Type
                        routerBean.getType(),  // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) routerBean.getElement()), // MainActivity.class
                        routerBean.getPath(),  // 路径名
                        routerBean.getGroup()  // 组名
                        );

            }


            methodBuilder.addStatement("return $N",Constants.PATH_PARAMETER_NAME);
            // 最终生成的类文件名
            String finalClassName = Constants.PATH_FILE_NAME + group;
            messager.printMessage(Diagnostic.Kind.NOTE, "entry.key："
                    + entry.getKey()
                    + ";;;;entry.value"
                    + entry.getValue().size());
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件："
                    + packageNameForAPT
                    + "."
                    + finalClassName);

            JavaFile.builder(packageNameForAPT,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathLoadType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodBuilder.build())
                            .build())
            .build()
            .writeTo(filer);

            tempGroupMap.put(entry.getKey(),finalClassName);
        }
    }

    /**
     * 赋值临时map存储，用来存放路由组Group对应的详细Path类对象，生成路由路径类文件时遍历
     *
     * @param routerBean 路由详细信息，最终实体封装类
     */
    private void valueOfPathMap(RouterBean routerBean) {
        if (checkRouterPath(routerBean)) {
            //判断routerBean中信息是否合法
            //合法后将routerBean存入临时集合
            List<RouterBean> routerBeans = tempPathMap.get(routerBean.getGroup());
            if (EmptyUtils.isEmpty(routerBeans)) {
                //不存在该组路由信息，创建该组路由信息集合，将信息加入集合，并将集合存入临时map
                routerBeans = new ArrayList<>();
                routerBeans.add(routerBean);
                tempPathMap.put(routerBean.getGroup(), routerBeans);
            } else {
                //已存在该组的路由信息，则直接加入到改组的集合
                routerBeans.add(routerBean);
            }
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ZRouter注解未按规范配置，如：/app/MainActivity");
        }


    }

    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param bean 路由详细信息，最终实体封装类
     */
    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();

        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // @ARouter注解中的group有赋值情况
        if (!EmptyUtils.isEmpty(group) && !group.equals(moduleName)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        return true;
    }
}
