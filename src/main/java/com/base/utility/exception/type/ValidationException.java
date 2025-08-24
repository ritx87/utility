package com.base.utility.exception.type;


import com.base.utility.exception.utils.ErrorCode;
import lombok.Getter;

@Getter
public class ValidationException extends BusinessException{
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }
    public ValidationException(String message, Object details) {
        super(ErrorCode.VALIDATION_FAILED, message, details);
    }

    public ValidationException(ErrorCode errorCode, String message, Object details) {
        super(errorCode, message, details);
    }
}
