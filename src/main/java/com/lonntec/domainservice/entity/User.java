package com.lonntec.domainservice.entity;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name="t_sys_user")
public class User {
    @Id
    @Column(name="frowid", length = 36)
    String rowId;

    @Column(name="fusername")
    String userName;

    @Column(name="fnickname")
    String nickName;

    @Column(name="fmobile")
    String mobile;

    @Column(name="femail")
    String email;

    @Column(name="fisadmin")
    Boolean isAdmin;

    @Column(name="fisenable")
    Boolean isEnable;

    @Transient
    String password;

    @Column(name="fpasswordhash")
    String passwordHash;

    public User() {
        rowId = UUID.randomUUID().toString();
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Boolean enable) {
        isEnable = enable;
    }
    public void setPassword(String password) {
        this.password = password;
        try{
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5Data = digest.digest(this.password.getBytes("utf-8"));
            this.passwordHash =  Base64.getEncoder().encodeToString(md5Data);
        }catch (Exception ex){
            ex.printStackTrace();
            this.passwordHash = this.password;
        }
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    @Override
    public String toString() {
        return "User{" +
                "rowId='" + rowId + '\'' +
                ", userName='" + userName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", isEnable=" + isEnable +
                '}';
    }
}
