package com.mycompany.simple.vm.devices;

import java.util.ArrayList;
import java.util.List;

public class PrinterController implements DeviceController {

    private final List<Integer> outputHistory = new ArrayList<>();

    @Override
    public String getDeviceName() {
        return "Printer";
    }

    public synchronized void write(int value) {
        outputHistory.add(value);
    }

    public synchronized List<Integer> getOutputHistory() {
        return new ArrayList<>(outputHistory);
    }

    @Override
    public synchronized void reset() {
        outputHistory.clear();
    }

    public void clear() {
        reset();
    }
}

