package com.mycompany.simple.vm;


import java.util.Scanner;

/**
 * Deliverable 1 - Test Suite Runner for Simple Virtual Machine.
 * Demonstrates all required test cases:
 * 1. Addition
 * 2. Subtraction
 * 3. Storage
 * 4. Conditional (JZ)
 * 5. Jump (JMP)
 * 6. Keyboard Input -> Disk Storage -> Disk Read -> Screen Output
 */
public class VMTestSuiteRunner {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VirtualMachine vm = new VirtualMachine();

        boolean running = true;

        System.out.println("==================================================");
        System.out.println("   SIMPLE VIRTUAL MACHINE - DELIVERABLE 1 TESTS   ");
        System.out.println("==================================================");

        while (running) {
            printMenu();
            System.out.print("Select an option (1-7): ");
            String input = scanner.nextLine().trim();

            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 7.");
                continue;
            }

            switch (choice) {
                case 1 -> runAdditionTest(vm);
                case 2 -> runSubtractionTest(vm);
                case 3 -> runStorageTest(vm);
                case 4 -> runConditionalTest(vm);
                case 5 -> runJumpTest(vm);
                case 6 -> runKeyboardToDiskToScreenTest(vm, scanner);
                case 7 -> {
                    System.out.println("\nExiting Test Suite. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please select between 1 and 7.");
            }

            if (running) {
                System.out.println("\nPress ENTER to return to the menu...");
                scanner.nextLine();
            }
        }
    }

    private static void printMenu() {
             
        System.out.println(
        """
        --------------------------------------------------
        TEST MENU:
        1. Addition Test (LOAD 10, ADD 11, STORE 20, OUTPUT)
        2. Subtraction Test (LOAD 10, SUB 11, STORE 20, OUTPUT)
        3. Storage Test (LOAD 10, STORE 25, LOAD 25, OUTPUT)
        4. Conditional Branch Test (SUB -> JZ Branch)
        5. Jump Test (Unconditional JMP)
        6. Keyboard Input -> Disk Save -> Disk Read -> Screen Output
        7. Exit
        --------------------------------------------------
        """);
    }

    /**
     * TEST 1: Addition Test
     */
    private static void runAdditionTest(VirtualMachine vm) {
        System.out.println("\n=== TEST 1: ADDITION ===");
        String program = 
            """
            LOAD 10
            ADD 11
            STORE 20
            OUTPUT
            HALT
            """;

        vm.loadProgram(program);
        vm.getMemory().write(10, 50);
        vm.getMemory().write(11, 35);
        System.out.println("Pre-conditions: Memory[10] = 50, Memory[11] = 35");

        vm.run();

        int resultAcc = vm.getCpu().getACC();
        int resultMem = vm.getMemory().read(20);
        System.out.println("\n--- Verification ---");
        System.out.println("Expected Result: 85");
        System.out.println("Actual ACC     : " + resultAcc);
        System.out.println("Actual Mem[20] : " + resultMem);
        if (resultAcc == 85 && resultMem == 85) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }

    /**
     * TEST 2: Subtraction Test
     */
    private static void runSubtractionTest(VirtualMachine vm) {
        System.out.println("\n=== TEST 2: SUBTRACTION ===");
        String program = 
            """
            LOAD 10
            SUB 11
            STORE 20
            OUTPUT
            HALT
            """;

        vm.loadProgram(program);
        vm.getMemory().write(10, 100);
        vm.getMemory().write(11, 40);
        System.out.println("Pre-conditions: Memory[10] = 100, Memory[11] = 40");

        vm.run();

        int resultAcc = vm.getCpu().getACC();
        int resultMem = vm.getMemory().read(20);
        System.out.println("\n--- Verification ---");
        System.out.println("Expected Result: 60 (100 - 40)");
        System.out.println("Actual ACC     : " + resultAcc);
        System.out.println("Actual Mem[20] : " + resultMem);
        if (resultAcc == 60 && resultMem == 60) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }

    /**
     * TEST 3: Storage Test
     */
    private static void runStorageTest(VirtualMachine vm) {
        System.out.println("\n=== TEST 3: STORAGE ===");
        String program = 
            """
            LOAD 10
            STORE 25
            LOAD 25
            OUTPUT
            HALT
            """;

        vm.loadProgram(program);
        vm.getMemory().write(10, 999);
        System.out.println("Pre-conditions: Memory[10] = 999, Memory[25] = 0");

        vm.run();

        int resultMem25 = vm.getMemory().read(25);
        int resultAcc = vm.getCpu().getACC();
        System.out.println("\n--- Verification ---");
        System.out.println("Expected Stored Value: 999");
        System.out.println("Actual Memory[25]    : " + resultMem25);
        System.out.println("Actual ACC           : " + resultAcc);
        if (resultMem25 == 999 && resultAcc == 999) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }

    /**
     * TEST 4: Conditional Branch Test (JZ)
     */
    private static void runConditionalTest(VirtualMachine vm) {
        System.out.println("\n=== TEST 4: CONDITIONAL BRANCH (JZ) ===");
        // Program branches to line 5 (LOAD 13) if 50 - 50 = 0
        String program = 
            """
            LOAD 10
            SUB 11
            JZ 5
            LOAD 12
            HALT
            LOAD 13
            OUTPUT
            HALT
            """;
        // Line 0
        // Line 1: ACC = 50 - 50 = 0 (zeroFlag = true)
        // Line 2: Jumps to Line 5 if ACC is 0
        // Line 3: Skipped
        // Line 4: Skipped
        // Line 5: Target of JZ branch
        // Line 6: Output ACC value
        // Line 7

        vm.loadProgram(program);
        vm.getMemory().write(10, 50);
        vm.getMemory().write(11, 50);
        vm.getMemory().write(12, 111); // Non-branch path value
        vm.getMemory().write(13, 777); // Branch target value

        System.out.println("Pre-conditions: Memory[10]=50, Memory[11]=50, Memory[12]=111, Memory[13]=777");
        System.out.println("Executing SUB 11 -> ACC becomes 0. JZ 5 should skip line 3-4 and jump to line 5.");

        vm.run();

        int resultAcc = vm.getCpu().getACC();
        System.out.println("\n--- Verification ---");
        System.out.println("Expected ACC: 777 (Branch taken to Line 5)");
        System.out.println("Actual ACC  : " + resultAcc);
        if (resultAcc == 777) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL (Conditional jump taken)");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }

    /**
     * TEST 5: Unconditional Jump Test (JMP)
     */
    private static void runJumpTest(VirtualMachine vm) {
        System.out.println("\n=== TEST 5: UNCONDITIONAL JUMP (JMP) ===");
        String program = 
            """
            JMP 3
            LOAD 10
            ADD 11
            LOAD 12
            OUTPUT
            HALT
            """;
        // Line 0: Jump unconditionally to Line 3
        // Line 1: Skipped
        // Line 2: Skipped
        // Line 3: Jump target
        // Line 4: Output ACC value
        // Line 5

        vm.loadProgram(program);
        vm.getMemory().write(10, 100);
        vm.getMemory().write(11, 200);
        vm.getMemory().write(12, 42);

        System.out.println("Pre-conditions: Memory[10]=100, Memory[11]=200, Memory[12]=42");
        System.out.println("Executing JMP 3 -> Should skip lines 1 & 2 directly to Line 3 (LOAD 12).");

        vm.run();

        int resultAcc = vm.getCpu().getACC();
        System.out.println("\n--- Verification ---");
        System.out.println("Expected ACC: 42");
        System.out.println("Actual ACC  : " + resultAcc);
        if (resultAcc == 42) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL (Unconditional jump executed)");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }

    /**
     * TEST 6: Extra Test - Keyboard Input -> Disk Save -> Disk Read -> Screen Output
     */
    private static void runKeyboardToDiskToScreenTest(VirtualMachine vm, Scanner inputScanner) {
        System.out.println("\n=== EXTRA TEST: KEYBOARD -> DISK -> SCREEN ===");
        
        System.out.print("Enter an integer value to input via Keyboard Controller: ");
        int inputVal = 0;
        try {
            inputVal = Integer.parseInt(inputScanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer. Defaulting to 12345.");
            inputVal = 12345;
        }

        // Queue input in KeyboardController for the VM INPUT instruction
        vm.getKeyboard().queueInput(inputVal);

        // Step 1: Program that prompts for INPUT and stores in Memory[10]
        String inputProgram = 
            """
            INPUT
            STORE 10
            HALT
            """;
        // Reads queued value into ACC
        // Stores ACC into Memory[10]
        

        System.out.println("\n1. Running Assembly Program to read Keyboard input into ACC...");
        vm.loadProgram(inputProgram);
        vm.run();

        int keyboardValue = vm.getCpu().getACC();
        System.out.println("ACC Value after INPUT: " + keyboardValue);

        // Step 2: Save the result from ACC to Simulated Disk Sector 7
        int targetSector = 7;
        System.out.printf("\n2. Saving ACC value (%d) to Simulated Disk Sector %d...\n", keyboardValue, targetSector);
        vm.getDisk().writeSector(targetSector);

        // Step 3: Clear ACC register and reset CPU to demonstrate reading from Disk
        System.out.println("\n3. Clearing CPU ACC Register to 0...");
        vm.getCpu().setACC(0);
        System.out.println("ACC value reset: " + vm.getCpu().getACC());

        // Step 4: Read value from Disk Sector 7 back into ACC
        System.out.printf("\n4. Reading value from Disk Sector %d back into ACC...\n", targetSector);
        vm.getDisk().readSector(targetSector);
        int diskValue = vm.getCpu().getACC();

        // Step 5: Run an assembly program to output the restored ACC value to the Screen/Printer
        String outputProgram = 
            """
            STORE 10
            OUTPUT
            HALT
            """;
            // Sends ACC value to PrinterController
        

        System.out.println("\n5. Running Assembly Program to print the restored value to Screen...");
        vm.loadProgram(outputProgram);
        // Ensure ACC retains the disk read value
        vm.getCpu().setACC(diskValue);
        vm.run();

        System.out.println("\n--- Verification ---");
        System.out.println("Original Keyboard Input : " + inputVal);
        System.out.println("Value Restored from Disk: " + diskValue);
        if (inputVal == diskValue) {
            System.out.println("[STATUS]: PASSED SUCCESSFUL (Keyboard -> Disk -> Read -> Screen cycle complete)");
        } else {
            System.out.println("[STATUS]: FAILED");
        }
    }
}
