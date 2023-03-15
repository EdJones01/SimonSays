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
        loseButtonFlash();
        sequence.clear();
        addToSequence();
    }

    private void showSequence() {
        if (allowShow) {
            allowShow = false;
            showingSequence = true;
            Tools.runInNewThread(() -> {
                for (int id : sequence) {
                    flashButton(buttons[id], flashDelay);
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

    private void flashButton(JButton button, int duration) {
        Tools.runInNewThread(() -> {
            button.setBackground(button.getBackground().brighter().brighter());
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            button.setBackground(button.getBackground().darker().darker());
        });
    }

    private void loseButtonFlash() {
        int[] order = new int[] {0, 1, 3, 2};
        Tools.runInNewThread(() -> {
            for (int i = 0; i < buttons.length; i++) {
               lastClickTime = System.currentTimeMillis();
                flashButton(buttons[order[i]], flashDelay/2);
                try {
                    Thread.sleep(flashDelay/2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void flashAllButtons() {

        Tools.runInNewThread(() -> {
            try {
                Thread.sleep(flashDelay);
                lastClickTime = System.currentTimeMillis();
                Thread.sleep(flashDelay);
                lastClickTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (JButton button : buttons)
                flashButton(button, flashDelay);
        });
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
                flashButton((JButton) e.getSource(), flashDelay);

                if (inputSequence.size() == sequence.size()) {
                    flashAllButtons();
                    allowShow = true;
                    addToSequence();
                    inputSequence.clear();
                }
            }
        }
    }
}