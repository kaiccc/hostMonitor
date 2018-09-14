package com.mon.host.common.rest;


import com.mon.host.common.enums.CodeMessage;

public class RestResponse<T> {
    private int code;
    private String message;
    private T content;

    public static RestResponse<String> success() {
        return success(CodeMessage.SUCCESS.getCode(), CodeMessage.SUCCESS.getMessage());
    }

    public static RestResponse<String> success(String message) {
        return success(CodeMessage.SUCCESS.getCode(), message);
    }

    public static <T> RestResponse<T> success(T content) {

        return success(CodeMessage.SUCCESS.getCode(),content);
    }

    public static RestResponse<String> isSuccess(boolean isSuccess) {
        return isSuccess?success():failed(500,"操作失败");
    }

    public static <T> RestResponse<T> success(int code, T content) {
        RestResponse<T> resp = new RestResponse<T>();
        resp.setCode(code);
        resp.setContent(content);
        return resp;
    }

    public static RestResponse<String> failed(CodeMessage codeMessage) {
        return failed(codeMessage.getCode(), codeMessage.getMessage());
    }

    public static RestResponse<String> failed(int code,String message) {
        RestResponse<String> resp = new RestResponse<String>();
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

}
