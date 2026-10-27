
package com.henrique.implantahub.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.Arrays;
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

    @ExceptionHandler(InvalidClientSortException.class)
    public ProblemDetail handleInvalidSort(InvalidClientSortException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more sort properties are not supported."
        );

        problemDetail.setTitle("Invalid Sort Property");
        problemDetail.setProperty(
                "allowedSortProperties",
                ClientService.SORTABLE_PROPERTIES
        );

        return problemDetail;
    }

    // Valores que não podem ser convertidos para o tipo esperado
    // (ex.: status=FOO ou /api/clients/abc). Sem este handler, o Spring
    // responderia com seu corpo de erro padrão, fora do formato ProblemDetail.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has an invalid value."
        );

        problemDetail.setTitle("Invalid Parameter");

        Class<?> requiredType = ex.getRequiredType();

        if (requiredType != null && requiredType.isEnum()) {
            problemDetail.setProperty(
                    "allowedValues",
                    Arrays.stream(requiredType.getEnumConstants())
                            .map(Object::toString)
                            .toList()
            );
        }

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