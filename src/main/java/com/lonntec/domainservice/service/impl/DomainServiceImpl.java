package com.lonntec.domainservice.service.impl;

import com.lonntec.domainservice.entity.Domain;
import com.lonntec.domainservice.entity.User;
import com.lonntec.domainservice.lang.DomainSystemException;
import com.lonntec.domainservice.lang.DomainSystemStateCode;
import com.lonntec.domainservice.proxy.CodeRuleService;
import com.lonntec.domainservice.repository.DomainRepository;
import com.lonntec.domainservice.repository.UserRepository;
import com.lonntec.domainservice.service.DomainService;
import com.lonntec.framework.lang.MicroServiceException;
import com.lonntec.framework.lang.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional(rollbackOn = {MicroServiceException.class, RuntimeException.class})
@Service
public class DomainServiceImpl implements DomainService{

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    CodeRuleService codeRuleService;

    @Autowired
    UserRepository userRepository;
    //添加企业域
    @Override
    public Domain appendDomain(Domain domain) {
        //用户逻辑 登录是否过期，是否禁用
        if(UserContext.getCurrentUserContext() == null){
            throw new DomainSystemException(DomainSystemStateCode.OwnerUserId_IsEmpty);
        }
        String ownerId=UserContext.getCurrentUserContext().properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(ownerId);
        if(!userOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Login_Overdue);
        }else if (userOptional.get().getIsEnable()==false){
            throw new DomainSystemException(DomainSystemStateCode.User_NotEnable);
        }
        //判断企业名称,企业简称,联系地址,联系人,联系人电话是否为空
        if(domain.getDomainName()==null||domain.getDomainName().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.DomainName_IsEmpty);
        }
        if(domain.getDomainShortName()==null||domain.getDomainShortName().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.DomainShortName_IsEmpty);
        }
        if(domain.getAddress()==null||domain.getAddress().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.Address_IsEmpty);
        }
        if(domain.getLinkMan()==null||domain.getLinkMan().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.LinkMan_IsEmpty);
        }
        if(domain.getLinkManMobile()==null||domain.getLinkManMobile().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.LinkManMobile_IsEmpty);
        }
        //判断企业域是否已存在
        Optional<Domain> domainOptional = domainRepository.findByDomainName(domain.getDomainName());
        if(domainOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsExist);
        }
        //完善企业域信息
        domain.setOwnerUser(userOptional.get());
        domain.setDomainNumber(codeRuleService.generateCode("domainNumberRule"));
        return  domainRepository.save(domain);
    }
    //根据关键字查询企业分页列表
    @Override
    public List<Domain> findDomains(String keyword, Integer page, Integer size){
        //判断用户登录是否过期,是否禁用
        UserContext currCtx = UserContext.getCurrentUserContext();
        String ownerId = currCtx.properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(ownerId);
        if(!userOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Login_Overdue);
        }else if (userOptional.get().getIsEnable()==false){
            throw new DomainSystemException(DomainSystemStateCode.User_NotEnable);
        }
        Integer queryPage = page==null || page <= 0 ? 1 : page;
        Integer querySize = size==null || size <= 0 ? 25 : size;
        PageRequest pageable = new PageRequest(queryPage -1, querySize);
        String queryKeywork = keyword== null || keyword.replaceAll("\\s*","").equals("") ? "%" : keyword;
        if(!queryKeywork.contains("%")){
            queryKeywork = "%" + queryKeywork + "%";
        }
        if(userOptional.get().getIsAdmin()==true){
            return domainRepository.findAllByMyQuery(queryKeywork, pageable);
        }
        return domainRepository.findAllByMyQuery2(queryKeywork,ownerId,pageable);
    }
    //获取企业域数量
    @Override
    public Integer getListCount(String keyword) {
        UserContext currCtx = UserContext.getCurrentUserContext();
        String ownerId = currCtx.properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(ownerId);
        if(!userOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Login_Overdue);
        }else if (userOptional.get().getIsEnable()==false){
            throw new DomainSystemException(DomainSystemStateCode.User_NotEnable);
        }
        String querykeywork = keyword== null || keyword.replaceAll("\\s*","").equals("") ? "%" : keyword;
        if(!querykeywork.contains("%")){
            querykeywork = "%" + querykeywork + "%";
        }
        if(userOptional.get().getIsAdmin()==true){
            return domainRepository.countAllByMyQuery(querykeywork);
        }
        return domainRepository.countAllByMyQuery2(querykeywork,ownerId);
    }
    //修改企业域
    @Override
    public Domain modifyDomain(Domain domain) {
        //判断企业id，企业名称,企业简称,联系地址,联系人,联系人电话是否为空
        if(domain.getRowId()==null||domain.getRowId().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsNotExist);
        }
        if(domain.getDomainName()==null||domain.getDomainName().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.DomainName_IsEmpty);
        }
        if(domain.getDomainShortName()==null||domain.getDomainShortName().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.DomainShortName_IsEmpty);
        }
        if(domain.getAddress()==null||domain.getAddress().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.Address_IsEmpty);
        }
        if(domain.getLinkMan()==null||domain.getLinkMan().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.LinkMan_IsEmpty);
        }
        if(domain.getLinkManMobile()==null||domain.getLinkManMobile().replaceAll("\\s*","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.LinkManMobile_IsEmpty);
        }
        //判断企业域是否存在
        Optional<Domain>  domainOptional = domainRepository.findById(domain.getRowId());
        if(!domainOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsNotExist);
        }
        //判断企业域名称是否存在
        Optional<Domain>  domainNameOptional = domainRepository.findByDomainName(domain.getDomainName());
        if(domainNameOptional.isPresent() && !(domainNameOptional.get().getRowId().equals(domain.getRowId()))){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsExist);
        }
        Domain dbDomain = domainOptional.get();
        dbDomain.setDomainName(domain.getDomainName());
        dbDomain.setDomainShortName(domain.getDomainShortName());
        dbDomain.setAddress(domain.getAddress());
        dbDomain.setLinkMan(domain.getLinkMan());
        dbDomain.setLinkManMobile(domain.getLinkManMobile());
        dbDomain.setBusinessLicense(domain.getBusinessLicense());
        dbDomain.setMemo(domain.getMemo());
        return domainRepository.save(dbDomain);

    }

    //启用禁用企业域
    @Override
    public void setEnable(String rowId, Boolean isEnable) {
        UserContext currCtx = UserContext.getCurrentUserContext();
        String ownerId = currCtx.properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(ownerId);
        if(userOptional==null){
            throw new DomainSystemException(DomainSystemStateCode.Login_Overdue);
        }
        //参数不能为空
        if(rowId==null||rowId.replaceAll("\\s","").equals("")){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsNotExist);
        }else if(isEnable==null){
            throw new DomainSystemException(DomainSystemStateCode.IsEnable_IsEmpty);
        }
        Optional<Domain>  domainOptional = domainRepository.findById(rowId);
        if(!domainOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsNotExist);
        }else if(userOptional.get().getIsAdmin()==false &&
                !(userOptional.get().getUserName().equalsIgnoreCase(domainOptional.get().getOwnerUser().getUserName()))){
            throw new DomainSystemException(DomainSystemStateCode.No_Permissions);
        }
        Domain domain=domainOptional.get();
        domain.setIsEnable(isEnable);
        domainRepository.save(domain);
    }

    //企业域实施人员变更
    @Override
    public Domain changeUser(String domainId, String newOwnerId) {
        Optional<Domain> domainOptional1 = domainRepository.findById(domainId);
        if(!domainOptional1.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Domain_IsNotExist);
        }
        UserContext currCtx = UserContext.getCurrentUserContext();
        String operateUserId = currCtx.properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(operateUserId);
        if(userOptional.get().getIsAdmin()==false){
            throw new DomainSystemException(DomainSystemStateCode.No_Permissions);
        }
        Optional<User> newUserOptional = userRepository.findById(newOwnerId);
        if(!newUserOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.NewOwnerUserId_IsEmpty);
        }
        User newUser = newUserOptional.get();
        Domain domainInfo = domainOptional1.get();
        domainInfo.setOwnerUser(newUser);
        return domainRepository.save(domainInfo);
    }

    //根据关键字查询(已/未)开通SUF企业分页列表
    @Override
    public List<Domain> findActiveSufListDomains(String keyword, Integer page, Integer size, Boolean IsActiveSuf){
        Integer queryPage = page==null || page <= 0 ? 1 : page;
        Integer querySize = size==null || size <= 0 ? 25 : size;
        PageRequest pageable = new PageRequest(queryPage -1, querySize);
        String queryKeywork = keyword== null || keyword.replaceAll("\\s*","").equals("") ? "%" : keyword;
        if(!queryKeywork.contains("%")){
            queryKeywork = "%" + queryKeywork + "%";
        }
        UserContext currCtx = UserContext.getCurrentUserContext();
        String ownerId = currCtx.properties.getString("rowid");
        Optional<User> userOptional = userRepository.findById(ownerId);
        if(!userOptional.isPresent()){
            throw new DomainSystemException(DomainSystemStateCode.Login_Overdue);
        }
        if(userOptional.get().getIsAdmin()==true){
            if(IsActiveSuf==true){
                return domainRepository.findAllByActiveDomain(queryKeywork,pageable);
            }else if(IsActiveSuf==false){
                return domainRepository.findAllByMyNotActiveDomain(queryKeywork,pageable);
            }
        }
        if(IsActiveSuf==true){
                return domainRepository.findAllByMyQueryActiveDomainOwner(queryKeywork,ownerId,pageable);
            }
        return domainRepository.findAllByMyQueryNotActiveDomainOwner(queryKeywork,ownerId,pageable);



    }
}
