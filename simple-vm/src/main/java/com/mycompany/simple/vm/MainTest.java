package com.mycompany.simple.vm;


/**
 * Main execution test class to demonstrate that the simple virtual machine
 * is fully functional and meets the specification.
 */
public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== SIMPLE VIRTUAL MACHINE TEST ===");

        VirtualMachine vm = new VirtualMachine();

        // Let's load the standard program defined in Deliverable 1:
        // LOAD 10
        // ADD 11
        // STORE 20
        // OUTPUT
        // HALT
        String program = 
            "LOAD 10\n" +
            "ADD 11\n" +
            "STORE 20\n" +
            "OUTPUT\n" +
            "HALT\n";

        System.out.println("\nLoading program:\n" + program);
        vm.loadProgram(program);

        // Pre-conditions (inputs)
        System.out.println("\nSetting memory values:");
        System.out.println("Memory[10] = 50");
        System.out.println("Memory[11] = 35");
        vm.getMemory().write(10, 50);
        vm.getMemory().write(11, 35);

        // Run the virtual machine!
        System.out.println("\nStarting program execution...\n");
        vm.run();

        // Verify the post-conditions
        System.out.println("\n=== EXECUTION VERIFICATION ===");
        int resultInAcc = vm.getCpu().getACC();
        int resultInMemory20 = vm.getMemory().read(20);
        
        System.out.println("Expected ACC: 85 (50 + 35)");
        System.out.println("Actual ACC  : " + resultInAcc);
        
        System.out.println("Expected Memory[20]: 85");
        System.out.println("Actual Memory[20]  : " + resultInMemory20);

        if (resultInAcc == 85 && resultInMemory20 == 85) {
            System.out.println("\nSUCCESS: All execution stages worked perfectly!");
        } else {
            System.err.println("\nFAILURE: Value mismatches detected.");
        }
    }
}
