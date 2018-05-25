package com.lonntec.domainservice.lang;


import com.lonntec.framework.lang.MicroServiceException;
import com.lonntec.framework.lang.StateCode;

public class DomainSystemException extends MicroServiceException {

    public DomainSystemException(StateCode stateCode) {
        super(stateCode);
    }

    public DomainSystemException(StateCode stateCode, String message) {
        super(stateCode, message);
    }

    public DomainSystemException(StateCode stateCode, Throwable cause) {
        super(stateCode, cause);
    }

    public DomainSystemException(StateCode stateCode, String message, Throwable cause) {
        super(stateCode, message, cause);
    }
}
