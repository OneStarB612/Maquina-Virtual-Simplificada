package com.mycompany.simple.vm;

import com.mycompany.simple.vm.devices.DiskController;
import com.mycompany.simple.vm.devices.KeyboardController;
import com.mycompany.simple.vm.devices.PrinterController;
import com.mycompany.simple.vm.devices.InputRequiredException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Locale;

public class VirtualMachine {

    private static final int DEFAULT_MAX_STEPS = 1000;

    private final CPU cpu;
    private final Memory memory;
    private final KeyboardController keyboard;
    private final PrinterController printer;
    private final DiskController disk;
    private volatile boolean stopRequested;
    private volatile VMEvent.State state = VMEvent.State.READY;

    private final List<VMListener> listeners =
            new CopyOnWriteArrayList<>();

    private Throwable lastError;

    public VirtualMachine() {
        cpu = new CPU();
        memory = new Memory();
        keyboard = new KeyboardController();
        printer = new PrinterController();
        disk = new DiskController();
    }

    public CPU getCpu() {
        return cpu;
    }

    public Memory getMemory() {
        return memory;
    }

    public KeyboardController getKeyboard() {
        return keyboard;
    }

    public PrinterController getPrinter() {
        return printer;
    }

    public DiskController getDisk() {
        return disk;
    }

    public VMEvent.State getState() {
        return state;
    }

    public Throwable getLastError() {
        return lastError;
    }

    public boolean hasError() {
        return state == VMEvent.State.ERROR;
    }

    public boolean isHalted() {
        return cpu.isHalted();
    }

