package com.wyan.springboot.opration.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wyan
 * @title: UserInfo
 * @projectName opration
 * @description: TODO
 * @date 2020/8/26 9:13 上午
 * @company 西南凯亚-DDC-4 PART
 */
@Getter
@Setter
public class UserInfo implements Serializable {
    int id;
    String name;
    String desc;
}
