
package com.mycompany.simple.vm.model;

public final class ProgramImage {

    private final int[] memoryImage;
    private final int instructionCount;
    private final int dataCount;

    public ProgramImage(
            int[] memoryImage,
            int instructionCount,
            int dataCount) {

        this.memoryImage = memoryImage.clone();
        this.instructionCount = instructionCount;
        this.dataCount = dataCount;
    }

    public int[] getMemoryImage() {
        return memoryImage.clone();
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public int getDataCount() {
        return dataCount;
    }
}

