package com.mycompany.simple.vm.service;

import com.mycompany.simple.vm.Instruction;
import com.mycompany.simple.vm.Memory;
import com.mycompany.simple.vm.model.ProgramImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ProgramAssembler {

    private static final Pattern IDENTIFIER =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final Pattern INTEGER =
            Pattern.compile("-?\\d+");

    private static final String DATA_DIRECTIVE = "DATA";
    private static final String TEXT_DIRECTIVE = "TEXT";

    private static final class SourceLine {

        private final int lineNumber;
        private final String label;
        private final String operation;
        private final String operand;
        private final boolean data;

        private SourceLine(
                int lineNumber,
                String label,
                String operation,
                String operand,
                boolean data) {

            this.lineNumber = lineNumber;
            this.label = label;
            this.operation = operation;
            this.operand = operand;
            this.data = data;
        }
    }

    public ProgramImage assemble(String source) {

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(
                    "El programa está vacío.");
        }

        List<SourceLine> code = new ArrayList<>();
        List<SourceLine> data = new ArrayList<>();
        Map<String, Integer> symbols = new HashMap<>();

        String[] lines = source.split("\\R", -1);

        boolean dataSectionStarted = false;
        int instructionCount = 0;
        int dataCount = 0;
        boolean haltFound = false;

        // Primera pasada: registrar instrucciones, datos y etiquetas.
        for (int i = 0; i < lines.length; i++) {

            int lineNumber = i + 1;
            String line = lines[i].trim();

            if (line.isEmpty()
                    || line.startsWith(";")
                    || line.startsWith("#")
                    || line.startsWith("//")) {
                continue;
            }

            String label = null;

            int colonIndex = line.indexOf(':');

            if (colonIndex >= 0) {
                label = line.substring(0, colonIndex).trim();
                line = line.substring(colonIndex + 1).trim();

                if (!IDENTIFIER.matcher(label).matches()) {
                    throw error(lineNumber,
                            "Nombre de etiqueta inválido: " + label);
                }

                if (symbols.containsKey(label)) {
                    throw error(lineNumber,
                            "Etiqueta duplicada: " + label);
                }

                if (line.isEmpty()) {
                    throw error(lineNumber,
                            "Falta una instrucción o DATA después de la etiqueta.");
                }
            }

            String[] parts = line.split("\\s+");
            String operation = parts[0].toUpperCase(Locale.ROOT);
            
            if (operation.equals(TEXT_DIRECTIVE)) {
                dataSectionStarted = true;
                
            if (label == null) {
                throw error(lineNumber,
                        "TEXT requiere una etiqueta, por ejemplo: mensaje: TEXT \"Hola\".");
            }
            
            String literal = line.substring(parts[0].length()).trim();
            String text = parseTextLiteral(literal, lineNumber);

    int address = instructionCount + dataCount;
    int requiredSpace = text.length() + 1;

    if (address + requiredSpace > Memory.SIZE) {
        throw error(lineNumber,
                "El texto supera la memoria disponible.");
    }

    symbols.put(label, address);

    data.add(new SourceLine(
            lineNumber,
            label,
            TEXT_DIRECTIVE,
            text,
            true
    ));

    dataCount += requiredSpace;
    continue;
}

            if (operation.equals(DATA_DIRECTIVE)) {

                dataSectionStarted = true;

                if (label == null) {
                    throw error(lineNumber,
                            "DATA requiere una etiqueta, por ejemplo: numero: DATA 5.");
                }

                if (parts.length != 2
                        || !INTEGER.matcher(parts[1]).matches()) {
                    throw error(lineNumber,
                            "Sintaxis de DATA inválida. Ejemplo: numero: DATA 5.");
                }

                int value;

                try {
                    value = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    throw error(lineNumber,
                            "El valor de DATA está fuera del rango de enteros.");
                }

                int address = instructionCount + dataCount;

                if (address >= Memory.SIZE) {
                    throw error(lineNumber,
                            "El programa y sus datos superan la memoria disponible.");
                }

                symbols.put(label, address);

                data.add(new SourceLine(
                        lineNumber, label,
                        DATA_DIRECTIVE, String.valueOf(value), true));

                dataCount++;
                continue;
            }

            if (dataSectionStarted) {
                throw error(lineNumber,
                        "Las instrucciones deben aparecer antes de las declaraciones DATA.");
            }

            if (instructionCount >= Memory.SIZE) {
                throw error(lineNumber,
                        "El programa supera la capacidad de memoria.");
            }

            if (label != null) {
                symbols.put(label, instructionCount);
            }

            String operand = null;

            if (parts.length > 2) {
                throw error(lineNumber,
                        "La instrucción contiene operandos adicionales.");
            }

            if (parts.length == 2) {
                operand = parts[1];
            }

            Instruction instruction;

            try {
                instruction = Instruction.fromMnemonic(operation);
            } catch (IllegalArgumentException ex) {
                throw error(lineNumber,
                        "Instrucción desconocida: " + operation);
            }

            if (instruction.requiresOperand() != (operand != null)) {
                throw error(lineNumber,
                        instruction.requiresOperand()
                                ? operation + " requiere un operando."
                                : operation + " no acepta operandos.");
            }

            if (instruction == Instruction.HALT) {
                haltFound = true;
            }

            code.add(new SourceLine(
                    lineNumber, label, operation, operand, false));

            instructionCount++;
        }

        if (instructionCount == 0) {
            throw new IllegalArgumentException(
                    "No se encontraron instrucciones ejecutables.");
        }

        if (!haltFound) {
            throw new IllegalArgumentException(
                    "No se encontró la instrucción HALT.");
        }

        if (instructionCount + dataCount > Memory.SIZE) {
            throw new IllegalArgumentException(
                    "El programa y sus datos superan la capacidad de memoria.");
        }

        // Segunda pasada: codificar las instrucciones y cargar los datos.
        int[] image = new int[Memory.SIZE];

        for (int address = 0; address < code.size(); address++) {

            SourceLine sourceLine = code.get(address);

            try {
                Instruction instruction =
                        Instruction.fromMnemonic(sourceLine.operation);

                int operand = 0;

                if (instruction.requiresOperand()) {
                    operand = resolveOperand(
                            sourceLine.operand,
                            symbols,
                            sourceLine.lineNumber);
                }

                image[address] =
                        instruction.requiresOperand()
                                ? Instruction.encode(instruction, operand)
                                : Instruction.encode(instruction);

            } catch (IllegalArgumentException ex) {
                throw error(sourceLine.lineNumber, ex.getMessage());
            }
        }

        for (SourceLine sourceLine : data) {
            int address = symbols.get(sourceLine.label);
            
            if (sourceLine.operation.equals(TEXT_DIRECTIVE)) {
                String text = sourceLine.operand;
                
                for (int i = 0; i < text.length(); i++) {
                    image[address + i] = text.charAt(i);
                }

            // Terminador del texto.
            image[address + text.length()] = 0;
            
            } else {
        image[address] = Integer.parseInt(sourceLine.operand);
            }
        }

        return new ProgramImage(
                image, instructionCount, dataCount);
    }

    private int resolveOperand(
            String operand,
            Map<String, Integer> symbols,
            int lineNumber) {

        Integer address = symbols.get(operand);

        if (address != null) {
            return address;
        }

        if (!INTEGER.matcher(operand).matches()) {
            throw error(lineNumber,
                    "Operando desconocido: " + operand);
        }

        try {
            address = Integer.parseInt(operand);
        } catch (NumberFormatException ex) {
            throw error(lineNumber,
                    "Operando fuera del rango de enteros.");
        }

        if (address < 0 || address >= Memory.SIZE) {
            throw error(lineNumber,
                    "Dirección fuera de rango: " + address);
        }

        return address;
    }
    
    private String parseTextLiteral(String literal, int lineNumber) {
        if (literal.length() < 2
                || literal.charAt(0) != '"'
                || literal.charAt(literal.length() - 1) != '"') {
            throw error(lineNumber,
                    "Texto inválido. Usa comillas dobles: TEXT \"Hola\".");
        }
        String content = literal.substring(1, literal.length() - 1);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c != '\\') {
                result.append(c);
                continue;
            }
            
            if (++i >= content.length()) {
                throw error(lineNumber,
                        "Secuencia de escape incompleta en TEXT.");
            }
            
            char escaped = content.charAt(i);
            
            switch (escaped) {
                case 'n':
                    result.append('\n');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                default:
                    throw error(lineNumber,
                            "Secuencia de escape no válida: \\" + escaped);
            }
        }
        return result.toString();
    }

    private IllegalArgumentException error(
            int lineNumber,
            String message) {

        return new IllegalArgumentException(
                "Línea " + lineNumber + ": " + message);
    }
}

