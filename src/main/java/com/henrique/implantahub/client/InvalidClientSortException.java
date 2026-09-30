package com.henrique.implantahub.client;

public class InvalidClientSortException extends RuntimeException {

    public InvalidClientSortException(String property) {
        super("Clients cannot be sorted by '" + property + "'.");
    }
}
