
package com.henrique.implantahub.client;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long id) {
        super("Client with ID " + id + " was not found.");
    }
}