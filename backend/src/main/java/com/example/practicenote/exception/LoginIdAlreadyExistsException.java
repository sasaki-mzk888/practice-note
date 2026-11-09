package com.example.practicenote.exception;

public class LoginIdAlreadyExistsException extends RuntimeException {
    public LoginIdAlreadyExistsException() {
        super("このログインIDはすでに使われています");
    }
}
