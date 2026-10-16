
package com.mycompany.simple.vm.service;

import com.mycompany.simple.vm.model.CompilationResult;
import com.mycompany.simple.vm.model.ProgramImage;

import java.util.ArrayList;
import java.util.List;

public class CompilationService {

    private final ProgramAssembler assembler =
            new ProgramAssembler();

    public CompilationResult compile(String source) {

        try {
            ProgramImage program = assembler.assemble(source);

            List<String> errors = new ArrayList<>();

            return new CompilationResult(
                    true,
                    program.getInstructionCount(),
                    errors
            );

        } catch (IllegalArgumentException ex) {

            List<String> errors = new ArrayList<>();
            errors.add(ex.getMessage());

            return new CompilationResult(
                    false,
                    0,
                    errors
            );
        }
    }
}

