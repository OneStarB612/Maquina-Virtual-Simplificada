
package com.mycompany.simple.vm;

public class VMExecutionException extends RuntimeException {

    private final int instructionAddress;

    public VMExecutionException(
            String message,
            int instructionAddress,
            Throwable cause) {

        super(message, cause);
        this.instructionAddress = instructionAddress;
    }

    public int getInstructionAddress() {
        return instructionAddress;
    }
}

