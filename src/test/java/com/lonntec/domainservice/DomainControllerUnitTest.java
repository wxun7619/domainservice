package com.lonntec.domainservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lonntec.domainservice.entity.Domain;
import com.lonntec.domainservice.entity.User;
import com.lonntec.domainservice.lang.DomainSystemStateCode;
import com.lonntec.domainservice.proxy.CodeRuleService;
import com.lonntec.domainservice.repository.DomainRepository;
import com.lonntec.domainservice.repository.UserRepository;
import com.lonntec.domainservice.service.DomainService;
import com.lonntec.framework.lang.Result;
import com.lonntec.framework.lang.SystemStateCode;
import com.lonntec.framework.service.TokenService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DomainControllerUnitTest {
    @Autowired
    private WebApplicationContext context;

    MockMvc mockMvc;

    @Autowired
    TokenService tokenService;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    UserRepository userRepository;

    @MockBean
    CodeRuleService codeRuleService;

    @Autowired
    DomainService domainService;

    @Before
    public  void beforeTest() throws Exception{
        mockMvc= MockMvcBuilders.webAppContextSetup(context).build();

        Mockito.when(codeRuleService.generateCode(Mockito.anyString())).thenReturn(UUID.randomUUID().toString());
        //创建用户 管理员
        User userIsAdmin=new User();
        userIsAdmin.setRowId("18052501");
        userIsAdmin.setUserName("admin");
        userIsAdmin.setNickName("adm");
        userIsAdmin.setMobile("2018052501");
        userIsAdmin.setEmail("admin@adm.com");
        userIsAdmin.setIsAdmin(true);
        userIsAdmin.setIsEnable(true);
        userIsAdmin.setPassword("123456");
        userRepository.save(userIsAdmin);
        //创建用户 非管理员
        User userNotAdmin=new User();
        userNotAdmin.setRowId("18052502");
        userNotAdmin.setUserName("UserTest");
        userNotAdmin.setNickName("UT");
        userNotAdmin.setMobile("2018052502");
        userNotAdmin.setEmail("User@Test.com");
        userNotAdmin.setIsAdmin(false);
        userNotAdmin.setIsEnable(true);
        userNotAdmin.setPassword("123456");
        userRepository.save(userNotAdmin);

        //admin创建企业域
        for(int i=1;i<101;i++){
            JSONObject object=new JSONObject();
            String token=adminlogin();
            Domain domain=new Domain();
            domain.setRowId("1000"+i);
            domain.setDomainName("domainByAdmin"+i);
            domain.setDomainShortName("dbAdmin"+i);
            domain.setAddress("桃花源");
            domain.setLinkMan("欧阳修");
            domain.setLinkManMobile("1000"+i);
            domain.setIsActiveSuf(false);
            domain.setIsEnable(false);
            domain.setOwnerUser(userNotAdmin);
            domainRepository.save(domain);
        }
        //非admin创建企业域
        for(int i=1;i<101;i++){
            JSONObject object=new JSONObject();
            String token=userlogin();
            Domain domain=new Domain();
            domain.setRowId("2000"+i);
            domain.setDomainName("domainByUser"+i);
            domain.setDomainShortName("dbUser"+i);
            domain.setAddress("乌托邦");
            domain.setLinkMan("晋武陵人");
            domain.setLinkManMobile("2000"+i);
            domain.setIsActiveSuf(false);
            domain.setIsEnable(false);
            domain.setOwnerUser(userIsAdmin);
            domainRepository.save(domain);
        }
    }
    @After
    public void testAfter(){
        domainRepository.deleteAll();
        userRepository.deleteAll();
    }
    /**
     *
     * 获取企业列表,数量是否相等
     */
     @Test      //企业列表,数量总数是否相等（管理员）
     public void test_getdomainlistbyadmin_case1() throws Exception {
         String token=adminlogin();
         //获取企业列表
         String domainList=mockMvc.perform(
                 get("/domain/list?keyword=&page=1&size=300")
                 .header("Suf-Token", token))
                 .andReturn().getResponse().getContentAsString();
         Result result1 = JSON.parseObject(domainList, Result.class);
         Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
         JSONArray array = (JSONArray) result1.getResult();
         //获取企业列表总数
         String domainCount=mockMvc.perform(
                 get("/domain/listcount?keyword=")
                         .header("Suf-Token", token))
                 .andReturn().getResponse().getContentAsString();

         Result result2 = JSON.parseObject(domainCount, Result.class);
         Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
         Integer count= (Integer)result2.getResult();
         Assert.assertEquals(array.size(),count.intValue());
     }
    @Test       //第一页（管理员）
    public void test_getdomainlistbyadmin_case2() throws Exception {
        String token=adminlogin();
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=&page=1&size=25")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result.getResult();
        Assert.assertEquals(array.size(), 25);
    }

    @Test       //最后一页列表与数量是否相等（管理员）
    public void test_getdomainlistbyadmin_case3() throws Exception {
        String token=adminlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=&page=8&size=25")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        int lastPageCount=count-25*7;
        Assert.assertEquals(array.size(),lastPageCount);
    }

    @Test       //根据关键字查询列表与数量是否相等（管理员）
    public void test_getdomainlistbyadmin_case4() throws Exception{
        String token=adminlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=Admin&page=1&size=300")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=Admin")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        Assert.assertEquals(array.size(),count.intValue());
    }

    @Test       //关键字不存在（管理员）
    public void test_getdomainlistbyadmin_case5() throws Exception{
        String token=adminlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=nothing&page=1&size=300")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=nothing")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        Assert.assertEquals(array.size()==0,count.intValue()==0);
    }
        //企业列表,数量总数是否相等（非管理员）
    @Test
    public void test_getdomainlistbyTestUser_case1() throws Exception {
         String token=userlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=&page=1&size=300")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        System.out.println(count);
        Assert.assertEquals(array.size(),count.intValue());
    }
        //第一页（非管理员）
    @Test
    public void test_getdomainlistbyTestUser_case2() throws Exception {
        String token=userlogin();
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=&page=1&size=25")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result.getResult();
        Assert.assertEquals(array.size(), 25);
    }
        //最后一页列表与数量是否相等（非管理员）
    @Test
    public void test_getdomainlistbyTestUser_case3() throws Exception {
        String token=userlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=&page=4&size=25")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        int lastPageCount=count-25*3;
        Assert.assertEquals(array.size(),lastPageCount);
    }
        //根据关键字查询列表与数量是否相等（非管理员）
    @Test
    public void test_getdomainlistbyTestUser_case4() throws Exception{
        String token=userlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=Admin&page=1&size=300")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=Admin")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        Assert.assertEquals(array.size(),count.intValue());
    }
        //关键字不存在（非管理员）
    @Test
    public void test_getdomainlistbyTestUser_case5() throws Exception{
        String token=userlogin();
        //获取企业列表
        String domainList=mockMvc.perform(
                get("/domain/list?keyword=nothing&page=1&size=300")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();
        Result result1 = JSON.parseObject(domainList, Result.class);
        Assert.assertEquals(result1.getStateCode(), SystemStateCode.OK.getCode());
        JSONArray array = (JSONArray) result1.getResult();
        //获取企业列表总数
        String domainCount=mockMvc.perform(
                get("/domain/listcount?keyword=nothing")
                        .header("Suf-Token", token))
                .andReturn().getResponse().getContentAsString();

        Result result2 = JSON.parseObject(domainCount, Result.class);
        Assert.assertEquals(result2.getStateCode(), SystemStateCode.OK.getCode());
        Integer count= (Integer)result2.getResult();
        Assert.assertEquals(array.size()==0,count.intValue()==0);
    }
        //用户登录过期
//    @Test
//    public void test_getdomainlistWithLoginOverdue_case() throws Exception {
//        String rellist=mockMvc.perform(
//                get("/domain/list?keyword=Admin&page=1&size=300")
//                        )
//                .andReturn().getResponse().getContentAsString();
//        Result result1 = JSON.parseObject(rellist, Result.class);
//        Assert.assertEquals(result1.getStateCode(), DomainSystemStateCode.Login_Overdue.getCode());
//    }

    /**
     *
     * 添加企业域
     */
    //用户登录过期
    //用户已禁用
    @Test
    public void test_createdomain_case1() throws Exception {
        String token=userlogin();
        Optional<User> userOptional=userRepository.findById("18052502");
        if(userOptional.isPresent()){
            User user=userOptional.get();
            user.setIsEnable(false);
            userRepository.save(user);
        }
        JSONObject domainNotAdmin=getJSONDomain();
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.User_NotEnable.getCode());
    }
    //企业名称为空
    @Test
    public void test_createdomain_case2() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        domainNotAdmin.remove("domainName");
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.DomainName_IsEmpty.getCode());
    }
    //企业简称为空
    @Test
    public void test_createdomain_case3() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        domainNotAdmin.remove("domainShortName");
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.DomainShortName_IsEmpty.getCode());
    }
    //联系地址为空
    @Test
    public void test_createdomain_case4() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        domainNotAdmin.remove("address");
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Address_IsEmpty.getCode());
    }
    //联系人为空
    @Test
    public void test_createdomain_case5() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        domainNotAdmin.remove("linkMan");
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.LinkMan_IsEmpty.getCode());
    }
    //联系电话为空
    @Test
    public void test_createdomain_case6() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        domainNotAdmin.remove("linkManMobile");
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.LinkManMobile_IsEmpty.getCode());
    }
    //企业域已存在
    @Test
    public void test_createdomain_case7() throws Exception {
        String token=userlogin();
        JSONObject domainNotAdmin=getJSONDomain();
        String responseBody=mockMvc.perform(
                post("/domain/create")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(domainNotAdmin.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Domain_IsExist.getCode());
    }

    /**
     *
     * 修改企业域
     */
    //企业id为空
    @Test
    public void test_modifydomain_case1() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.put("rowId","");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Domain_IsNotExist.getCode());
    }
    //企业名称为空
    @Test
    public void test_modifydomain_case2() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.remove("domainName");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.DomainName_IsEmpty.getCode());
    }
    //企业简称为空
    @Test
    public void test_modifydomain_case3() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.remove("domainShortName");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.DomainShortName_IsEmpty.getCode());
    }
    //联系地址为空
    @Test
    public void test_modifydomain_case4() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.remove("address");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Address_IsEmpty.getCode());
    }
    //联系人为空
    @Test
    public void test_modifydomain_case5() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.remove("linkMan");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.LinkMan_IsEmpty.getCode());
    }
    //联系电话为空
    @Test
    public void test_modifydomain_case6() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        jsondomain.remove("linkManMobile");
        String responseBody=mockMvc.perform(
                post("/domain/modify")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.LinkManMobile_IsEmpty.getCode());
    }
    //企业域不存在
    @Test
    public void test_modifydomain_case7() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
            String responseBody=mockMvc.perform(
                    post("/domain/modify")
                            .header("Suf-Token",token)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsondomain.toJSONString()))
                    .andReturn().getResponse().getContentAsString();
            Result result=JSON.parseObject(responseBody,Result.class);
            Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Domain_IsNotExist.getCode());
    }
    /**
     *
     * 启用/禁用企业域
     */
    //用户登录过期
    //企业域不存在
    @Test
    public void test_setenabledomain_case1() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
            jsondomain.put("isEnable",true);
            jsondomain.remove("rowId");
            String responseBody=mockMvc.perform(
                    post("/domain/setenable")
                            .header("Suf-Token",token)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsondomain.toJSONString()))
                    .andReturn().getResponse().getContentAsString();
            Result result=JSON.parseObject(responseBody,Result.class);
            Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Domain_IsNotExist.getCode());
    }
    //是否启用不能为空
    @Test
    public void test_setenabledomain_case2() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=getJSONDomain();
        String responseBody=mockMvc.perform(
                post("/domain/setenable")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.IsEnable_IsEmpty.getCode());
    }

    //非管理员启用自己创建的企业域
    @Test
    public void test_setenabledomain_case3() throws Exception {
        String token=userlogin();
        JSONObject jsondomain=new JSONObject();
        jsondomain.put("rowId","20001");
        jsondomain.put("isEnable",true);
        String responseBody=mockMvc.perform(
                post("/domain/setenable")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Domain domain=(Domain) result.getResult();
        Boolean isEnable=domain.getIsEnable();
        Assert.assertEquals(isEnable,true);
    }
    //管理员启用他人创建的企业域
    @Test
    public void test_setenabledomain_case4() throws Exception {
        String token=adminlogin();

        JSONObject jsondomain=new JSONObject();
        jsondomain.put("rowId","20001");
        jsondomain.put("isEnable",false);
        String responseBody=mockMvc.perform(
                post("/domain/setenable")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsondomain.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Domain domain=(Domain) result.getResult();
        Boolean isEnable=domain.getIsEnable();
        Assert.assertEquals(isEnable,true);
    }
    /**
     *
     *企业域实施人员变更
     */
    //成功操作
    @Test
    public void test_changeuser_case1() throws Exception {
        String token=adminlogin();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("domainId","20001");
        jsonObject.put("newOwnerId","18052501");
        String responseBody=mockMvc.perform(
                post("/domain/changeuser")
                    .header("Suf-Token",token)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonObject.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        JSONObject domain=(JSONObject)result.getResult();
        Assert.assertEquals(domain.getString("ownerId"),"18052501");
    }
    //用户登录过期
    //企业域不存在
    @Test
    public void test_changeuser_case3() throws Exception {
        String token=adminlogin();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("domainId","200011");
        jsonObject.put("newOwnerId","18052501");
        String responseBody=mockMvc.perform(
                post("/domain/changeuser")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonObject.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.Domain_IsNotExist);
    }
    //要变更的实施人员不存在
    @Test
    public void test_changeuser_case4() throws Exception {
        String token=adminlogin();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("domainId","20001");
        jsonObject.put("newOwnerId","18052503");
        String responseBody=mockMvc.perform(
                post("/domain/changeuser")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonObject.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.NewOwnerUserId_IsEmpty);
    }
    //操作用户非管理员
    @Test
    public void test_changeuser_case5() throws Exception {
        String token=adminlogin();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("domainId","20001");
        jsonObject.put("newOwnerId","18052502");
        String responseBody=mockMvc.perform(
                post("/domain/changeuser")
                        .header("Suf-Token",token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonObject.toJSONString()))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(responseBody,Result.class);
        Assert.assertEquals(result.getStateCode(),DomainSystemStateCode.No_Permissions);
    }
    /**
     *
     *获取已/未开通suf企业列表
     */
    //用户登录过期
    //已开通suf列表，数量总数是否相等（管理员）
    @Test
    public void test_activesuflist_case1() throws Exception {
        String token=adminlogin();
        String activeSufList=mockMvc.perform(
                get("/domain/activesuflist?keword=&page=1&size=300")
                .header("Suf-Token",token))
                .andReturn().getResponse().getContentAsString();
        Result result=JSON.parseObject(activeSufList,Result.class);
        Assert.assertEquals(result.getStateCode(),SystemStateCode.OK.getCode());
        JSONArray jsonArray=(JSONArray)result.getResult();
        
    }
    //最后一页列表与数量是否相等（已开通）（管理员）
    //根据关键字查询列表与数量是否相等（已开通）（管理员）
    //关键字不存在（已开通）（管理员）
    //未开通suf列表，数量总数是否相等（管理员）
    //最后一页列表与数量是否相等（未开通）（管理员）
    //根据关键字查询列表与数量是否相等（未开通）（管理员）
    //关键字不存在（未开通）（管理员）

    //已开通suf列表，数量总数是否相等（非管理员）
    //最后一页列表与数量是否相等（已开通）（非管理员）
    //根据关键字查询列表与数量是否相等（已开通）（非管理员）
    //关键字不存在（已开通）（非管理员）
    //未开通suf列表，数量总数是否相等（非管理员）
    //最后一页列表与数量是否相等（未开通）（非管理员）
    //根据关键字查询列表与数量是否相等（未开通）（非管理员）
    //关键字不存在（未开通）（非管理员）

    //管理员登录
    private  String adminlogin() throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("rowid", "18052501");
        requestBody.put("username", "admin");
        String sufToken= tokenService.grantToken(requestBody);
        return sufToken;
    }
    //非管理员登录
    private String userlogin() throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("rowid","18052502");
        requestBody.put("username", "UserTest");
        String sufToken = tokenService.grantToken(requestBody);
        return sufToken;
    }
    //企业域
    private JSONObject getJSONDomain(){
        JSONObject domainNotAdmin=new JSONObject();
        domainNotAdmin.put("rowId","20001");
        domainNotAdmin.put("domainName","domainByUser1");
        domainNotAdmin.put("domainShortName","dbUser1");
        domainNotAdmin.put("address","乌托邦");
        domainNotAdmin.put("linkMan","晋武陵人");
        domainNotAdmin.put("linkManMobile","20001");
        domainNotAdmin.put("isActiveSuf",false);
        domainNotAdmin.put("isAdmin",false);
        domainNotAdmin.put("ownerUseId",18052502);
        return  domainNotAdmin;
    }

}
