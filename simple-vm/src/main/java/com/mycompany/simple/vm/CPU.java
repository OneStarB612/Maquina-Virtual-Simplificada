package com.mycompany.simple.vm;

/**
 * Simulates the Central Processing Unit (CPU) of the Simple Virtual Machine.
 * It contains registers PC, IR, ACC, and a zeroFlag.
 * It coordinates the Fetch-Decode-Execute instruction cycle.
 */
public class CPU {
    private int pc;       // Program Counter: points to next instruction address
    private int ir;       // Instruction Register: holds the current fetched instruction word
    private int acc;      // Accumulator: general purpose data register
    private boolean zeroFlag; // Status flag set to true if the last operation result was zero
    private boolean halted;   // CPU state representing if execution has halted

    public CPU() {
        reset();
    }

    public void reset() {
        this.pc = 0;
        this.ir = 0;
        this.acc = 0;
        this.zeroFlag = true;
        this.halted = false;
    }

    // Getters and Setters for Registers
    public int getPC() { return pc; }
    public void setPC(int pc) { this.pc = pc; }

    public int getIR() { return ir; }
    public void setIR(int ir) { this.ir = ir; }

    public int getACC() { return acc; }
    public void setACC(int acc) { 
        this.acc = acc; 
        this.zeroFlag = (acc == 0);
    }

    public boolean isZeroFlag() { return zeroFlag; }
    public boolean isHalted() { return halted; }
    public void setHalted(boolean halted) { this.halted = halted; }

    /**
     * Executes a single Fetch-Decode-Execute cycle on the given memory.
     * @param memory The memory instance to interact with.
     * @param vm The orchestrator VirtualMachine containing device controllers.
     */
    public void step(Memory memory, VirtualMachine vm) {
        if (halted) {
            System.out.println("CPU is currently halted. Reset the VM to run a new program.");
            return;
        }

        // 1. FETCH
        System.out.println("\n--- [FETCH STAGE] ---");
        System.out.printf("PC = %d, Fetching instruction...\n", pc);
        this.ir = memory.read(pc);
        System.out.printf("Instruction word fetched: %05d\n", ir);
        this.pc++; // Increment PC after fetch

        // 2. DECODE
        System.out.println("--- [DECODE STAGE] ---");
        Instruction instr = Instruction.decodeInstruction(ir);
        int operand = Instruction.decodeOperand(ir);
        System.out.printf("Decoded: Opcode = %d (%s), Operand = %d\n", instr.getOpcode(), instr.getMnemonic(), operand);

        // 3. EXECUTE
        System.out.println("--- [EXECUTE STAGE] ---");
        switch (instr) {
            case LOAD:
                // Load from memory into ACC
                setACC(memory.read(operand));
                System.out.printf("LOAD: Loaded memory address %d value %d into ACC.\n", operand, getACC());
                break;

            case STORE:
                // Store ACC value in memory
                memory.write(operand, getACC());
                System.out.printf("STORE: Stored ACC value %d into memory address %d.\n", getACC(), operand);
                break;

            case ADD:
                // Add value from memory to ACC
                int addValue = memory.read(operand);
                int oldAddAcc = getACC();
                setACC(oldAddAcc + addValue);
                System.out.printf("ADD: Added memory address %d value %d to ACC (%d -> %d).\n", operand, addValue, oldAddAcc, getACC());
                break;

            case SUB:
                // Subtract value from memory from ACC
                int subValue = memory.read(operand);
                int oldSubAcc = getACC();
                setACC(oldSubAcc - subValue);
                System.out.printf("SUB: Subtracted memory address %d value %d from ACC (%d -> %d).\n", operand, subValue, oldSubAcc, getACC());
                break;

            case JMP:
                // Unconditional branch
                setPC(operand);
                System.out.printf("JMP: Jumped unconditionally to address %d.\n", operand);
                break;

            case JZ:
                // Jump if ACC is zero (or zeroFlag is true)
                if (isZeroFlag()) {
                    setPC(operand);
                    System.out.printf("JZ: ACC is zero. Jumped to address %d.\n", operand);
                } else {
                    System.out.printf("JZ: ACC is %d (non-zero). No jump taken. Continuing sequentially.\n", getACC());
                }
                break;

            case INPUT:
                // Read from user input (keyboard) into ACC
                int inputVal = vm.getKeyboard().read();
                setACC(inputVal);
                System.out.printf("INPUT: Read value %d from Keyboard into ACC.\n", inputVal);
                break;

            case OUTPUT:
                // Write ACC value to simulated output (printer)
                vm.getPrinter().write(getACC());
                System.out.printf("OUTPUT: Sent ACC value %d to Printer.\n", getACC());
                break;

            case HALT:
                // Halt the execution
                this.halted = true;
                System.out.println("HALT: Terminating CPU execution cycle.");
                break;

            default:
                throw new UnsupportedOperationException("Opcode execution not implemented for: " + instr);
        }

        // Print final status registers at the end of execution
        System.out.printf("STATUS AFTER STEP: ACC = %d, PC = %d, IR = %05d, ZeroFlag = %b, Halted = %b\n",
                getACC(), getPC(), getIR(), isZeroFlag(), isHalted());
    }
}
