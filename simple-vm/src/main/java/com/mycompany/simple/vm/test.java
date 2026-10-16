package com.mycompany.simple.vm;

import com.mycompany.simple.vm.service.VMService;
import com.mycompany.simple.vm.controller.VMController;
import com.mycompany.simple.vm.ui.ESFrame;
import com.mycompany.simple.vm.ui.ExeFrame;
import com.mycompany.simple.vm.ui.VMFrame;
import com.mycompany.simple.vm.ui.VMView;

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
            ESFrame esFrame = new ESFrame();
            VMView exeFrame = new VMView();

            new VMController(
                vmFrame,
                esFrame,
                exeFrame,
                service
            );

            // La ventana principal se muestra al iniciar.
            // ESFrame y ExeFrame comienzan ocultas.
            esFrame.setVisible(false);
            exeFrame.setVisible(false);

            vmFrame.setLocationRelativeTo(null);
            vmFrame.setVisible(true);
        });
    }
}