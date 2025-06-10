import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class FlashcardApp extends JFrame {

    private Deck deck;
    private JTextField questionField, answerField;
    private JLabel displayLabel;
    private boolean showingQuestion = true;

    public FlashcardApp() {
        setTitle("Flashcard Learning App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        deck = new Deck();
        deck.getCards().addAll(FileHandler.loadCards());

        showMainMenu();
    }

    private void showMainMenu() {
        getContentPane().removeAll();
        JButton inputButton = new JButton("Add Flashcards");
        JButton reviewButton = new JButton("Review Flashcards");

        inputButton.addActionListener(e -> showInputPanel());
        reviewButton.addActionListener(e -> showReviewPanel());

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(inputButton);
        panel.add(reviewButton);
        add(panel);
        revalidate();
        repaint();
    }

    private void showInputPanel() {
        getContentPane().removeAll();

        questionField = new JTextField(20);
        answerField = new JTextField(20);
        JButton saveButton = new JButton("Save Card");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> {
            String q = questionField.getText();
            String a = answerField.getText();
            if (!q.isEmpty() && !a.isEmpty()) {
                deck.addCard(new Card(q, a));
                FileHandler.saveCards(deck.getCards());
                questionField.setText("");
                answerField.setText("");
            }
        });

        backButton.addActionListener(e -> showMainMenu());

        JPanel inputPanel = new JPanel(new GridLayout(6, 1));
        inputPanel.add(new JLabel("Question:"));
        inputPanel.add(questionField);
        inputPanel.add(new JLabel("Answer:"));
        inputPanel.add(answerField);
        inputPanel.add(saveButton);
        inputPanel.add(backButton);

        add(inputPanel);
        revalidate();
        repaint();
    }

    private void showReviewPanel() {
        getContentPane().removeAll();

        displayLabel = new JLabel("Click 'Flip' to see the answer", SwingConstants.CENTER);
        JButton flipButton = new JButton("Flip");
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Previous");
        JButton backButton = new JButton("Back");

        flipButton.addActionListener(e -> {
            Card current = deck.getCurrentCard();
            if (current != null) {
                if (showingQuestion) {
                    displayLabel.setText(current.getAnswer());
                } else {
                    displayLabel.setText(current.getQuestion());
                }
                showingQuestion = !showingQuestion;
            }
        });

        nextButton.addActionListener(e -> {
            deck.nextCard();
            Card current = deck.getCurrentCard();
            if (current != null) {
                displayLabel.setText(current.getQuestion());
                showingQuestion = true;
            }
        });

        prevButton.addActionListener(e -> {
            deck.prevCard();
            Card current = deck.getCurrentCard();
            if (current != null) {
                displayLabel.setText(current.getQuestion());
                showingQuestion = true;
            }
        });

        backButton.addActionListener(e -> showMainMenu());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(flipButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(backButton);

        add(displayLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        if (deck.getCurrentCard() != null)
            displayLabel.setText(deck.getCurrentCard().getQuestion());

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlashcardApp().setVisible(true));
    }

    // -------------------- Inner Classes --------------------

    static class Card {
        private String question;
        private String answer;

        public Card(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        @Override
        public String toString() {
            return question + ";;" + answer;
        }

        public static Card fromString(String line) {
            String[] parts = line.split(";;");
            if (parts.length == 2) {
                return new Card(parts[0], parts[1]);
            }
            return new Card("Invalid", "Invalid");
        }
    }

    class Deck {
        private ArrayList<Card> cards = new ArrayList<>();
        private int currentIndex = 0;

        public void addCard(Card card) {
            cards.add(card);
        }

        public ArrayList<Card> getCards() {
            return cards;
        }

        public Card getCurrentCard() {
            if (cards.isEmpty()) return null;
            return cards.get(currentIndex);
        }

        public void nextCard() {
            if (!cards.isEmpty()) currentIndex = (currentIndex + 1) % cards.size();
        }

        public void prevCard() {
            if (!cards.isEmpty()) currentIndex = (currentIndex - 1 + cards.size()) % cards.size();
        }
    }

    static class FileHandler {
        private static final String FILE_NAME = "cards.txt";

        public static void saveCards(ArrayList<Card> cards) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                for (Card card : cards) {
                    writer.write(card.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error saving cards: " + e.getMessage());
            }
        }

        public static ArrayList<Card> loadCards() {
            ArrayList<Card> cards = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    cards.add(Card.fromString(line));
                }
            } catch (IOException e) {
                System.out.println("Error loading cards: " + e.getMessage());
            }
            return cards;
        }
    }
}
