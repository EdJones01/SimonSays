import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {
    private Color[] buttonColors = new Color[]{Color.green, Color.red, Color.yellow, Color.blue};

    private JButton[] buttons = new JButton[4];

    private final Random random = new Random();

    private int flashDelay = 500;

    private long lastClickTime = 0;

    private LinkedList<Integer> sequence = new LinkedList<>();
    private LinkedList<Integer> inputSequence = new LinkedList<>();

    private boolean showingSequence = false;
    private boolean allowShow = true;

    public GamePanel() {
        setLayout(new GridLayout(2, 2));

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = createButton(i);
            add(buttons[i]);
        }

        addToSequence();

        Tools.addKeyBinding(this, KeyEvent.VK_SPACE, "show", (evt) -> showSequence());
    }

    private void reset() {
        allowShow = true;
        flashAllButtons();
        sequence.clear();
        addToSequence();
    }

    private void showSequence() {
        if (allowShow) {
            allowShow = false;
            showingSequence = true;
            Tools.runInNewThread(() -> {
                for (int id : sequence) {
                    flashButton(buttons[id]);
                    try {
                        Thread.sleep(flashDelay * 2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                showingSequence = false;
            });
        }
    }

    private JButton createButton(int index) {
        JButton button = new JButton();
        button.addActionListener(this);
        button.setActionCommand("" + index);
        button.setBackground(buttonColors[index].darker().darker());
        button.setRolloverEnabled(false);
        button.setFocusable(false);
        return button;
    }

    private void addToSequence() {
        sequence.add(random.nextInt(4));
    }

    private void flashButton(JButton button) {
        Tools.runInNewThread(() -> {
            button.setBackground(button.getBackground().brighter().brighter());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            button.setBackground(button.getBackground().darker().darker());
        });
    }

    private void flashAllButtons() {
        for (JButton button : buttons)
            flashButton(button);
    }

    private boolean checkLose() {
        for (int i = 0; i < inputSequence.size(); i++)
            if (sequence.get(i) != inputSequence.get(i))
                return true;
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int buttonID = Integer.parseInt(e.getActionCommand());

        long currentTime = System.currentTimeMillis();
        if (!showingSequence && currentTime - lastClickTime > flashDelay) {
            lastClickTime = currentTime;

            inputSequence.add(buttonID);
            if (checkLose()) {
                reset();
                inputSequence.clear();
            } else {
                flashButton((JButton) e.getSource());

                if (inputSequence.size() == sequence.size()) {
                    allowShow = true;
                    addToSequence();
                    inputSequence.clear();
                }
            }
        }
    }
}