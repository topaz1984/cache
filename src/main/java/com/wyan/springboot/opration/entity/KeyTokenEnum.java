package com.wyan.springboot.opration.entity;

/**
 * @author wyan
 * @title: KeyTokenEnum
 * @projectName opration
 * @description: TODO
 * @date 2020/9/26 5:47 下午
 * @company 西南凯亚-DDC-4 PART
 */
public enum KeyTokenEnum {
    SPLIT(":"), JOIN("+");
//    SPLIT("AND", ":"), JOIN("AND", "+");
//    private String expression;
//    private String token;
//
//    private KeyTokenEnum(String expression, String token) {
//        this.expression = expression;
//        this.token = token;
//    }
//
//
//    public static String getToken(int expression) {
//        for (KeyTokenEnum t : KeyTokenEnum.values()) {
//            if (t.getExpression().equals(expression)) {
//                return t.token;
//            }
//        }
//        return null;
//    }
//
//    public String getExpression() {
//        return expression;
//    }
//
//    public void setExpression(String expression) {
//        this.expression = expression;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }

    KeyTokenEnum(String s) {
    }
}
