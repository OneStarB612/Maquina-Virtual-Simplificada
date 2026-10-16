package com.mycompany.simple.vm.devices;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeyboardController implements DeviceController {

    private final Queue<Integer> inputQueue =
            new ConcurrentLinkedQueue<>();

    @Override
    public String getDeviceName() {
        return "Keyboard";
    }

    /**
     * Recibe un valor desde la interfaz gráfica o desde las pruebas.
     * @param value
     */
    public void submitInput(int value) {
        inputQueue.offer(value);
    }

    /**
     * Alias para pruebas existentes.
     * @param value
     */
    public void queueInput(int value) {
        submitInput(value);
    }

    /**
     * No bloquea el hilo de ejecución.
     * @return 
     */
    public int read() {
        Integer value = inputQueue.poll();

        if (value == null) {
            throw new InputRequiredException();
        }

        return value;
    }

    public boolean hasPendingInput() {
        return !inputQueue.isEmpty();
    }

    @Override
    public void reset() {
        inputQueue.clear();
    }

    public void clear() {
        reset();
    }
}
