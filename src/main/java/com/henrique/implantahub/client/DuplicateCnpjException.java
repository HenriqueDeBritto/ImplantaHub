package com.henrique.implantahub.client;

public class DuplicateCnpjException extends RuntimeException {

    public DuplicateCnpjException(String cnpj) {
        super("A client with CNPJ " + cnpj + " already exists.");
    }
}