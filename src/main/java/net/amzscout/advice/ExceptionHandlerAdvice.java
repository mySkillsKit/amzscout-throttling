package net.amzscout.advice;

import lombok.NonNull;
import net.amzscout.exception.RequestLimitException;
import net.amzscout.model.ErrorMsg;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(RequestLimitException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorMsg requestLimitExceptionHandler(@NonNull RequestLimitException exc) {
        return new ErrorMsg(exc.getMessage(), exc.getId());
    }
}