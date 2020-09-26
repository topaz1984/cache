package com.wyan.springboot.opration.config.aspect.util;



import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wyan
 * @title: AnnotationParamResolver
 * @projectName aic-cores
 * @description: TODO 还需要具体补充解析的模式，暂时按照 有先从":"拆分，开始逐个解析。再对"+"进行拆分，再逐个对"#{}"来解析动态对象参数。
 * 期望：实现key解析："模块:方法:参数1:#{变量1}:参数2#{变量2}....."方式。不传根据参数进行相关压缩处理，生成唯一key。
 * @date 2020/8/26 4:02 下午
 */
public class AnnotationParamResolver {

    private static AnnotationParamResolver resolver;

    public static AnnotationParamResolver newInstance() {

        if (Objects.isNull(resolver)) {
            return resolver = new AnnotationParamResolver();
        } else {
            return resolver;
        }

    }


    /**
     * 解析注解上的值
     *
     * @param joinPoint
     * @param val       需要解析的字符串
     * @return
     */
    public Object preResolver(ProceedingJoinPoint joinPoint, String val) {
        if (Objects.isNull(val)) return null;
        Object value = null;
        //按照格式进行分解 具体类型后续再次详细约定或自动生成key
        String[] splitArrays = null;
        List paramList = new ArrayList<Object>();
        if(val.contains(":")){
            splitArrays = val.split(":");
            for(String outerArray : splitArrays){
                mainResolver( joinPoint,outerArray,paramList);
            }
        }else{
            mainResolver( joinPoint,val,paramList);
        }
        //将自定义变量和key的其他字符串以":"的方式进行拼接
        value = paramList.stream().filter(v -> v != null).map(Objects::toString).collect(Collectors.joining(":"));
        return value;
    }

    private void mainResolver(ProceedingJoinPoint joinPoint,String outerArg,List paramList){
        Object[] splitArrays = outerArg.split("\\+");
        if (splitArrays.length >= 1) {
            for (Object innerArg : splitArrays) {
                String arg = innerArg == null ? "" : innerArg.toString();
                Object item = singleResolverStage(joinPoint, arg);
                paramList.add(item);
            }
        } else {
            paramList.add(singleResolverStage(joinPoint, outerArg));
        }
    }


    private Object singleResolverStage(ProceedingJoinPoint joinPoint, String val) {
        Object value = null;
        if (val.matches("#\\{\\D*\\}")) {// 如果name匹配上了#{},则把内容当作变量
            String newStr = val.replaceAll("#\\{", "").replaceAll("\\}", "");
            if (newStr.contains(".")) { // 复杂类型
                try {
                    value = complexResolver(joinPoint, newStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                value = simpleResolver(joinPoint, newStr);
            }
        } else { //非变量
            value = val;
        }
        return value;
    }

    /**
     *
     * @param joinPoint
     * @param str 当前cache注解中传过来的代表对象的特殊字符串，一般如 user.id
     * @return Object value
     * @throws Exception
     */
    private Object complexResolver(ProceedingJoinPoint joinPoint, String str) throws Exception {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        String[] strArrays = str.split("\\.");

        for (int i = 0; i < names.length; i++) {
            if (strArrays[0].equals(names[i])) {
                Object obj = args[i];
                Method declaredMethod = obj.getClass().getDeclaredMethod(getMethodName(strArrays[1]), null);
                Object value = declaredMethod.invoke(args[i]);
                return getValue(value, 1, strArrays);
            }
        }
        return null;
    }

    /**
     * 通过反射方式 获取对象参数中对应的值
     * @param obj
     * @param index
     * @param strArrays
     * @return
     */
    private Object getValue(Object obj, int index, String[] strArrays) {

        try {
            if (Objects.isNull(obj) && index < strArrays.length - 1) {
                Method method = obj.getClass().getDeclaredMethod(getMethodName(strArrays[index + 1]), null);
                obj = method.invoke(obj);
                getValue(obj, index + 1, strArrays);
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param name
     * @return 获取对象方法名称
     */
    private String getMethodName(String name) {
        return "get" + name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
    }

    /**
     * 解析不包含特殊key定义字符的字符串对象 如："123"  特殊字符："#{user.id}"
     * @param joinPoint
     * @param str
     * @return
     */
    private Object simpleResolver(ProceedingJoinPoint joinPoint, String str) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < names.length; i++) {
            if (str.equals(names[i])) {
                return args[i];
            }
        }
        return null;
    }
}
