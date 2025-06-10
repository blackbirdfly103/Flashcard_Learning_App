import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

class FlashcardApp extends JFrame {

    private Deck deck;
    private JTextField questionField, answerField;
    private JLabel displayLabel;
    private boolean showingQuestion = true;

    public FlashcardApp() {
        setTitle("Flashcard Learning App");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        deck = new Deck();
        deck.getCards().addAll(FileHandler.loadCards());

        showMainMenu();
    }

    private void showMainMenu() {
        getContentPane().removeAll();

        JButton inB = new JButton("Add/Remove Flashcards");
        JButton revB = new JButton("Review Flashcards");
        JButton qB = new JButton("Quit");

        inB.addActionListener(e -> showInputPanel());
        revB.addActionListener(e -> showReviewPanel());
        qB.addActionListener(e -> System.exit(0));

        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(inB);
        panel.add(revB);
        panel.add(qB);

        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void showInputPanel() {
        getContentPane().removeAll();

        questionField = new JTextField(20);
        answerField = new JTextField(20);
        JButton saveb = new JButton("Save Card");
        JButton removeb = new JButton("Remove Selected Card");
        JButton backb = new JButton("Back");
        JButton quitb = new JButton("Quit");

        String[] columnNames = {"#", "Question", "Answer"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        JTable table = new JTable(tableModel);

        ArrayList<Card> cards = deck.getCards();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            tableModel.addRow(new Object[]{i + 1, c.getQuestion(), c.getAnswer()});
        }

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column > 0) { // Ignore numbering column
                    String updatedValue = (String) tableModel.getValueAt(row, column);
                    Card card = deck.getCards().get(row);
                    if (column == 1) { // Question updated
                        card = new Card(updatedValue, card.getAnswer());
                    } else if (column == 2) { // Answer updated
                        card = new Card(card.getQuestion(), updatedValue);
                    }
                    deck.getCards().set(row, card);
                    FileHandler.saveCards(deck.getCards());
                }
            }
        });

        saveb.addActionListener(e -> {
            String q = questionField.getText().trim();
            String a = answerField.getText().trim();
            if (!q.isEmpty() && !a.isEmpty()) {
                Card newCard = new Card(q, a);
                deck.addCard(newCard);
                FileHandler.saveCards(deck.getCards());
                tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, q, a});
                questionField.setText("");
                answerField.setText("");
            }
        });

        removeb.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                deck.getCards().remove(selectedRow);
                FileHandler.saveCards(deck.getCards());
                tableModel.removeRow(selectedRow);

                // Update numbering
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    tableModel.setValueAt(i + 1, i, 0);
                }
            }
        });

        backb.addActionListener(e -> showMainMenu());
        quitb.addActionListener(e -> System.exit(0));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("                             Question:"));
        inputPanel.add(questionField);
        inputPanel.add(new JLabel("                             Answer:"));
        inputPanel.add(answerField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveb);
        buttonPanel.add(removeb);
        buttonPanel.add(backb);
        buttonPanel.add(quitb);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 150));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        revalidate();
        repaint();
    }


    private void showReviewPanel() {
        getContentPane().removeAll();

        displayLabel = new JLabel("", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton flip = new JButton("Flip");
        JButton next = new JButton("Next");
        JButton prev = new JButton("Previous");
        JButton back = new JButton("Back");
        JButton quit = new JButton("Quit");

        flip.addActionListener(e -> {
            Card current = deck.getCurrentCard();
            if (current != null) {
                displayLabel.setText(showingQuestion ? current.getAnswer() : current.getQuestion());
                showingQuestion = !showingQuestion;
            }
        });

        next.addActionListener(e -> {
            deck.nextCard();
            Card current = deck.getCurrentCard();
            if (current != null) {
                displayLabel.setText(current.getQuestion());
                showingQuestion = true;
            }
        });

        prev.addActionListener(e -> {
            deck.prevCard();
            Card current = deck.getCurrentCard();
            if (current != null) {
                displayLabel.setText(current.getQuestion());
                showingQuestion = true;
            }
        });

        back.addActionListener(e -> showMainMenu());
        quit.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prev);
        buttonPanel.add(flip);
        buttonPanel.add(next);
        buttonPanel.add(back);
        buttonPanel.add(quit);

        add(displayLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        Card current = deck.getCurrentCard();
        if (current != null) {
            displayLabel.setText(current.getQuestion());
            showingQuestion = true;
        } else {
            displayLabel.setText("No flashcards available.");
        }

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlashcardApp().setVisible(true));
    }

//belly

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
        private int currentindex = 0;

        public void addCard(Card card) {
            cards.add(card);
        }

        public ArrayList<Card> getCards() {
            return cards;
        }

        public Card getCurrentCard() {
            if (cards.isEmpty()) return null;
            return cards.get(currentindex);
        }

        public void nextCard() {
            if (!cards.isEmpty()) currentindex = (currentindex + 1) % cards.size();
        }

        public void prevCard() {
            if (!cards.isEmpty()) currentindex = (currentindex - 1 + cards.size()) % cards.size();
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

