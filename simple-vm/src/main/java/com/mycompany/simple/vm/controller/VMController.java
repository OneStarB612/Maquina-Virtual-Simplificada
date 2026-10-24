package com.mycompany.simple.vm.controller;

import com.mycompany.simple.vm.VMEvent;
import com.mycompany.simple.vm.service.VMService;
import com.mycompany.simple.vm.model.CompilationResult;
import com.mycompany.simple.vm.service.ProgramFileService;
import com.mycompany.simple.vm.ui.ConsoleFrame;
import com.mycompany.simple.vm.ui.RegFrame;
import com.mycompany.simple.vm.ui.VMFrame;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;

public class VMController {

    private final VMFrame vm;
    private final ConsoleFrame console;
    private final RegFrame rview;
    private final VMService service;
    private final String tablaHead = "        | Dirección |   Valor   |\n";
    private final ProgramFileService programFileService;
    private File currentFile;
    private boolean initializingExamples;

    public VMController(
            VMFrame vm,
            ConsoleFrame console,
            RegFrame rview,
            VMService service) {

        this.vm = vm;
        this.console = console;
        this.rview = rview;
        this.service = service;

        configureWindows();
        bindActions();

        service.addListener(this::handleVMEvent);

        console.setVisible(false);
        rview.setVisible(false);
        rview.areaTable.setText(tablaHead);
        
        this.programFileService = new ProgramFileService(
        service.getVirtualMachine().getDisk());

        initializeExamples();
    }
    
    private void configureWindows() {
        vm.setLocationRelativeTo(null);

        console.setDefaultCloseOperation(
                javax.swing.JFrame.HIDE_ON_CLOSE);

        rview.setDefaultCloseOperation(
                javax.swing.JFrame.HIDE_ON_CLOSE);

        console.setLocation(
                vm.getX() + vm.getWidth(),
                vm.getY());

        rview.setLocation(
                vm.getX(),
                vm.getY() + vm.getHeight());
    }

