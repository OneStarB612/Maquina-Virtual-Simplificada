package com.mycompany.simple.vm.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Keyboard Controller simulating standard user input.
 * Implements the DeviceController interface.
 * Supports queuing predefined values for test automation.
 */
public class KeyboardController implements DeviceController {
    private final List<Integer> inputQueue = new ArrayList<>();
    private Scanner scanner;

    @Override
    public String getDeviceName() {
        return "Keyboard";
    }

    @Override
    public void reset() {
        inputQueue.clear();
    }

    /**
     * Clears any queued inputs.
     */
    public void clear() {
        reset();
    }

    /**
     * Queues a value to be read next. Useful for automated unit testing.
     * @param value The integer value to enqueue.
     */
    public void queueInput(int value) {
        inputQueue.add(value);
    }

    /**
     * Reads a value. Retrieves the next queued value if available;
     * otherwise, prompts the user for real-time terminal input.
     * @return The integer read.
     */
    public int read() {
        if (!inputQueue.isEmpty()) {
            return inputQueue.remove(0);
        }
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        System.out.print("[KEYBOARD INPUT] Enter an integer: ");
        return scanner.nextInt();
    }
}
