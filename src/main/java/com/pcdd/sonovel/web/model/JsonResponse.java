package com.pcdd.sonovel.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JsonResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> JsonResponse<T> ok(T data) {
        return new JsonResponse<>(200, "OK", data);
    }

    public static <T> JsonResponse<T> error(int code, String message) {
        return new JsonResponse<>(code, message, null);
    }

}