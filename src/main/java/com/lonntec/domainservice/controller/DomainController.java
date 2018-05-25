package com.lonntec.domainservice.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lonntec.domainservice.entity.Domain;
import com.lonntec.domainservice.repository.UserRepository;
import com.lonntec.domainservice.service.DomainService;
import com.lonntec.framework.annotation.RequestSufToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.websocket.server.PathParam;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/domain")
public class DomainController {

    @Autowired
    DomainService domainService;

    @Autowired
    UserRepository userRepository;
    //创建企业域
    @RequestSufToken
    @PostMapping("/create")
    public Domain createDomain(@RequestBody JSONObject postData){
        Domain domainInfo = new Domain();
        domainInfo.setDomainName(postData.getString("domainName"));
        domainInfo.setDomainShortName(postData.getString("domainShortName"));
        domainInfo.setAddress(postData.getString("address"));
        domainInfo.setLinkMan(postData.getString("linkMan"));
        domainInfo.setLinkManMobile(postData.getString("linkManMobile"));
        domainInfo.setBusinessLicense(postData.getString("businessLicense"));
        domainInfo.setMemo(postData.getString("memo"));
        return domainService.appendDomain(domainInfo);
    }
    //获取企业列表
    @RequestSufToken
    @GetMapping("/list")
    public JSONArray findDomains(
            @PathParam("keyword") String keyword,
            @PathParam("page") Integer page,
            @PathParam("size") Integer size
    ){
        List<Domain> domainList = domainService.findDomains(keyword, page, size);
        JSONArray reValue = new JSONArray();
        for(Domain item:domainList){
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(item));
            reValue.add(json);
        }
        return reValue;
    }
    //获取用户数量
    @RequestSufToken
    @RequestMapping("/listcount")
    public Integer getListCount(@PathParam("keyword") String keyword){
                return domainService.getListCount(keyword);
    }
    //修改企业域
    @RequestSufToken
    @PostMapping("/modify")
    public JSONObject modify(@RequestBody JSONObject postData){
        String domainId = postData.getString("rowId");
        String domainName = postData.getString("domainName");
        String domainShortName = postData.getString("domainShortName");
        String address = postData.getString("address");
        String linkMan = postData.getString("linkMan");
        String linkManMobile = postData.getString("linkManMobile");
        String businessLicense = postData.getString("businessLicense");
        String memo = postData.getString("memo");
        Domain domainInfo = new Domain();
        domainInfo.setRowId(domainId);
        domainInfo.setDomainName(domainName);
        domainInfo.setDomainShortName(domainShortName);
        domainInfo.setAddress(address);
        domainInfo.setLinkMan(linkMan);
        domainInfo.setLinkManMobile(linkManMobile);
        domainInfo.setBusinessLicense(businessLicense);
        domainInfo.setMemo(memo);
        Domain domain = domainService.modifyDomain(domainInfo);
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(domain));
        return json;
    }

    //启用/禁用企业域
    @RequestSufToken
    @PostMapping("/setenable")
    public void setEnable(@RequestBody JSONObject postForm){
        String rowId = postForm.getString("rowId");
        Boolean isEnable = postForm.getBoolean("isEnable");
        domainService.setEnable(rowId,isEnable);
    }

    //企业域实施人员变更
    @RequestSufToken
    @PostMapping("/changeuser")
    public Domain changeUser(@RequestBody JSONObject postData){

        String domainId = postData.getString("domainId");
        String newOwnerId = postData.getString("newOwnerId");

        //todo:
        return domainService.changeUser(domainId, newOwnerId);
    }

    //获取已开通SUF企业列表
    @RequestSufToken
    @GetMapping("/activesuflist")
    public JSONArray findActiveSufListDomains(
            @PathParam("keyword") String keyword,
            @PathParam("page") Integer page,
            @PathParam("size") Integer size
    ){
        List<Domain> domainList = domainService.findActiveSufListDomains(keyword, page, size,true);
        JSONArray reValue = new JSONArray();
        for(Domain item:domainList){
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(item));
            json.remove("domainShortName");
            json.remove("address");
            json.remove("linkMan");
            json.remove("linkManMobile");
            json.remove("businessLicense");
            json.remove("memo");
            json.remove("ownerUser");
            json.remove("domainUsers");
            json.remove("isEnable");
            json.remove("isActiveSuf");
            reValue.add(json);
        }
        return reValue;
    }

    //获取未开通SUF企业列表
    @RequestSufToken
    @GetMapping("/unactivesuflist")
    public JSONArray findNotActiveSufListDomains(
            @PathParam("keyword") String keyword,
            @PathParam("page") Integer page,
            @PathParam("size") Integer size
    ){
        List<Domain> domainList = domainService.findActiveSufListDomains(keyword, page, size,false);
        JSONArray reValue = new JSONArray();
        for(Domain item:domainList){
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(item));
            json.remove("domainShortName");
            json.remove("address");
            json.remove("userCount");
            json.remove("expireDate");
            json.remove("businessLicense");
            json.remove("memo");
            json.remove("ownerUser");
            json.remove("domainUsers");
            json.remove("isEnable");
            json.remove("isActiveSuf");
            reValue.add(json);
        }
        return reValue;
    }
}
