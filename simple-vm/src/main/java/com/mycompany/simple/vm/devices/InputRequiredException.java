
package com.mycompany.simple.vm.devices;

public class InputRequiredException extends RuntimeException {

    public InputRequiredException() {
        super("La instrucción INPUT necesita un valor.");
    }
}

