/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple.vm.service;

import com.mycompany.simple.vm.devices.DiskController;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author steven
 */
public class ProgramFileService {

    private final DiskController disk;

    public ProgramFileService(DiskController disk) {
        if (disk == null) {
            throw new IllegalArgumentException("El disco no puede ser null.");
        }
        this.disk = disk;
    }

    public void saveExample(String name, String source) {
        disk.writeFile(name, source);
    }

    public String loadExample(String name) {
        return disk.readFile(name);
    }

    public String openFile(JFileChooser chooser, java.awt.Component parent)
            throws IOException {

        configureChooser(chooser, "Abrir programa");

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return Files.readString(
                chooser.getSelectedFile().toPath(),
                StandardCharsets.UTF_8);
    }

    public File saveFile(
            JFileChooser chooser,
            java.awt.Component parent,
            String content,
            File currentFile) throws IOException {

        File target = currentFile;

        if (target == null) {
            configureChooser(chooser, "Guardar programa");

            if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            target = chooser.getSelectedFile();

            if (!target.getName().contains(".")) {
                target = new File(
                        target.getParentFile(),
                        target.getName() + ".txt");
            }

            if (target.exists()) {
                int answer = javax.swing.JOptionPane.showConfirmDialog(
                        parent,
                        "El archivo ya existe. ¿Deseas sobrescribirlo?",
                        "Confirmar guardado",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE);

                if (answer != javax.swing.JOptionPane.YES_OPTION) {
                    return null;
                }
            }
        }

        Files.writeString(
                target.toPath(),
                content,
                StandardCharsets.UTF_8);

        return target;
    }

    private void configureChooser(JFileChooser chooser, String title) {
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Archivos de programa (*.txt, *.asm)", "txt", "asm"));
    }
}