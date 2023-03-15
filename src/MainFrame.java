import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    public MainFrame() {
        setTitle("Simon Says");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GamePanel panel = new GamePanel();
        panel.setPreferredSize(new Dimension(600, 600));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
