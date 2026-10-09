package com.mycompany.simple.vm;

/**
 * Simulates the Virtual Memory of the Simple Virtual Machine.
 * It has a size of 4096 words, where each word is a standard 32-bit integer.
 * It provides robust boundary checking, and read/write operations.
 */
public class Memory {
    public static final int SIZE = 4096;
    private final int[] ram;

    public Memory() {
        this.ram = new int[SIZE];
    }

    /**
     * Reads a value from the specified memory address.
     * @param address The 12-bit address (0 to 4095).
     * @return The integer value stored at the address.
     */
    public int read(int address) {
        validateAddress(address);
        return ram[address];
    }

    /**
     * Writes a value to the specified memory address.
     * @param address The 12-bit address (0 to 4095).
     * @param value The integer value to write.
     */
    public void write(int address, int value) {
        validateAddress(address);
        ram[address] = value;
    }

    /**
     * Returns the total size of the memory.
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * Clears all memory locations to 0.
     */
    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            ram[i] = 0;
        }
    }

    private void validateAddress(int address) {
        if (address < 0 || address >= SIZE) {
            throw new IllegalArgumentException("Segmentation Fault: Memory address " + address + " is out of bounds (0-" + (SIZE - 1) + ").");
        }
    }
}
