package com.mycompany.simple.vm.service;

import com.mycompany.simple.vm.VMEvent;
import com.mycompany.simple.vm.VMListener;
import com.mycompany.simple.vm.VirtualMachine;
import com.mycompany.simple.vm.model.CompilationResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VMService implements AutoCloseable {

    private final VirtualMachine vm;
    private final CompilationService compilationService;
    private final ExecutorService executor;

    private boolean resumeAfterInput;
    private boolean runScheduledOrRunning;
    private boolean closed;

    public VMService() {
        vm = new VirtualMachine();
        compilationService = new CompilationService();

        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(
                    runnable,
                    "vm-execution-thread"
            );
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompilationResult loadProgram(String source) {
        synchronized (this) {
            if (runScheduledOrRunning) {
                throw new IllegalStateException(
                        "No se puede cargar un programa mientras "
                                + "la VM está ejecutándose.");
            }
            ensureOpen();
        }

        CompilationResult result =
                compilationService.compile(source);

        if (!result.isSuccessful()) {
            return result;
        }

        try {
            vm.loadProgram(source);
            return result;
        } catch (RuntimeException ex) {
            return new CompilationResult(
                    false,
                    0,
                    Collections.singletonList(ex.getMessage())
            );
        }
    }

    /**
     * Ejecuta una instrucción.
     */
    public void step() {
        synchronized (this) {
            ensureOpen();

            if (runScheduledOrRunning) {
                throw new IllegalStateException(
                        "La VM está ejecutándose.");
            }
        }

        vm.step();
    }

    /**
     * Ejecuta el programa fuera del hilo de Swing.
     */
    public Future<?> runAsync() {
        synchronized (this) {
            ensureOpen();

            if (runScheduledOrRunning) {
                return null;
            }

            runScheduledOrRunning = true;

            return executor.submit(this::executeAndRelease);
        }
    }

    private void executeAndRelease() {
        try {
            vm.run();
        } 
finally {
    synchronized (this) {
        runScheduledOrRunning = false;

        boolean waiting =
                vm.getState() == VMEvent.State.WAITING_FOR_INPUT;

        boolean resume = !closed
                && waiting
                && vm.getKeyboard().hasPendingInput();

        resumeAfterInput = !closed && waiting && !resume;

        if (resume) {
            resumeAfterInput = false;
            runScheduledOrRunning = true;
            executor.execute(this::executeAndRelease);
        }
    }
}

    }
    
    public void stop() {
    vm.stop();
}

    /**
    * Envía el dato a INPUT y ejecuta únicamente esa instrucción.
     * @param value
    */

    public void submitInput(int value) {
        boolean resumeAutomatically;

        synchronized (this) {
            ensureOpen();

            if (runScheduledOrRunning) {
                throw new IllegalStateException(
                    "La VM está ejecutándose.");
            }

            if (vm.getState() != VMEvent.State.WAITING_FOR_INPUT) {
                throw new IllegalStateException(
                        "La VM no está esperando una entrada.");
            }

            resumeAutomatically = resumeAfterInput;
            resumeAfterInput = false;
        }

        vm.submitInput(value);
        vm.step();

        if (resumeAutomatically
                && vm.getState() == VMEvent.State.PAUSED) {
            runAsync();
        }
    }
    
    public void reset() {
        synchronized (this) {
            ensureOpen();

            if (runScheduledOrRunning) {
                throw new IllegalStateException(
                        "Espera a que termine la ejecución "
                                + "antes de reiniciar la VM.");
            }
        }

        vm.reset();
    }
    

    public void addListener(VMListener listener) {
        vm.addListener(listener);
    }

    public void removeListener(VMListener listener) {
        vm.removeListener(listener);
    }

    public VMEvent.State getState() {
        return vm.getState();
    }

    public Throwable getLastError() {
        return vm.getLastError();
    }

    public int getPC() {
        return vm.getCpu().getPC();
    }

    public int getIR() {
        return vm.getCpu().getIR();
    }

    public int getACC() {
        return vm.getCpu().getACC();
    }

    public boolean isZeroFlag() {
        return vm.getCpu().isZeroFlag();
    }

    public boolean isHalted() {
        return vm.isHalted();
    }

    public int readMemory(int address) {
        return vm.getMemory().read(address);
    }

    public int getMemorySize() {
        return vm.getMemory().getSize();
    }

    /**
     * Acceso temporal para pruebas y operaciones aún no abstraídas.
     * @return 
     */
    public VirtualMachine getVirtualMachine() {
        return vm;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "VMService ya está cerrado.");
        }
    }
    
    public List<Integer> getOutputHistory() {
    return (List<Integer>) vm.getPrinter().getOutputHistory();
}

    @Override
    public void close() {
        synchronized (this) {
            closed = true;
        }

        executor.shutdownNow();
    }
}
