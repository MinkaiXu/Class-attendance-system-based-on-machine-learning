package com.demo.aicas.common;

/**
 * 表示登陆信息的类
 * username
 * password
 * role：0：Teacher；1：Student
 */
public class LoginInfo {
    private String userName;
    private String passWord;
    private boolean role;

    public LoginInfo(String userName, String passWord, boolean role) {
        this.userName = userName;
        this.passWord = passWord;
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public boolean isRole() {
        return role;
    }
}
