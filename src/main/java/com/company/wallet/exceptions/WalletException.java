package com.company.wallet.exceptions;

/**
 * Custom wallet exception
 *
 * @author Elena Medvedeva
 */
public class WalletException extends Exception{
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public WalletException(String message, int errorCode){
        super(message);
        this.errorCode = errorCode;
    }

    public WalletException(){
        super();

    }

    public WalletException(String message){
        super(message);
    }

    public WalletException(Exception e){
        super(e);
    }


}
