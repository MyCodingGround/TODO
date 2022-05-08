package com.example.todobackend.dto;

public class userDTO {

    private String userName;
    private String passWord;
    private String gmail;

    public userDTO() {
    }

    public userDTO(String userName, String passWord, String gmail) {
        this.userName = userName;
        this.passWord = passWord;
        this.gmail = gmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    @Override
    public String toString() {
        return "userDTO{" +
                "userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", gmail='" + gmail + '\'' +
                '}';
    }
}
