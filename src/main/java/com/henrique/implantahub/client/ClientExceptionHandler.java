
package com.henrique.implantahub.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(assignableTypes = ClientController.class)
public class ClientExceptionHandler {

    @ExceptionHandler(DuplicateCnpjException.class)
    public ProblemDetail handleDuplicateCnpj(DuplicateCnpjException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "A client with this CNPJ already exists."
        );

        problemDetail.setTitle("Duplicate CNPJ");

        return problemDetail;
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ProblemDetail handleClientNotFound(ClientNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Client not found."
        );

        problemDetail.setTitle("Client Not Found");

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more fields are invalid."
        );

        problemDetail.setTitle("Validation Failed");

        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors
                    .computeIfAbsent(
                            fieldError.getField(),
                            key -> new ArrayList<>()
                    )
                    .add(fieldError.getDefaultMessage());
        }

        problemDetail.setProperty("errors", fieldErrors);

        return problemDetail;
    }
}