package com.example.zhangzd.zroutercompile;

import com.example.zhangzd.zrouterannotation.Parameter;
import com.example.zhangzd.zroutercompile.utils.Constants;
import com.example.zhangzd.zroutercompile.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @Description:
 * @Author: zhangzd
 * @CreateDate: 2019-10-23 14:43
 */

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Constants.ParameterAnnotationType})
public class ParameterProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();
    
    
    
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (EmptyUtils.isEmpty(set)) return false;

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
        if (!EmptyUtils.isEmpty(elements)) {
            // 生成类文件，如：
            try {
                valueOfParameterMap(elements);
                createParameterFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    private void createParameterFile() throws IOException {
        if (EmptyUtils.isEmpty(tempParameterMap)) {
            return;
        }


        TypeElement activityTypeElement = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeElement iparameterType = elementUtils.getTypeElement(Constants.IPARAMETER);
        Set<Map.Entry<TypeElement, List<Element>>> entries = tempParameterMap.entrySet();
        //方法参数
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAMR).build();

        for (Map.Entry<TypeElement, List<Element>> entry :entries) {
            TypeElement typeElement = entry.getKey();
            List<Element> elements = entry.getValue();

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PARAMETER_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addParameter(parameterSpec)
                    .addModifiers(Modifier.PUBLIC);

            TypeMirror typeMirror = typeElement.asType();
            String activityName = typeElement.getSimpleName().toString();
            //取类名
            ClassName className =ClassName.get(typeElement);
            //判断注解所在类是否是Activity
            if (typeUtils.isSubtype(typeMirror,activityTypeElement.asType())) {

                //将参数转化为Activity   MainActivity t = (MainActivity) target;
                methodBuilder.addStatement("$T t = ($T)target", className, className);

                //循环所有Filed
                for (Element element : elements) {
                    // 遍历注解的属性节点 生成函数体
                    //t.name = t.getIntent().getStringExtra("name");
                    TypeMirror fieldTypeMirror = element.asType();
                    // 获取 TypeKind 枚举类型的序列号
                    int type = fieldTypeMirror.getKind().ordinal();
                    //获取属性名
                    String filedName = element.getSimpleName().toString();

                    Parameter parameter = element.getAnnotation(Parameter.class);
                    if (parameter != null) {
                        String annotationValue = parameter.name();
                        annotationValue = (EmptyUtils.isEmpty(annotationValue) ? filedName : annotationValue);
                        // 最终拼接的前缀：
                        String finalValue = "t." + filedName;
                        // t.s = t.getIntent().
                        String methodContent = finalValue + " = t.getIntent().";
                        if (type == TypeKind.INT.ordinal()) {
                            methodContent += "getIntExtra($S, " + finalValue + ")";
                        } else if(type == TypeKind.BOOLEAN.ordinal()) {
                            methodContent += "getBooleanExtra($S, " + finalValue + ")";
                        }else {
                            // t.s = t.getIntent.getStringExtra("s");
                            if (fieldTypeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                                methodContent += "getStringExtra($S)";
                            }
                        }
                        messager.printMessage(Diagnostic.Kind.NOTE, "methodContent: " + methodContent);
                        // 健壮代码
                        if (methodContent.endsWith(")")) {
                            // 添加最终拼接方法内容语句
                            methodBuilder.addStatement(methodContent, annotationValue);
                        } else {
                            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
                        }
                    }

                }


                JavaFile.builder(className.packageName(),
                        TypeSpec.classBuilder(activityName + Constants.PARAMETER_FILE_NAME)
                                .addMethod(methodBuilder.build())
                                .addSuperinterface(ClassName.get(iparameterType))
                                .addModifiers(Modifier.PUBLIC)
                                .build())
                        .build()
                        .writeTo(filer);

            }else {
                throw new IllegalArgumentException("该注解必须应用在Actvity中的成员属性之上");
            }

        }


    }

    /**
     * 赋值临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
     *
     * @param elements 被 @Parameter 注解的 元素集合
     */
    private void valueOfParameterMap(Set<? extends Element> elements) {
        for (Element element : elements) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if (tempParameterMap.containsKey(enclosingElement)) {
                tempParameterMap.get(enclosingElement).add(element);
            }else {
                List<Element> parameters = new ArrayList<>();
                parameters.add(element);
                tempParameterMap.put(enclosingElement,parameters);
            }
        }
    }
}
