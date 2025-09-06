package com.base.utility.exception.response;

import com.base.utility.exception.utils.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseApiResponse<T>{
    private String status;
    private T data;
    private ErrorDetail error;
    private MetaData meta;

    public static <T> BaseApiResponse<T> success(T data) {
        return BaseApiResponse.<T>builder()
                .status("success")
                .data(data)
                .meta(MetaData.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static <T> BaseApiResponse<T> success(T data, String requestId) {
        return BaseApiResponse.<T>builder()
                .status("success")
                .data(data)
                .meta(MetaData.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static BaseApiResponse<Void> error(ErrorCode errorCode) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .meta(MetaData.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static BaseApiResponse<Void> error(ErrorCode errorCode, String customMessage) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(customMessage)
                        .build())
                .meta(MetaData.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static BaseApiResponse<Void> error(ErrorCode errorCode, Object details, String requestId) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .details(details)
                        .build())
                .meta(MetaData.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static BaseApiResponse<Void> error(ErrorCode errorCode, String customMessage, Object details, String requestId) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(customMessage)
                        .details(details)
                        .build())
                .meta(MetaData.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    // Backward compatibility methods
    public static BaseApiResponse<Void> error(String code, String message) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .build())
                .meta(MetaData.builder()
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static BaseApiResponse<Void> error(String code, String message, Object details, String requestId) {
        return BaseApiResponse.<Void>builder()
                .status("error")
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .meta(MetaData.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }
}
