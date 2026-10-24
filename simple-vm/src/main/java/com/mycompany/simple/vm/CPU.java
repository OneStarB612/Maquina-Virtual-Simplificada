package com.mycompany.simple.vm;

import com.mycompany.simple.vm.devices.InputRequiredException;

public class CPU {

    private int pc;
    private int ir;
    private int acc;
    private boolean zeroFlag;
    private boolean halted;

    public CPU() {
        reset();
    }

    public void reset() {
        pc = 0;
        ir = 0;
        acc = 0;
        zeroFlag = true;
        halted = false;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(int pc) {
        this.pc = pc;
    }

    public int getIR() {
        return ir;
    }

    public void setIR(int ir) {
        this.ir = ir;
    }

    public int getACC() {
        return acc;
    }

    public void setACC(int acc) {
        this.acc = acc;
        zeroFlag = (acc == 0);
    }

    public boolean isZeroFlag() {
        return zeroFlag;
    }

    public boolean isHalted() {
        return halted;
    }

    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    /**
     * Ejecuta un único ciclo Fetch-Decode-Execute.
     * No imprime mensajes ni controla la interfaz gráfica.
     */
    public void step(Memory memory, VirtualMachine vm) {
        if (halted) {
            return;
        }

        int instructionAddress = pc;

        try {
            // FETCH
            ir = memory.read(pc);
            pc++;

            // DECODE
            Instruction instruction =
                    Instruction.decodeInstruction(ir);

            int operand = Instruction.decodeOperand(ir);

            // EXECUTE
            switch (instruction) {
                case LOAD:
                    setACC(memory.read(operand));
                    break;

                case STORE:
                    memory.write(operand, acc);
                    break;

                case ADD:
                    setACC(acc + memory.read(operand));
                    break;

                case SUB:
                    setACC(acc - memory.read(operand));
                    break;

                case JMP:
                    validateJumpAddress(operand, memory);
                    pc = operand;
                    break;

                case JZ:
                    if (zeroFlag) {
                        validateJumpAddress(operand, memory);
                        pc = operand;
                    }
                    break;

                case INPUT:
                    setACC(vm.getKeyboard().read());
                    break;

                case OUTPUT:
                    vm.getPrinter().write(acc);
                    break;

                case HALT:
                    halted = true;
                    break;
                    
                case PRINT:
                    StringBuilder text = new StringBuilder();
                    int address = operand;
                    while (address < memory.getSize()) {
                        int character = memory.read(address++);
                        if (character == 0) {
                            break;
                        }
                        
                        text.append((char) character);
                    }
                    
                    if (address > memory.getSize()
                            || (address == memory.getSize() && text.length() > 0)) {
                        throw new IllegalArgumentException(
                                "Texto sin terminador en memoria.");
                    }
                    
                    vm.getPrinter().writeText(text.toString());
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Instrucción no implementada: "
                            + instruction);
            }

        } catch (InputRequiredException ex) {
    // La instrucción INPUT no se completó.
    // Se reintentará cuando llegue un valor.
    pc = instructionAddress;
    throw ex;
        } catch (RuntimeException ex) {
            if (ex instanceof VMExecutionException) {
                throw ex;
            }

    throw new VMExecutionException(
            "Error en la dirección "
                    + instructionAddress + ": "
                    + ex.getMessage(),
            instructionAddress,
            ex
    );
}
    }

    private void validateJumpAddress(
            int address,
            Memory memory) {

        if (address < 0 || address >= memory.getSize()) {
            throw new IllegalArgumentException(
                    "Dirección de salto fuera de rango: "
                    + address);
        }
    }
}