    private void bindActions() {
        vm.loadButton.addActionListener(e -> loadProgram());
        vm.clearButton.addActionListener(e -> clearProgram());
        rview.runButton.addActionListener(e -> runProgram());
        rview.stepButton.addActionListener(e -> executeStep());
        rview.stopButton.addActionListener(e -> stopProgram());
        rview.resetButton.addActionListener(e -> resetProgram());
        vm.openButton.addActionListener(e -> openProgram());
        vm.saveButton.addActionListener(e -> saveProgram());
        vm.exampleBox.addActionListener(e -> loadSelectedExample());

        console.addSubmitListener(e -> submitInput());
        
        vm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.close();
                console.dispose();
                rview.dispose();
            }
        });
    }

    private void initializeExamples() {
        initializingExamples = true;

        try {
            programFileService.saveExample("Cargar", 
                    """
                    ; cargar un valor en ACC
                    LOAD numero
                    OUTPUT
                    HALT
                
                    numero: DATA 25
                    """);

            programFileService.saveExample("Guardar",
                    """
                    ; guardar ACC en memoria
                    LOAD numero
                    STORE resultado
                    LOAD resultado
                    OUTPUT
                    HALT
                    
                    numero: DATA 30
                    resultado: DATA 0
                    """);

            programFileService.saveExample("Sumar",
                    """
                    ; sumar un valor a ACC
                    LOAD numero1
                    ADD numero2
                    OUTPUT
                    HALT
                    
                    numero1: DATA 10
                    numero2: DATA 20
                    """);

            programFileService.saveExample("Restar",
                    """
                    ; restar un valor a ACC
                    LOAD numero1
                    SUB numero2
                    OUTPUT
                    HALT
                    
                    numero1: DATA 30
                    numero2: DATA 10
                    """);

            programFileService.saveExample("Saltar",
                    """
                    ; saltar una instruccion
                    JMP destino
                    LOAD ignorado
                    destino: LOAD numero
                    OUTPUT
                    HALT
                    
                    ignorado: DATA 99
                    numero: DATA 15
                    """);
            
            programFileService.saveExample("Cero",
                    """
                    ; saltar si ACC es cero
                    LOAD cero
                    JZ destino
                    LOAD alternativa
                    OUTPUT
                    HALT
                    destino: LOAD correcto
                    OUTPUT
                    HALT
                    
                    cero: DATA 0
                    alternativa: DATA 5
                    correcto: DATA 9
                    """);

            programFileService.saveExample("Entrada",
                    """
                    ; recibir un valor por teclado
                    INPUT
                    OUTPUT
                    HALT
                    """);

            programFileService.saveExample("Salida",
                    """
                    ; mostrar un valor
                    LOAD numero
                    OUTPUT
                    HALT
                    
                    numero: DATA 123
                    """);

            programFileService.saveExample("Terminar",
                    """
                    ; terminar ejecucion
                    LOAD numero
                    OUTPUT
                    HALT
                    
                    numero: DATA 7
                    """);
            
            programFileService.saveExample("Memoria",
                    """
                    ; sumar dos numeros desde memoria
                    LOAD numero1
                    ADD numero2
                    OUTPUT
                    HALT
                                                      
                    numero1: DATA 5
                    numero2: DATA 7
                    """);

            programFileService.saveExample("Teclado",
                    """
                    ; sumar dos numeros ingresados por teclado
                    INPUT
                    STORE 20
                    INPUT
                    ADD 20
                    OUTPUT
                    HALT
                    """);

            programFileService.saveExample("Impresora",
                    """
                    ; recibir un dato, guardarlo e imprimirlo
                    INPUT
                    STORE 20
                    LOAD 20
                    OUTPUT
                    HALT
                    """);
            
            programFileService.saveExample("Menu",
                    """
                    ; Menu de operaciones
                    inicio: PRINT menu
                    INPUT
                    STORE opcion
                    
                    ; Opcion 1: sumar
                    LOAD opcion
                    SUB uno
                    JZ sumar
                    
                    ; Opcion 2: restar
                    LOAD opcion
                    SUB dos
                    JZ restar
                    
                    ; Opcion 3: salir
                    LOAD opcion
                    SUB tres
                    JZ salir
                    
                    ; Opcion invalida
                    PRINT invalida
                    JMP inicio
                    
                    sumar: PRINT mensaje1
                    INPUT
                    STORE numero1
                    INPUT
                    ADD numero1
                    OUTPUT
                    JMP inicio
                    
                    restar: PRINT mensaje2
                    INPUT
                    STORE numero1
                    INPUT
                    STORE numero2
                    LOAD numero1
                    SUB numero2
                    OUTPUT
                    JMP inicio
                    
                    salir: PRINT despedida
                    HALT
                    
                    ; Textos
                    menu: TEXT "\\n=== MENU ===\\n1. Sumar dos numeros\\n2. Restar dos numeros\\n3. Salir\\nElige una opcion:"
                    mensaje1: TEXT  "\\nIntroduce el primer numero y luego el segundo:"
                    mensaje2: TEXT  "\\nIntroduce el primer numero y luego el segundo:"
                    invalida: TEXT  "\\nOpcion invalida. Intenta de nuevo."
                    despedida: TEXT  "\\nPrograma finalizado."
                    
                    ; Datos
                    opcion: DATA 0
                    uno: DATA 1
                    dos: DATA 2
                    tres: DATA 3
                    numero1: DATA 0
                    numero2: DATA 0
                    """);
        } finally {
            initializingExamples = false;
        }
    }
    
    private void loadSelectedExample() {
        if (initializingExamples) {
            return;
        }

        String selected = (String) vm.exampleBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        try {
            String source = programFileService.loadExample(selected);

            vm.programArea.setText(source);
            vm.programArea.setCaretPosition(0);
            currentFile = null;
        
            appendLog("Ejemplo \"" + selected
                    + "\" cargado desde el disco simulado.");

        } catch (RuntimeException ex) {
            appendLog("Error al leer el disco simulado: "
                    + ex.getMessage());
        }
    }

    private void openProgram() {
        try {
            String content = programFileService.openFile(
                    vm.openChoose, vm
            );

            if (content == null) {
                return;
            }

            vm.programArea.setText(content);
            vm.programArea.setCaretPosition(0);
            currentFile = vm.openChoose.getSelectedFile();

            appendLog("Archivo leído desde el sistema de archivos: "
                    + currentFile.getName());

        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    vm,
                    "No se pudo abrir el archivo:\n" + ex.getMessage(),
                    "Error al abrir",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void saveProgram() {
        try {
            File saved = programFileService.saveFile(
                    vm.openChoose,
                    vm,
                    vm.programArea.getText(),
                    currentFile
            );

            if (saved != null) {
                currentFile = saved;
                appendLog("Archivo guardado: " + saved.getName());
            }
            
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    vm,
                    "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error al guardar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadProgram() {
        CompilationResult result =
                service.loadProgram(vm.programArea.getText());

        appendLog(result.getSummary());
        
        if (!showAuxiliaryWindows()) {
            return;
        }

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
        try {
            service.reset();
            console.setInputEnabled(false);
            updateControls(service.getState());
            rview.areaTable.setText(tablaHead);
        } catch (IllegalStateException ex) {
            console.appendLine(
                    "No se pudo reiniciar: " + ex.getMessage());
        }
    }
    private void submitInput() {
        if (service.getState() != VMEvent.State.WAITING_FOR_INPUT) {
            console.appendLine(
                    "La máquina virtual no está esperando una entrada.");
            return;
        }

        String text = console.getInputText();

        if (text.isBlank()) {
            console.appendLine("Introduce un número entero.");
            return;
        }
        
        try {
            int value = Integer.parseInt(text);

            service.submitInput(value);
            
            console.appendLine("> " + value);
            console.clearInput();
            console.setInputEnabled(false);
        
        } catch (NumberFormatException ex) {
            console.appendLine(
                    "Entrada inválida: debe ser un número entero.");

        } catch (IllegalStateException ex) {
            console.appendLine(
                    "No se pudo enviar la entrada: " + ex.getMessage());
        }
    }
    
    private boolean showAuxiliaryWindows() {
        VMEvent.State state = service.getState();

        if (state != VMEvent.State.READY
                && state != VMEvent.State.PAUSED
                && state != VMEvent.State.WAITING_FOR_INPUT) {
            
            console.appendLine(
                    "Carga un programa válido antes de ejecutarlo.");
            return false;
        }


        console.setLocation(
                vm.getX() + vm.getWidth(),
                vm.getY());

        rview.setLocation(
                vm.getX(),
                vm.getY() + vm.getHeight());
        
        console.setVisible(true);
        rview.setVisible(true);

        return true;
    }
    
    private void handleVMEvent(VMEvent event) {
        SwingUtilities.invokeLater(() -> {
            updateRegisters(event);
            updateControls(event.getState());
            updateIO(event);
            
            switch (event.getType()) {
                case PROGRAM_LOADED:
                    updateMemory();
                    console.appendLine(event.getMessage());
                    break;

                case RESET:
                    updateMemory();
                    console.clearHistory();
                    console.appendLine(event.getMessage());
                    break;

                case MEMORY_MODIFIED:
                    updateMemory();
                    console.appendLine(event.getMessage());
                    break;

                case INPUT_REQUIRED:
                    console.appendLine(
                            "Entrada requerida: introduce un entero.");
                    console.setInputEnabled(true);
                    break;

                case OUTPUT:
                    // El valor se presenta en updateIO().
                    console.appendLine(event.getMessage());
                    break;

                case HALTED:
                    console.setInputEnabled(false);
                    console.appendLine(event.getMessage());
                    console.appendLine("Ejecución finalizada.");
                    break;

                    case ERROR:
                    console.setInputEnabled(false);
                    console.appendLine(
                            "Error: " + event.getMessage());
                    break;

                case STOPPED:
                    console.appendLine(event.getMessage());
                    break;
                    
                default:
                    if (event.getType()
                            == VMEvent.Type.INSTRUCTION_EXECUTED
                            || event.getType()
                            == VMEvent.Type.STATE_CHANGED) {
                        console.appendLine(event.getMessage());
                    }
                    break;
            }
        });
    }

   private void updateIO(VMEvent event) {
       if (event.getType() != VMEvent.Type.OUTPUT) {
           return;
       }
       
       String message = event.getMessage();

        // Salida de texto mediante PRINT.
        if (message != null && message.contains("PRINT")) {
            java.util.List<String> history =
                    service.getVirtualMachine()
                           .getPrinter()
                           .getTextHistory();

            if (history.isEmpty()) {
                console.appendLine(
                        "La impresora no tiene textos registrados.");
                return;
            }
            
            console.appendLine(
                    history.get(history.size() - 1));
            return;
        }

        // Salida numérica mediante OUTPUT.
        java.util.List<Integer> history = service.getOutputHistory();
        
        if (history.isEmpty()) {
            console.appendLine(
                    "La impresora no tiene valores numéricos registrados.");
            return;
        }
        
        console.appendLine(
                "Último valor en outputHistory: "
                        + history.get(history.size() - 1));
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

        rview.areaTable.setText(tablaHead);
        rview.areaTable.append(memory.toString());
    }
    private void updateControls(VMEvent.State state) {
        boolean running = state == VMEvent.State.RUNNING;
        boolean finished = state == VMEvent.State.HALTED
                || state == VMEvent.State.ERROR;

        rview.runButton.setEnabled(!running && !finished);
        rview.stepButton.setEnabled(!running && !finished);
        rview.stopButton.setEnabled(running);
        rview.resetButton.setEnabled(!running);

        rview.statusLabel.setText(state.name());
        console.setInputEnabled(
                state == VMEvent.State.WAITING_FOR_INPUT);
    }

    private void appendLog(String message) {
        console.appendLine(message);
    }
    
    private void updateTextOutput() {
        java.util.List<String> history =
                service.getVirtualMachine()
                       .getPrinter()
                       .getTextHistory();

        if (history.isEmpty()) {
            return;
        }

        console.appendLine(
                history.get(history.size() - 1));
    }
    
}
