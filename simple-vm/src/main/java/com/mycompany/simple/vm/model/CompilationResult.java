
package com.mycompany.simple.vm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*

isSuccessful()	Indica si la validación terminó sin errores.
getInstructionCount()	Devuelve la cantidad de instrucciones reconocidas.
getErrors()	Devuelve los errores encontrados.
getSummary()	Genera un mensaje breve para mostrar en la interfaz.

*/

public class CompilationResult {

    private final boolean successful;
    private final int instructionCount;
    private final List<String> errors;

    public CompilationResult(
            boolean successful,
            int instructionCount,
            List<String> errors) {

        this.successful = successful;
        this.instructionCount = instructionCount;
        this.errors = new ArrayList<>(errors);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String getSummary() {
        if (successful) {
            return "Validación exitosa. Instrucciones: "
                    + instructionCount;
        }

        return "Se encontraron " + errors.size()
                + " error(es) en el programa.";
    }
}

