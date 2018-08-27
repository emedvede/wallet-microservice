package com.company.wallet.exceptions;
/**
 *  Enum to store error codes
 *
 *  @author Elena Medvedeva
 */
public enum ErrorCode {
    BadRequest(400),
    NotFound(404),
    InternalServerError(500);

    int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    ErrorCode(int code) {
        this.code = code;
    }


}