    public void addListener(VMListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(VMListener listener) {
        listeners.remove(listener);
    }

    private void publish(
            VMEvent.Type type,
            String message,
            Throwable error) {

        VMEvent event = new VMEvent(
                type,
                state,
                message,
                cpu.getPC(),
                cpu.getIR(),
                cpu.getACC(),
                cpu.isZeroFlag(),
                error
        );

        for (VMListener listener : listeners) {
    try {
        listener.onVMEvent(event);
    } catch (RuntimeException ex) {
        // Un listener defectuoso no debe detener la máquina.
        ex.printStackTrace();
    }
}
    }

    public void reset() {
        cpu.reset();
        memory.clear();
        keyboard.clear();
        printer.clear();
        disk.clear();

        lastError = null;
        state = VMEvent.State.READY;

        publish(
                VMEvent.Type.RESET,
                "Máquina virtual reiniciada.",
                null
        );
    }
    
    public void stop() {
    stopRequested = true;

    // Si la ejecución ya estaba suspendida por INPUT,
    // no hay un hilo de ejecución activo que pueda detenerla.
    if (state == VMEvent.State.WAITING_FOR_INPUT) {
        state = VMEvent.State.PAUSED;

        publish(
                VMEvent.Type.STOPPED,
                "Ejecución detenida por el usuario.",
                null
        );
    }
}

    /**
     * Analiza primero el programa en memoria temporal.
     * La memoria actual solo se reemplaza si el análisis termina bien.
     */
    public void loadProgram(String source) {
        try {
            int[] image = assemble(source);

            reset();

            for (int address = 0; address < image.length; address++) {
                memory.write(address, image[address]);
            }

            state = VMEvent.State.READY;

            publish(
                    VMEvent.Type.PROGRAM_LOADED,
                    "Programa cargado. Instrucciones: "
                            + image.length,
                    null
            );

        } catch (RuntimeException ex) {
            lastError = ex;
            state = VMEvent.State.ERROR;

            publish(
                    VMEvent.Type.ERROR,
                    "No se pudo cargar el programa: "
                            + ex.getMessage(),
                    ex
            );

            throw ex;
        }
    }

    private int[] assemble(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El programa está vacío.");
        }

        String[] lines = source.split("\\R", -1);
        int[] image = new int[Memory.SIZE];
        int address = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNumber = i + 1;

            if (line.isEmpty()
                    || line.startsWith("/")
                    || line.startsWith("#")
                    || line.startsWith(";")) {
                continue;
            }

            if (line.contains(":")) {
                line = line.substring(
                        line.indexOf(':') + 1).trim();
            }

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String mnemonic = parts[0].toUpperCase(Locale.ROOT);

            try {
                Instruction instruction =
                        Instruction.fromMnemonic(mnemonic);

                boolean requiresOperand =
                        instruction.requiresOperand();

                if (requiresOperand && parts.length != 2) {
                    throw new IllegalArgumentException(
                            "La instrucción requiere exactamente "
                            + "un operando.");
                }

                if (!requiresOperand && parts.length != 1) {
                    throw new IllegalArgumentException(
                            "La instrucción no acepta operandos.");
                }

                int operand = 0;

                if (requiresOperand) {
                    operand = Integer.parseInt(parts[1]);

                    if (operand < 0 || operand >= Memory.SIZE) {
                        throw new IllegalArgumentException(
                                "Operando fuera de rango: " + operand);
                    }
                }

                if (address >= Memory.SIZE) {
                    throw new IllegalArgumentException(
                            "El programa supera la capacidad de memoria.");
                }

                image[address] =
                        Instruction.encode(instruction, operand);

                address++;

            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Línea " + lineNumber + ": "
                                + ex.getMessage(),
                        ex
                );
            }
        }

        if (address == 0) {
            throw new IllegalArgumentException(
                    "No se encontraron instrucciones.");
        }

        int[] program = new int[address];
        System.arraycopy(image, 0, program, 0, address);

        return program;
    }

    /**
     * Ejecuta una sola instrucción y notifica el nuevo estado.
     */
    public void step() {
        executeStep(false);
    }

    private void executeStep(boolean running) {
    if (state == VMEvent.State.ERROR
            || state == VMEvent.State.HALTED
            || cpu.isHalted()) {
        return;
    }

    try {
        cpu.step(memory, this);

        if (cpu.isHalted()) {
            state = VMEvent.State.HALTED;

            publish(
                    VMEvent.Type.HALTED,
                    "La ejecución terminó correctamente.",
                    null
            );
            return;
        }

        state = running
                ? VMEvent.State.RUNNING
                : VMEvent.State.PAUSED;

        Instruction executed =
                Instruction.decodeInstruction(cpu.getIR());

        VMEvent.Type eventType =
                executed == Instruction.OUTPUT
                        ? VMEvent.Type.OUTPUT
                        : VMEvent.Type.INSTRUCTION_EXECUTED;

        publish(
                eventType,
                "Ejecutada: " + executed.getMnemonic(),
                null
        );
    } catch (InputRequiredException ex) {
        state = VMEvent.State.WAITING_FOR_INPUT;
        publish(
                VMEvent.Type.INPUT_REQUIRED,
                "Se requiere un valor entero para continuar.",
                null
        );
    } catch (RuntimeException ex) {
        lastError = ex;
        state = VMEvent.State.ERROR;
        publish(
                VMEvent.Type.ERROR,
                ex.getMessage(),
                ex
        );
    }

    
}

    /**
     * Ejecución síncrona.
     * No debe llamarse desde el hilo de eventos de Swing.
     */
    public void run() {
        run(DEFAULT_MAX_STEPS);
    }

    /**
     * Ejecuta hasta HALT, error o límite de seguridad.
     */
    public void run(int maxSteps) {
        if (maxSteps < 1) {
            throw new IllegalArgumentException(
                    "maxSteps debe ser mayor que cero.");
        }

        if (state == VMEvent.State.ERROR
                || state == VMEvent.State.HALTED) {
            return;
        }

        state = VMEvent.State.RUNNING;

        publish(
                VMEvent.Type.STATE_CHANGED,
                "Ejecución iniciada.",
                null
        );
        
        stopRequested = false;
        state = VMEvent.State.RUNNING;

        int steps = 0;

        while (!stopRequested
        && state == VMEvent.State.RUNNING
        && !cpu.isHalted()
        && steps < maxSteps) {

    executeStep(true);
    steps++;
}
        
        if (stopRequested
        && state == VMEvent.State.RUNNING) {

    state = VMEvent.State.PAUSED;

    publish(
            VMEvent.Type.STOPPED,
            "Ejecución detenida por el usuario.",
            null
    );
    return;
}

        if (state == VMEvent.State.RUNNING
                && !cpu.isHalted()
                && steps >= maxSteps) {

            VMExecutionException error =
                    new VMExecutionException(
                            "Se alcanzó el límite de "
                            + maxSteps + " instrucciones.",
                            cpu.getPC(),
                            null
                    );

            lastError = error;
            state = VMEvent.State.ERROR;

            publish(
                    VMEvent.Type.ERROR,
                    error.getMessage(),
                    error
            );
        }
    }
    

/**
 * Recibe una entrada y permite reanudar la ejecución
 * desde VMService.
 */
public void submitInput(int value) {
    keyboard.submitInput(value);
}
}
