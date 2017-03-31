package com.stream.client.exception;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class CancelledException extends Exception {

    public CancelledException() {
        super("canceled");
    }
}
