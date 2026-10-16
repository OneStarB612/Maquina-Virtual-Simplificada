
package com.mycompany.simple.vm.service;

import com.mycompany.simple.vm.Instruction;
import com.mycompany.simple.vm.Memory;
import com.mycompany.simple.vm.model.CompilationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/*
¿Qué valida?
- Instrucciones desconocidas.
- Instrucciones que necesitan un operando y no lo tienen.
- Operandos que no son números enteros.
- Operandos fuera del rango de memoria 0–4095.
- Operandos adicionales en instrucciones que no los admiten.
- Programas sin HALT.
- Programas que superan la capacidad de memoria.
Limitación deliberada: esta primera versión no resuelve nombres de etiquetas ni verifica que un salto llegue a una instrucción existente. Por ejemplo, JMP 100 puede ser válido aunque el programa tenga menos de 101 instrucciones. La etiqueta se ignora como en tu cargador actual; los saltos deben utilizar direcciones numéricas.

*/

public class CompilationService {

    public CompilationResult compile(String source) {
        List<String> errors = new ArrayList<>();
        int instructionCount = 0;
        boolean haltFound = false;

        if (source == null || source.trim().isEmpty()) {
            errors.add("El programa está vacío.");

            return new CompilationResult(
                    false, 0, errors);
        }

        String[] lines = source.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNumber = i + 1;

            // Ignorar líneas vacías y comentarios.
            if (line.isEmpty()
                    || line.startsWith("/")
                    || line.startsWith("#")
                    || line.startsWith(";")) {
                continue;
            }

            // Permitir etiquetas como "INICIO: LOAD 10".
            // Por ahora, los saltos utilizan direcciones numéricas.
            if (line.contains(":")) {
                line = line.substring(
                        line.indexOf(':') + 1).trim();
            }

            if (line.isEmpty()) {
                errors.add("Línea " + lineNumber
                        + ": falta una instrucción después de la etiqueta.");
                continue;
            }

            String[] parts = line.split("\\s+");
            String mnemonic = parts[0].toUpperCase(Locale.ROOT);

            Instruction instruction;

            try {
                instruction = Instruction.fromMnemonic(mnemonic);
            } catch (IllegalArgumentException ex) {
                errors.add("Línea " + lineNumber
                        + ": instrucción desconocida '"
                        + mnemonic + "'.");
                continue;
            }

            boolean requiresOperand = instruction.requiresOperand();

            if (requiresOperand && parts.length < 2) {
                errors.add("Línea " + lineNumber
                        + ": la instrucción " + mnemonic
                        + " requiere un operando.");
                continue;
            }

            if (!requiresOperand && parts.length > 1) {
                errors.add("Línea " + lineNumber
                        + ": la instrucción " + mnemonic
                        + " no acepta operandos.");
                continue;
            }

            int operand = 0;

            if (requiresOperand) {
                try {
                    operand = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    errors.add("Línea " + lineNumber
                            + ": operando inválido '"
                            + parts[1] + "'. Debe ser un entero.");
                    continue;
                }

                if (operand < 0 || operand >= Memory.SIZE) {
                    errors.add("Línea " + lineNumber
                            + ": el operando debe estar entre 0 y "
                            + (Memory.SIZE - 1) + ".");
                    continue;
                }
            }

            instructionCount++;

            if (instruction == Instruction.HALT) {
                haltFound = true;
            }
        }

        if (instructionCount > Memory.SIZE) {
            errors.add("El programa supera la capacidad de memoria: "
                    + Memory.SIZE + " instrucciones.");
        }

        if (instructionCount > 0 && !haltFound) {
            errors.add("No se encontró la instrucción HALT.");
        }

        boolean successful = errors.isEmpty();

        return new CompilationResult(
                successful,
                instructionCount,
                errors);
    }
}

