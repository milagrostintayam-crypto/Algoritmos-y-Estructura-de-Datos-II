import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                frmPrin formulario = new frmPrin();
                JFrame ventana = new JFrame("Sistema de registro de hospedaje");
                ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                ventana.setContentPane(formulario.getPanel());
                ventana.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosing(WindowEvent e) { if (formulario.guardar()) ventana.dispose(); }
                });
                ventana.setMinimumSize(new Dimension(1050,680)); ventana.setSize(1200,780); ventana.setLocationRelativeTo(null); ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,"No se pudo iniciar el sistema:\n"+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
