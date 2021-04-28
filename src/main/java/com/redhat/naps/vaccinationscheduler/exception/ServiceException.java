package com.redhat.naps.vaccinationscheduler.exception;

public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = -5225036823279004410L;

    public ServiceException(String message) {
        super(message);
    }

}
