package com.lonntec.domainservice.entity;

import javax.persistence.*;

@Entity
@Table(name="t_domain_domainuser")
public class DomainUser {
    @Id
    @Column(name = "frowid")
    String rowId;

    @Column(name = "fusername")
    String userName;

    @Transient
    String domainUserPassword;

    @Column(name = "fpasswordhash")
    String passwordHash;

    @Column(name = "fmobile")
    String mobile;

    @Column(name = "femail")
    String email;

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

    public String getDomainUserPassword() {
        return domainUserPassword;
    }

    public void setDomainUserPassword(String domainUserPassword) {
        this.domainUserPassword = domainUserPassword;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
}
