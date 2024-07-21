package com.movieflex.exceptions;

public class FileExcistsException extends RuntimeException{
    public FileExcistsException(String message) {
        super(message);
    }
}
