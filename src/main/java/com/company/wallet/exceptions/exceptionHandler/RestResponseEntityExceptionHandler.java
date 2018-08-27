package com.company.wallet.exceptions.exceptionHandler;

import com.company.wallet.exceptions.ErrorMessage;
import com.company.wallet.exceptions.WalletException;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

/**
 *  Exception handler to create custom error messages for user
 *
 *  @author Elena Medvedeva
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value={ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorDetails> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String bodyOfResponse = String.format(ErrorMessage.ARGUMENT_TYPE_MISMATCH,ex.getName(),ex.getRequiredType().getName());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), bodyOfResponse,request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value={  DataIntegrityViolationException .class })
    public ResponseEntity<ErrorDetails> handleDataIntegrityViolationException (
            DataIntegrityViolationException  ex, WebRequest request) {
        Throwable rootCause = Throwables.getRootCause(ex);
        String error = rootCause.getMessage();
        String errorMessage = ErrorMessage.generateErrorMessageForDataIntegrityViolationException(error);
        ErrorDetails errorDetails = new ErrorDetails(new Date(), errorMessage,request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value
            = { IllegalArgumentException.class })
    protected ResponseEntity<Object> handleConflict(
            IllegalArgumentException ex, WebRequest request) {
        logger.error(ex.toString());
        ex.printStackTrace();
        String bodyOfResponse = ex.toString();
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }


    @ExceptionHandler(value
            = { WalletException.class })
    protected ResponseEntity<ErrorDetails> handleWalletException(
            WalletException ex, WebRequest request) {
        logger.error(ex.toString());
        HttpStatus status = HttpStatus.valueOf(ex.getErrorCode());
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),request.getDescription(false));
        return new ResponseEntity<>(errorDetails, status);
    }


    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        super.handleExceptionInternal(ex,body,headers,status, request);
        logger.error(ex.toString());
        String message = (body != null)?body.toString(): ex.getMessage();
        ErrorDetails errorDetails = new ErrorDetails(new Date(),message,request.getDescription(false));
        return new ResponseEntity<>(errorDetails, status);

    }

}