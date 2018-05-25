package com.lonntec.domainservice.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "t_domain")
public class Domain {
    @Id
    @Column(name = "frowid")
    String rowId;

    @Column(name = "fdomainnumber")
    String domainNumber;

    @Column(name = "fdomainname")
    String domainName;

    @Column(name = "fdomainshortname")
    String domainShortName;

    @Column(name = "faddress")
    String address;

    @Column(name = "flinkman")
    String linkMan;

    @Column(name = "flinkmanmobile")
    String linkManMobile;

    @Column(name = "fbusinesslicense")
    String businessLicense;

    @Column(name = "fmemo")
    String memo;

    @Column(name = "fisenable")
    Boolean isEnable;

    @Column(name = "fisactivesuf")
    Boolean isActiveSuf;

    @Column(name = "fusercount")
    Integer userCount;

    @Column(name = "fexpiredate")
    Date expireDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderBy(value = "fusername")
    @JoinTable(name = "t_domain_domainuser_rel",
            joinColumns = @JoinColumn(name = "fdomainid",referencedColumnName = "frowid"),
            inverseJoinColumns = @JoinColumn(name = "fdomainuserid",referencedColumnName = "frowid")
    )
    List<DomainUser> domainUsers=new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @OrderBy(value = "fexpiredate")
    @JoinColumn(name = "fownerid")
    User ownerUser;

    public Domain() {
        rowId = UUID.randomUUID().toString();
        isActiveSuf =false;
        isEnable=false;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getDomainNumber() {
        return domainNumber;
    }

    public void setDomainNumber(String domainNumber) {
        this.domainNumber = domainNumber;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainShortName() {
        return domainShortName;
    }

    public void setDomainShortName(String domainShortName) {
        this.domainShortName = domainShortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLinkMan() {
        return linkMan;
    }

    public void setLinkMan(String linkMan) {
        this.linkMan = linkMan;
    }

    public String getLinkManMobile() {
        return linkManMobile;
    }

    public void setLinkManMobile(String linkManMobile) {
        this.linkManMobile = linkManMobile;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Boolean enable) {
        isEnable = enable;
    }

    public Boolean getIsActiveSuf() {
        return isActiveSuf;
    }

    public void setIsActiveSuf(Boolean activeSuf) {
        isActiveSuf = activeSuf;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public List<DomainUser> getDomainUsers() {
        return domainUsers;
    }

    public void setDomainUsers(List<DomainUser> domainUsers) {
        this.domainUsers = domainUsers;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    @Override
    public String toString() {
        return "Domain{" +
                "rowId='" + rowId + '\'' +
                ", domainNumber='" + domainNumber + '\'' +
                ", domainName='" + domainName + '\'' +
                ", domainShortName='" + domainShortName + '\'' +
                ", address='" + address + '\'' +
                ", linkMan='" + linkMan + '\'' +
                ", linkManMobile='" + linkManMobile + '\'' +
                ", businessLicense='" + businessLicense + '\'' +
                ", memo='" + memo + '\'' +
                ", isEnable=" + isEnable +
                ", isActiveSuf=" + isActiveSuf +
                ", userCount=" + userCount +
                ", expireDate='" + expireDate + '\'' +
                ", domainUsers=" + domainUsers +
                ", ownerUser=" + ownerUser +
                '}';
    }
}
