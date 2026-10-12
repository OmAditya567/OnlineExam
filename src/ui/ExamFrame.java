package ui;

import dao.ResultDAO;
import model.Exam;
import model.Question;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

public class ExamFrame extends JFrame {
    private final User user;
    private final Exam exam;
    private final List<Question> questions;
    private final char[] answers;          
    private int index = 0;
    private int secondsLeft;
    private boolean submitted = false;

    private final JLabel lblProgress = new JLabel();
    private final JLabel lblTimer = new JLabel();
    private final JLabel lblQuestion = new JLabel();
    private final JRadioButton[] options = new JRadioButton[4];
    private final ButtonGroup group = new ButtonGroup();
    private final JButton btnPrev = new JButton("< Previous");
    private final JButton btnNext = new JButton("Next >");
    private final JButton btnSubmit = new JButton("Submit Exam");
    private final javax.swing.Timer timer;

    public ExamFrame(User user, Exam exam, List<Question> questions) {
        this.user = user;
        this.exam = exam;
        this.questions = questions;
        this.answers = new char[questions.size()];
        this.secondsLeft = exam.getDurationMinutes() * 60;

        setTitle(exam.getTitle() + " - " + user.getFullName());
        setSize(760, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                int c = JOptionPane.showConfirmDialog(ExamFrame.this,
                        "Leave the exam? Your answers so far will be submitted.", "Exit exam",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) submitExam();
            }
        });

        // Top bar: progress + timer
        lblProgress.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 18));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(lblProgress, BorderLayout.WEST);
        top.add(lblTimer, BorderLayout.EAST);

        // Centre: question + options
        lblQuestion.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        lblQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblQuestion);
        center.add(Box.createVerticalStrut(20));
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("SansSerif", Font.PLAIN, 15));
            options[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(options[i]);
            center.add(options[i]);
            center.add(Box.createVerticalStrut(8));
        }

        // Bottom: navigation
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottom.add(btnPrev);
        bottom.add(btnNext);
        bottom.add(btnSubmit);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> move(-1));
        btnNext.addActionListener(e -> move(1));
        btnSubmit.addActionListener(e -> confirmAndSubmit());

        timer = new javax.swing.Timer(1000, e -> tick());
        updateTimerLabel();
        showQuestion();
        timer.start();
    }

    private void tick() {
        secondsLeft--;
        updateTimerLabel();
        if (secondsLeft <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Time is up! Your exam will be submitted now.");
            submitExam();
        }
    }

    private void updateTimerLabel() {
        lblTimer.setText(String.format("Time left: %02d:%02d", secondsLeft / 60, secondsLeft % 60));
        lblTimer.setForeground(secondsLeft <= 60 ? Color.RED : Color.BLACK);
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                answers[index] = (char) ('A' + i);
            }
        }
    }

    private void move(int delta) {
        saveCurrentAnswer();
        index += delta;
        showQuestion();
    }

    private void showQuestion() {
        Question q = questions.get(index);
        lblProgress.setText("Question " + (index + 1) + " of " + questions.size());
        lblQuestion.setText("<html><body style='width:620px'>" + (index + 1) + ". " + q.getText() + "</body></html>");
        options[0].setText("A.  " + q.getOptionA());
        options[1].setText("B.  " + q.getOptionB());
        options[2].setText("C.  " + q.getOptionC());
        options[3].setText("D.  " + q.getOptionD());
        group.clearSelection();
        if (answers[index] != 0) {
            options[answers[index] - 'A'].setSelected(true);
        }
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < questions.size() - 1);
    }

    private void confirmAndSubmit() {
        saveCurrentAnswer();
        int unanswered = 0;
        for (char a : answers) if (a == 0) unanswered++;
        String msg = unanswered > 0
                ? "You have " + unanswered + " unanswered question(s). Submit anyway?"
                : "Submit your exam?";
        int c = JOptionPane.showConfirmDialog(this, msg, "Submit exam", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) submitExam();
    }

    private void submitExam() {
        if (submitted) return;
        submitted = true;
        timer.stop();
        saveCurrentAnswer();

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (answers[i] == questions.get(i).getCorrectOption()) score++;
        }
        int total = questions.size();

        try {
            new ResultDAO().saveResult(user.getId(), exam.getId(), score, total);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save result: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        double pct = total == 0 ? 0 : score * 100.0 / total;
        JOptionPane.showMessageDialog(this,
                String.format("Exam submitted!%n%nScore: %d / %d (%.1f%%)", score, total, pct),
                "Result", JOptionPane.INFORMATION_MESSAGE);

        new DashboardFrame(user).setVisible(true);
        dispose();
    }
}