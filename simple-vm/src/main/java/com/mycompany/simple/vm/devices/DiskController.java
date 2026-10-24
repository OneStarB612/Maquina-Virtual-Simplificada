package com.mycompany.simple.vm.devices;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DiskController implements DeviceController {

    public static final int DISK_SIZE = 16384;
    private static final int FILE_SLOT_SIZE = 1024;
    private static final int MAX_FILES = DISK_SIZE / FILE_SLOT_SIZE;

    private final int[] diskStorage = new int[DISK_SIZE];
    private final Map<String, Integer> fileSlots = new HashMap<>();

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

    public synchronized void writeFile(String name, String content) {
        if (name == null || name.isBlank() || content == null) {
            throw new IllegalArgumentException("Nombre o contenido inválido.");
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        if (bytes.length >= FILE_SLOT_SIZE) {
            throw new IllegalArgumentException(
                    "El archivo supera el límite de "
                    + (FILE_SLOT_SIZE - 1) + " bytes.");
        }

        Integer slot = fileSlots.get(name);

        if (slot == null) {
            boolean[] occupied = new boolean[MAX_FILES];

            for (int existingSlot : fileSlots.values()) {
                occupied[existingSlot] = true;
            }

            slot = -1;
            for (int i = 0; i < MAX_FILES; i++) {
                if (!occupied[i]) {
                    slot = i;
                    break;
                }
            }

            if (slot == -1) {
                throw new IllegalStateException("Disco simulado sin espacio para archivos.");
            }

            fileSlots.put(name, slot);
        }

        int start = slot * FILE_SLOT_SIZE;
        Arrays.fill(diskStorage, start, start + FILE_SLOT_SIZE, 0);
        diskStorage[start] = bytes.length;

        for (int i = 0; i < bytes.length; i++) {
            diskStorage[start + 1 + i] = bytes[i] & 0xFF;
        }
    }

    public synchronized String readFile(String name) {
        Integer slot = fileSlots.get(name);

        if (slot == null) {
            throw new IllegalArgumentException(
                    "Archivo no encontrado en el disco simulado: " + name);
        }

        int start = slot * FILE_SLOT_SIZE;
        int length = diskStorage[start];

        if (length < 0 || length >= FILE_SLOT_SIZE) {
            throw new IllegalStateException("Contenido de archivo inválido.");
        }

        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) diskStorage[start + 1 + i];
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public synchronized boolean fileExists(String name) {
        return fileSlots.containsKey(name);
    }

    @Override
    public synchronized void reset() {
        Arrays.fill(diskStorage, 0);
        fileSlots.clear();
    }

    public void clear() {
        reset();
    }

    private void validateSector(int sector) {
        if (sector < 0 || sector >= DISK_SIZE) {
            throw new IllegalArgumentException(
                    "Sector inválido: " + sector
                    + ". Rango permitido: 0–" + (DISK_SIZE - 1) + ".");
        }
    }
}