package com.mycompany.simple.vm;

import com.mycompany.simple.vm.service.VMService;
import com.mycompany.simple.vm.controller.VMController;
import com.mycompany.simple.vm.ui.ConsoleFrame;
import com.mycompany.simple.vm.ui.RegFrame;
import com.mycompany.simple.vm.ui.VMFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class test{

     public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                System.err.println(
                    "No se pudo aplicar el tema del sistema: "
                    + ex.getMessage()
                );
            }

            VMService service = new VMService();

            VMFrame vmFrame = new VMFrame();
            ConsoleFrame console = new ConsoleFrame();
            RegFrame rview = new RegFrame();

            new VMController(
                vmFrame,
                console,
                rview,
                service
            );

            // La ventana principal se muestra al iniciar.
            // ESFrame y ExeFrame comienzan ocultas.
            console.setVisible(false);
            rview.setVisible(false);

            vmFrame.setLocationRelativeTo(null);
            vmFrame.setVisible(true);
        });
    }
}