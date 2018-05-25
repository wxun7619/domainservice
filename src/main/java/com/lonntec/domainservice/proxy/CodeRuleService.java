package com.lonntec.domainservice.proxy;

import com.lonntec.framework.annotation.MicroServiceMethodProxy;
import com.lonntec.framework.lang.CustomerStateCode;
import com.lonntec.framework.lang.MicroServiceException;
import com.lonntec.framework.lang.RequestType;
import org.springframework.stereotype.Service;

@Service
public class CodeRuleService {

    @MicroServiceMethodProxy(microServiceKey  = "codeRuleService", path = "/coderule/generate", type = RequestType.POST)
    public String generateCode(String ruleKey){
        throw new MicroServiceException(new CustomerStateCode(-3, ""));
    }
}
