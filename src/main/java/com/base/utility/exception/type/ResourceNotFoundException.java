package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class ResourceNotFoundException extends BusinessException{
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with identifier '%s' not found", resource, identifier),
                Map.of("resource", resource, "identifier", identifier));
    }

    public ResourceNotFoundException(ErrorCode errorCode, String resource, String identifier) {
        super(errorCode,
                String.format("%s with identifier '%s' not found", resource, identifier),
                Map.of("resource", resource, "identifier", identifier));
    }
}
