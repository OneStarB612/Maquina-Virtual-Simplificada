package com.mycompany.simple.vm;

import com.mycompany.simple.vm.devices.DiskController;
import com.mycompany.simple.vm.devices.KeyboardController;
import com.mycompany.simple.vm.devices.PrinterController;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Orchestrator class simulating the entire Simple Virtual Machine hardware.
 * It houses the CPU, Memory, and the three simulated device controllers:
 * Keyboard, Disk, and Printer.
 * 
 * Version 2: Integrated with top-level standalone DeviceController implementations.
 */
public class VirtualMachine {
    private final CPU cpu;
    private final Memory memory;
    private final KeyboardController keyboard;
    private final PrinterController printer;
    private final DiskController disk;

    public VirtualMachine() {
        this.cpu = new CPU();
        this.memory = new Memory();
        this.keyboard = new KeyboardController();
        this.printer = new PrinterController();
        this.disk = new DiskController(this);
    }

    public CPU getCpu() { return cpu; }
    public Memory getMemory() { return memory; }
    public KeyboardController getKeyboard() { return keyboard; }
    public PrinterController getPrinter() { return printer; }
    public DiskController getDisk() { return disk; }

    /**
     * Resets the entire VM to clean state.
     */
    public void reset() {
        cpu.reset();
        memory.clear();
        keyboard.clear();
        printer.clear();
        disk.clear();
    }

    /**
     * Loads a raw assembly program into memory starting at address 0.
     * Parses instructions line by line.
     * @param assemblyText The multi-line assembly program string.
     */
    public void loadProgram(String assemblyText) {
        reset();
        try (BufferedReader reader = new BufferedReader(new StringReader(assemblyText))) {
            String line;
            int currentAddress = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines or comments
                if (line.isEmpty() || line.startsWith("/") || line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }

                // Handle label if present (e.g. "LABEL: LOAD 10")
                if (line.contains(":")) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                }

                String[] parts = line.split("\\s+");
                String mnemonic = parts[0].toUpperCase();
                Instruction instr = Instruction.fromMnemonic(mnemonic);

                int operand = 0;
                if (instr.requiresOperand()) {
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("Syntax Error: Instruction " + mnemonic + " requires an operand.");
                    }
                    operand = Integer.parseInt(parts[1]);
                }

                int encodedWord = Instruction.encode(instr, operand);
                memory.write(currentAddress, encodedWord);
                currentAddress++;
            }
            System.out.println("Program loaded successfully into memory. Used words: " + currentAddress);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load program: " + e.getMessage(), e);
        }
    }

    /**
     * Runs the loaded program step-by-step until the CPU halts.
     */
    public void run() {
        System.out.println("Starting Simple Virtual Machine Execution...");
        int stepCount = 0;
        while (!cpu.isHalted()) {
            stepCount++;
            System.out.printf("\n=== VM STEP %d ===", stepCount);
            cpu.step(memory, this);
            if (stepCount > 1000) { // Infinite loop protector
                System.err.println("Execution Error: Maximum execution step count (1000) exceeded. Halting CPU.");
                cpu.setHalted(true);
                break;
            }
        }
        System.out.println("\nExecution Finished.");
    }
}
