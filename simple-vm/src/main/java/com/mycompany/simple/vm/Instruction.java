package com.mycompany.simple.vm;

/**
 * Defines the Instruction Set Architecture (ISA) for the Simple Virtual Machine.
 * It contains the supported opcodes, syntax names, and utility methods for 
 * encoding assembly strings into numeric words and decoding them.
 */
public enum Instruction {
    LOAD(1, "LOAD", true),
    STORE(2, "STORE", true),
    ADD(3, "ADD", true),
    SUB(4, "SUB", true),
    JMP(5, "JMP", true),
    JZ(6, "JZ", true),
    INPUT(7, "INPUT", false),
    OUTPUT(8, "OUTPUT", false),
    HALT(9, "HALT", false),
    PRINT(10, "PRINT", true);

    private final int opcode;
    private final String mnemonic;
    private final boolean requiresOperand;

    Instruction(int opcode, String mnemonic, boolean requiresOperand) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.requiresOperand = requiresOperand;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public boolean requiresOperand() {
        return requiresOperand;
    }

    /**
     * Resolves an Instruction from a numeric opcode.
     */
    public static Instruction fromOpcode(int opcode) {
        for (Instruction instr : values()) {
            if (instr.opcode == opcode) {
                return instr;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: " + opcode);
    }

    /**
     * Resolves an Instruction from its string mnemonic (case-insensitive).
     */
    public static Instruction fromMnemonic(String mnemonic) {
        for (Instruction instr : values()) {
            if (instr.mnemonic.equalsIgnoreCase(mnemonic)) {
                return instr;
            }
        }
        throw new IllegalArgumentException("Unknown mnemonic: " + mnemonic);
    }

    /**
     * Encodes an instruction and an optional operand into a single 32-bit integer word.
     * We use a decimal representation (Opcode * 10000 + Operand) for human-readability
     * when examining raw memory, or a bitwise representation. Let's use the decimal
     * method because it is highly standard for educational simulators (e.g. LOAD 12 -> 10012).
     */
    public static int encode(Instruction instr, int operand) {
        if (operand < 0 || operand >= Memory.SIZE) {
            throw new IllegalArgumentException("Operand " + operand + " out of memory bounds.");
        }
        return (instr.getOpcode() * 10000) + operand;
    }

    /**
     * Encodes an instruction without an operand (defaults to operand = 0).
     */
    public static int encode(Instruction instr) {
        return instr.getOpcode() * 10000;
    }

    /**
     * Decodes the opcode part of an encoded memory word.
     */
    public static Instruction decodeInstruction(int word) {
        int opcode = word / 10000;
        return fromOpcode(opcode);
    }

    /**
     * Decodes the operand part of an encoded memory word.
     */
    public static int decodeOperand(int word) {
        return word % 10000;
    }
}
