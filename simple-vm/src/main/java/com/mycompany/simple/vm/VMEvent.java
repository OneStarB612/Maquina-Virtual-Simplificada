
package com.mycompany.simple.vm;

public final class VMEvent {

    public enum Type {
    RESET,
    PROGRAM_LOADED,
    INSTRUCTION_EXECUTED,
    OUTPUT,
    INPUT_REQUIRED,
    STOPPED,
    HALTED,
    ERROR,
    STATE_CHANGED
    }

public enum State {
    READY,
    RUNNING,
    PAUSED,
    WAITING_FOR_INPUT,
    HALTED,
    ERROR
    }

    private final Type type;
    private final State state;
    private final String message;
    private final int pc;
    private final int ir;
    private final int acc;
    private final boolean zeroFlag;
    private final Throwable error;

    public VMEvent(
            Type type,
            State state,
            String message,
            int pc,
            int ir,
            int acc,
            boolean zeroFlag,
            Throwable error) {

        this.type = type;
        this.state = state;
        this.message = message;
        this.pc = pc;
        this.ir = ir;
        this.acc = acc;
        this.zeroFlag = zeroFlag;
        this.error = error;
    }

    public Type getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public int getPC() {
        return pc;
    }

    public int getIR() {
        return ir;
    }

    public int getACC() {
        return acc;
    }

    public boolean isZeroFlag() {
        return zeroFlag;
    }

    public Throwable getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}

