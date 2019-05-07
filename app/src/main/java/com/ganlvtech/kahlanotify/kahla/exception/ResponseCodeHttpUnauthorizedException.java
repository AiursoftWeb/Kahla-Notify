package com.ganlvtech.kahlanotify.kahla.exception;

public class ResponseCodeHttpUnauthorizedException extends Throwable {
    @Override
    public String getMessage() {
        return "HTTP 401 Unauthorized";
    }
}
