import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class FlashcardApp extends JFrame {

    private JTextField questionField;
    private JTextField answerField;

    public FlashcardApp() {
        setTitle("Flashcard Learning App - Add Only");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        showInputPanel();
    }

    private void showInputPanel() {
        questionField = new JTextField(20);
        answerField = new JTextField(20);
        JButton saveButton = new JButton("Save Card");

        saveButton.addActionListener(e -> {
            String question = questionField.getText();
            String ans = answerField.getText();
            if (!question.isEmpty() && !ans.isEmpty()) {
                Card card = new Card(question, ans);
                FileHandler.saveCard(card);
                questionField.setText("");
                answerField.setText("");
                JOptionPane.showMessageDialog(this, "Card saved!");
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(5, 1));
        inputPanel.add(new JLabel("Question:"));
        inputPanel.add(questionField);
        inputPanel.add(new JLabel("Answer:"));
        inputPanel.add(answerField);
        inputPanel.add(saveButton);

        add(inputPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlashcardApp().setVisible(true));
    }

// Inside classes

    static class Card {
        private String question;
        private String answer;

        public Card(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        @Override
        public String toString() {
            return question + ";;" + answer;
        }
    }

    static class FileHandler {
        private static final String FILE_NAME = "cards.txt";

        public static void saveCard(Card card) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                writer.write(card.toString());
                writer.newLine();
            } catch (IOException e) {
                System.out.println("Error saving card: " + e.getMessage());
            }
        }
    }
}
