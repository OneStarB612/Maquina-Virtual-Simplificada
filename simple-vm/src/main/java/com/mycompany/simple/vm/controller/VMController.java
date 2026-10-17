package com.mycompany.simple.vm.controller;

import com.mycompany.simple.vm.VMEvent;
import com.mycompany.simple.vm.service.VMService;
import com.mycompany.simple.vm.model.CompilationResult;
import com.mycompany.simple.vm.ui.ESFrame;
import com.mycompany.simple.vm.ui.ExeFrame;
import com.mycompany.simple.vm.ui.VMFrame;
import com.mycompany.simple.vm.ui.VMView;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class VMController {

    private final VMFrame vm;
    private final ESFrame eview;
    private final VMView rview;
    private final VMService service;
    private boolean auxiliaryWindowsShown;
    private String tablaHead = "        | Dirección |   Valor   |\n";

    public VMController(
            VMFrame vm,
            ESFrame eview,
            VMView rview,
            VMService service) {

        this.vm = vm;
        this.eview = eview;
        this.rview = rview;
        this.service = service;

        configureWindows();
        bindActions();

        service.addListener(this::handleVMEvent);

        eview.setVisible(false);
        rview.setVisible(false);

        rview.areaTable.setText(tablaHead);
        
    }

    private void configureWindows() {
        vm.setLocationRelativeTo(null);

        eview.setDefaultCloseOperation(
                javax.swing.JFrame.HIDE_ON_CLOSE);

        rview.setDefaultCloseOperation(
                javax.swing.JFrame.HIDE_ON_CLOSE);

        eview.setLocation(
                vm.getX() + vm.getWidth(),
                vm.getY());

        rview.setLocation(
                vm.getX(),
                vm.getY() + vm.getHeight());
    }

    private void bindActions() {
        vm.loadButton.addActionListener(e -> loadProgram());
        vm.clearButton.addActionListener(e -> clearProgram());
        vm.runButton.addActionListener(e -> runProgram());
        vm.stepButton.addActionListener(e -> executeStep());
        vm.stopButton.addActionListener(e -> stopProgram());
        vm.resetButton.addActionListener(e -> resetProgram());

        eview.submitInputButton.addActionListener(
                e -> submitInput());

        vm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.close();
                eview.dispose();
                rview.dispose();
            }
        });
    }

    private void loadProgram() {
        CompilationResult result =
                service.loadProgram(vm.programArea.getText());

        appendLog(result.getSummary());

        if (!result.isSuccessful()) {
            result.getErrors().forEach(this::appendLog);
            return;
        }

        appendLog("Programa cargado correctamente.");
        updateControls(service.getState());
    }

    private void clearProgram() {
        vm.programArea.setText("");
        appendLog("Editor limpiado.");
        
    }

    private void runProgram() {
        if (!showAuxiliaryWindows()) {
            return;
        }

        service.runAsync();
    }

    private void executeStep() {
        if (!showAuxiliaryWindows()) {
            return;
        }

        service.step();
    }

    private void stopProgram() {
        service.stop();
    }

    private void resetProgram() {
        service.reset();
        eview.outputArea.setText("");
        eview.logArea.setText("");
        updateControls(service.getState());
        rview.areaTable.setText(tablaHead);
    }

    private void submitInput() {
        String text = eview.inputField.getText().trim();

        if (text.isEmpty()) {
            appendLog("Introduce un número entero.");
            return;
        }

        try {
            int value = Integer.parseInt(text);

            service.submitInput(value);
            eview.inputField.setText("");

        } catch (NumberFormatException ex) {
            appendLog("Entrada inválida: debe ser un número entero.");
        }
    }

    private boolean showAuxiliaryWindows() {
        if (auxiliaryWindowsShown) {
            eview.setVisible(true);
            rview.setVisible(true);
            return true;
        }

        if (service.getState() != VMEvent.State.READY
                && service.getState() != VMEvent.State.PAUSED) {
            appendLog("Carga un programa antes de ejecutarlo.");
            return false;
        }

        auxiliaryWindowsShown = true;

        eview.setLocation(
                vm.getX() + vm.getWidth(),
                vm.getY());

        rview.setLocation(
                vm.getX(),
                vm.getY() + vm.getHeight());

        eview.setVisible(true);
        rview.setVisible(true);

        return true;
    }

    private void handleVMEvent(VMEvent event) {
        SwingUtilities.invokeLater(() -> {
            updateRegisters(event);
            updateControls(event.getState());
            updateIO(event);
            appendLog(event.getMessage());

            switch (event.getType()) {
    case PROGRAM_LOADED:
        updateMemory();
        break;

    case RESET:
        updateMemory();
        break;

    case MEMORY_MODIFIED:
        updateMemory();
        break;

    case HALTED:
        appendLog("Ejecución finalizada.");
        break;

    case ERROR:
        appendLog("Error: " + event.getMessage());
        break;

    default:
        break;
}
        });
    }

    private void updateRegisters(VMEvent event) {
        rview.irValue.setText(String.valueOf(event.getIR()));
        rview.accValue.setText(String.valueOf(event.getACC()));
        rview.zeroFlagValue.setText(String.valueOf(event.isZeroFlag()));
        rview.pcValue.setText(String.valueOf(event.getPC()));
        
    }
    
    
    private void updateMemory() {
    StringBuilder memory = new StringBuilder();
    int memorySize = service.getMemorySize();

    for (int address = 0; address < memorySize; address++) {
        int value = service.readMemory(address);

        if (value != 0) {
            memory.append("        | ")
                  .append(address)
                  .append(" | ")
                  .append(value)
                  .append(" |")
                  .append(System.lineSeparator());
        }
    }

    rview.areaTable.append(memory.toString());
}

    private void updateIO(VMEvent event) {
        switch (event.getType()) {
            case OUTPUT:
                eview.appendOutput(event.getACC());
                break;

            case ERROR:
                eview.appendLog(
                        "Error: " + event.getMessage());
                break;

            default:
                break;
        }
    }

    private void updateControls(VMEvent.State state) {
        boolean running = state == VMEvent.State.RUNNING;
        boolean finished = state == VMEvent.State.HALTED
                || state == VMEvent.State.ERROR;

        vm.runButton.setEnabled(!running && !finished);
        vm.stepButton.setEnabled(!running && !finished);
        vm.stopButton.setEnabled(running);
        vm.resetButton.setEnabled(!running);

        rview.statusLabel.setText(state.name());
    }

    private void appendLog(String message) {
        if (message != null && !message.isBlank()) {
            eview.appendLog(message);
        }
    }
}
