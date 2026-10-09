package com.mycompany.simple.vm.devices;

import com.mycompany.simple.vm.VirtualMachine;

/**
 * Disk Controller simulating a persistent random-access secondary storage medium.
 * Implements the DeviceController interface.
 * ACC register values can be written to and loaded from specific disk sectors.
 */
public class DiskController implements DeviceController {
    private static final int DISK_SIZE = 128; // Simulated disk sectors
    private final int[] diskStorage = new int[DISK_SIZE];
    private final VirtualMachine vm;

    public DiskController(VirtualMachine vm) {
        this.vm = vm;
    }

    @Override
    public String getDeviceName() {
        return "Disk";
    }

    @Override
    public void reset() {
        for (int i = 0; i < DISK_SIZE; i++) {
            diskStorage[i] = 0;
        }
    }

    /**
     * Clears disk storage.
     */
    public void clear() {
        reset();
    }

    /**
     * Writes the current ACC value to a simulated disk sector.
     * @param sector The sector index on disk (0 to 127).
     */
    public void writeSector(int sector) {
        validateSector(sector);
        diskStorage[sector] = vm.getCpu().getACC();
        System.out.printf("[DISK CONTROLLER] Wrote ACC value %d to sector %d\\n", diskStorage[sector], sector);
    }

    /**
     * Reads a sector from disk and places the value in the ACC register.
     * @param sector The sector index on disk (0 to 127).
     */
    public void readSector(int sector) {
        validateSector(sector);
        vm.getCpu().setACC(diskStorage[sector]);
        System.out.printf("[DISK CONTROLLER] Read value %d from sector %d into ACC\\n", diskStorage[sector], sector);
    }

    private void validateSector(int sector) {
        if (sector < 0 || sector >= DISK_SIZE) {
            throw new IllegalArgumentException("Disk I/O Error: Sector " + sector + " is out of bounds (0-" + (DISK_SIZE - 1) + ").");
        }
    }

    /**
     * Helper method to get the disk storage for verification.
     * @return Clone of the disk array.
     */
    public int[] getDiskStorage() {
        return diskStorage.clone();
    }
}
