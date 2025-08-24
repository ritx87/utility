package com.base.utility.exception.type;



import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class DuplicateResourceException extends BusinessException{
    public DuplicateResourceException(String resource, String field, String value) {
        super(ErrorCode.DUPLICATE_RESOURCE,
                String.format("%s with %s '%s' already exists", resource, field, value),
                Map.of("resource", resource, "field", field, "value", value));
    }
}
