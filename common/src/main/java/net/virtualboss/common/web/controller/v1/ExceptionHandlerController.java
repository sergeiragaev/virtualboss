package net.virtualboss.common.web.controller.v1;

import lombok.extern.slf4j.Slf4j;
import net.virtualboss.common.exception.AccessDeniedException;
import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.web.dto.error.ErrorDetails;
import net.virtualboss.common.web.dto.error.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(CircularLinkingException.class)
    public ResponseEntity<ErrorResponse> notFound(CircularLinkingException ex) {
        log.error("There is error occurred while trying link tasks", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getLocalizedMessage(), new ArrayList<>()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(EntityNotFoundException ex) {
        log.error("There is error occurred while trying to get entity", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getLocalizedMessage(), new ArrayList<>()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> notValid(MethodArgumentNotValidException ex) {

        log.error("There is error occurred", ex);

        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String errorMessage = String.join("\n", errorMessages);

        List<ErrorDetails> detailsList = new ArrayList<>();
        for (ObjectError objectError : bindingResult.getAllErrors()) {
            String fieldName = Objects.requireNonNull(objectError.getCodes())[1];
            fieldName = fieldName.substring(fieldName.indexOf(".")+1);
            detailsList.add(new ErrorDetails(fieldName, objectError.getDefaultMessage()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, detailsList));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> alreadyExistsHandler(AlreadyExistsException ex) {
        log.error("There is error occurred while trying to create new/update entity", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getLocalizedMessage(), new ArrayList<>()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedHandler(AccessDeniedException ex) {
        log.error("There is error occurred while trying to update entity", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getLocalizedMessage(), new ArrayList<>()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandled(Exception ex) {

        log.error("There is error occurred", ex);

        String errorMessage = ex.getLocalizedMessage();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorMessage, new ArrayList<>()));
    }
}
