
package com.mycompany.simple.vm.devices;

import java.util.Arrays;

public class DiskController implements DeviceController {

    public static final int DISK_SIZE = 128;

    private final int[] diskStorage = new int[DISK_SIZE];

    @Override
    public String getDeviceName() {
        return "Disk";
    }

    public synchronized void writeSector(int sector, int value) {
        validateSector(sector);
        diskStorage[sector] = value;
    }

    public synchronized int readSector(int sector) {
        validateSector(sector);
        return diskStorage[sector];
    }

    public synchronized int[] getDiskStorage() {
        return diskStorage.clone();
    }

    @Override
    public synchronized void reset() {
        Arrays.fill(diskStorage, 0);
    }

    public void clear() {
        reset();
    }

    private void validateSector(int sector) {
        if (sector < 0 || sector >= DISK_SIZE) {
            throw new IllegalArgumentException(
                    "Sector inválido: " + sector
                    + ". Rango permitido: 0–"
                    + (DISK_SIZE - 1) + ".");
        }
    }
}

