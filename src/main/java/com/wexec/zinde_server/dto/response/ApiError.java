package com.wexec.zinde_server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiError {

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<FieldViolation> fieldErrors;

    @Getter
    @AllArgsConstructor
    public static class FieldViolation {
        private final String field;
        private final String message;
    }
}
