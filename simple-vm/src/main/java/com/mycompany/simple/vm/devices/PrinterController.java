package com.mycompany.simple.vm.devices;

import java.util.ArrayList;
import java.util.List;

/**
 * Printer Controller simulating screen/paper output.
 * Implements the DeviceController interface.
 * Logs output history for verification and test assertions.
 */
public class PrinterController implements DeviceController {
    private final List<Integer> outputHistory = new ArrayList<>();

    @Override
    public String getDeviceName() {
        return "Printer";
    }

    @Override
    public void reset() {
        outputHistory.clear();
    }

    /**
     * Clears output history.
     */
    public void clear() {
        reset();
    }

    /**
     * Simulates printing a value to standard output.
     * @param value The value to print.
     */
    public void write(int value) {
        outputHistory.add(value);
        System.out.println("[+++PRINTER OUTPUT]: " + value);
    }

    /**
     * Retrieves a copy of the printed history.
     * @return List of integers representing printed history.
     */
    public List<Integer> getOutputHistory() {
        return new ArrayList<>(outputHistory);
    }
}
