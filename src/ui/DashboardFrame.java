package ui;

import dao.ExamDAO;
import dao.QuestionDAO;
import dao.ResultDAO;
import dao.UserDAO;
import model.Exam;
import model.Question;
import model.Result;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final User user;
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final ResultDAO resultDAO = new ResultDAO();
    private final UserDAO userDAO = new UserDAO();

    private final DefaultComboBoxModel<Exam> examModel = new DefaultComboBoxModel<>();

    private final DefaultTableModel questionTableModel =
            new DefaultTableModel(new String[]{"ID", "Exam", "Question", "A", "B", "C", "D", "Answer"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    private final DefaultTableModel resultTableModel =
            new DefaultTableModel(new String[]{"Exam", "Score", "Total", "Percentage", "Date"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    private final DefaultTableModel adminResultTableModel =
            new DefaultTableModel(new String[]{"Username", "Name", "Exam", "Score", "Percentage", "Date"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    private final DefaultTableModel studentTableModel =
            new DefaultTableModel(new String[]{"User ID", "Username", "Full Name"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    private final DefaultComboBoxModel<Exam> statusExamModel = new DefaultComboBoxModel<>();
    private final JLabel lblStatusSummary = new JLabel(" ");
    private final DefaultTableModel statusTableModel =
            new DefaultTableModel(new String[]{"Username", "Name", "Status", "Latest Score", "Attempts", "Last Attempt"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("Dashboard - " + user.getFullName() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        Theme.frame(this);

        JLabel welcome = new JLabel("Welcome, " + user.getFullName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        welcome.setForeground(Color.WHITE);
        btnLogout.putClientProperty("variant", "ghost");
        JPanel header = new Modern.GradientPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        header.add(welcome, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (user.isAdmin()) {
            tabs.addTab("Add Question", buildAddQuestionPanel());
            tabs.addTab("Manage Questions", buildQuestionViewPanel());
            tabs.addTab("All Student Results", buildAdminResultsPanel());
            tabs.addTab("Manage Students", buildManageStudentsPanel());
            tabs.addTab("Exam Status", buildExamStatusPanel());
            reloadExams();
            loadQuestions();
            loadAdminResults();
            loadStudents();
        } else {
            tabs.addTab("Take Exam", buildTakeExamPanel());
            tabs.addTab("My Results", buildResultsPanel());
            reloadExams();
            loadResults();
        }
        add(tabs, BorderLayout.CENTER);
        Modern.apply(getContentPane());
    }


    private JPanel buildAddQuestionPanel() {
        JComboBox<Exam> cmbExam = new JComboBox<>(examModel);
        JButton btnNewExam = new JButton("New Exam");
        JPanel examRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        examRow.add(cmbExam);
        examRow.add(Box.createHorizontalStrut(8));
        examRow.add(btnNewExam);

        JTextField txtQ = new JTextField(40);
        JTextField txtA = new JTextField(20), txtB = new JTextField(20),
                   txtC = new JTextField(20), txtD = new JTextField(20);
        JComboBox<String> cmbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JButton btnSave = new JButton("Save Question");

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;
        String[] labels = {"Exam:", "Question:", "Option A:", "Option B:", "Option C:", "Option D:", "Correct:"};
        JComponent[] fields = {examRow, txtQ, txtA, txtB, txtC, txtD, cmbCorrect};
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; p.add(new JLabel(labels[i]), c);
            c.gridx = 1; p.add(fields[i], c);
        }
        c.gridx = 1; c.gridy = labels.length; p.add(btnSave, c);

        btnNewExam.addActionListener(e -> createExam());

        btnSave.addActionListener(e -> {
            Exam exam = (Exam) cmbExam.getSelectedItem();
            if (exam == null) {
                JOptionPane.showMessageDialog(this, "Create an exam first.", "No exam", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtQ.getText().trim().isEmpty() || txtA.getText().trim().isEmpty() || txtB.getText().trim().isEmpty()
                    || txtC.getText().trim().isEmpty() || txtD.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields.", "Missing input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Question q = new Question(0, txtQ.getText().trim(), txtA.getText().trim(), txtB.getText().trim(),
                    txtC.getText().trim(), txtD.getText().trim(),
                    ((String) cmbCorrect.getSelectedItem()).charAt(0));
            try {
                questionDAO.addQuestion(q, exam.getId());
                JOptionPane.showMessageDialog(this, "Question saved.");
                for (JTextField f : new JTextField[]{txtQ, txtA, txtB, txtC, txtD}) f.setText("");
                loadQuestions();
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });
        return p;
    }

    private void createExam() {
        String title = JOptionPane.showInputDialog(this, "Exam title:");
        if (title == null || title.trim().isEmpty()) return;
        String dur = JOptionPane.showInputDialog(this, "Duration in minutes:", "30");
        if (dur == null) return;
        try {
            int minutes = Integer.parseInt(dur.trim());
            if (minutes <= 0) throw new NumberFormatException();
            examDAO.addExam(title.trim(), minutes);
            reloadExams();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration must be a positive number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JPanel buildQuestionViewPanel() {
        JTable table = new JTable(questionTableModel);
        
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");

        btnRefresh.addActionListener(e -> loadQuestions());

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a question to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qId = (Integer) table.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this question?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    questionDAO.deleteQuestion(qId);
                    loadQuestions();
                } catch (SQLException ex) {
                    showDbError(ex);
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a question to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int qId = (Integer) table.getValueAt(row, 0);
            JTextField txtQ = new JTextField((String) table.getValueAt(row, 2));
            JTextField txtA = new JTextField((String) table.getValueAt(row, 3));
            JTextField txtB = new JTextField((String) table.getValueAt(row, 4));
            JTextField txtC = new JTextField((String) table.getValueAt(row, 5));
            JTextField txtD = new JTextField((String) table.getValueAt(row, 6));
            
            JComboBox<String> cmbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});
            cmbCorrect.setSelectedItem(table.getValueAt(row, 7).toString());

            Object[] message = {
                "Question Text:", txtQ,
                "Option A:", txtA,
                "Option B:", txtB,
                "Option C:", txtC,
                "Option D:", txtD,
                "Correct Answer:", cmbCorrect
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Edit Question", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    questionDAO.updateQuestion(qId, txtQ.getText().trim(), txtA.getText().trim(), 
                        txtB.getText().trim(), txtC.getText().trim(), txtD.getText().trim(), 
                        ((String) cmbCorrect.getSelectedItem()).charAt(0));
                    loadQuestions();
                } catch (SQLException ex) {
                    showDbError(ex);
                }
            }
        });

        JPanel bottomControls = new JPanel(new FlowLayout());
        bottomControls.add(btnEdit);
        bottomControls.add(btnDelete);
        bottomControls.add(btnRefresh);

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(bottomControls, BorderLayout.SOUTH);
        return p;
    }

    private void loadQuestions() {
        try {
            questionTableModel.setRowCount(0);
            for (Object[] row : questionDAO.getQuestionRows()) {
                questionTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JPanel buildAdminResultsPanel() {
        JTable table = new JTable(adminResultTableModel);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadAdminResults());
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnRefresh, BorderLayout.SOUTH);
        return p;
    }

    private void loadAdminResults() {
        try {
            adminResultTableModel.setRowCount(0);
            for (Object[] row : resultDAO.getAllResults()) {
                adminResultTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JPanel buildManageStudentsPanel() {
        JTextField txtUser = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JTextField txtName = new JTextField(15);
        JButton btnAdd = new JButton("Add Student");

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Student"));
        formPanel.add(new JLabel("Username:")); formPanel.add(txtUser);
        formPanel.add(new JLabel("Password:")); formPanel.add(txtPass);
        formPanel.add(new JLabel("Name:")); formPanel.add(txtName);
        formPanel.add(btnAdd);

        btnAdd.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            String n = txtName.getText().trim();

            if (u.isEmpty() || pass.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
                return;
            }
            try {
                userDAO.addUser(u, pass, n, "STUDENT");
                JOptionPane.showMessageDialog(this, "Student added.");
                txtUser.setText(""); txtPass.setText(""); txtName.setText("");
                loadStudents();
                loadExamStatus();
            } catch (SQLException ex) {
                showDbError(ex);
            }
        });

        JTable table = new JTable(studentTableModel);
        JButton btnDelete = new JButton("Delete Selected Student");
        JButton btnRefresh = new JButton("Refresh");

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sId = (Integer) table.getValueAt(row, 0);
            String sName = (String) table.getValueAt(row, 2);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to completely remove " + sName + "?\nAll their exam history will also be permanently deleted.", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    userDAO.deleteUser(sId);
                    loadStudents();
                    loadAdminResults();
                    loadExamStatus();
                } catch (SQLException ex) {
                    showDbError(ex);
                }
            }
        });

        btnRefresh.addActionListener(e -> loadStudents());

        JPanel tableBottom = new JPanel(new FlowLayout());
        tableBottom.add(btnDelete);
        tableBottom.add(btnRefresh);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Existing Students"));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(tableBottom, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout());
        p.add(formPanel, BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildExamStatusPanel() {
        JComboBox<Exam> cmbExam = new JComboBox<>(statusExamModel);
        JButton btnRefresh = new JButton("Refresh");
        cmbExam.addActionListener(e -> loadExamStatus());
        btnRefresh.addActionListener(e -> loadExamStatus());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Exam:"));
        top.add(cmbExam);
        top.add(btnRefresh);
        top.add(lblStatusSummary);

        JTable table = new JTable(statusTableModel);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                if (!sel) comp.setForeground("Not attempted".equals(v) ? Theme.RED : new Color(0x2E7D32));
                return comp;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadExamStatus() {
        Exam exam = (Exam) statusExamModel.getSelectedItem();
        statusTableModel.setRowCount(0);
        if (exam == null) {
            lblStatusSummary.setText(" ");
            return;
        }
        try {
            int done = 0;
            for (Object[] row : resultDAO.getExamStatus(exam.getId())) {
                statusTableModel.addRow(row);
                if ("Attempted".equals(row[2])) done++;
            }
            int total = statusTableModel.getRowCount();
            lblStatusSummary.setText(done + " of " + total + " attempted, " + (total - done) + " pending");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void loadStudents() {
        try {
            studentTableModel.setRowCount(0);
            for (User u : userDAO.getAllStudents()) {
                studentTableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName()});
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }


    private JPanel buildTakeExamPanel() {
        JComboBox<Exam> cmbExam = new JComboBox<>(examModel);
        JButton btnStart = new JButton("Start Exam");

        JLabel info = new JLabel("<html><body style='width:420px'>Select an exam and click <b>Start Exam</b>. "
                + "The timer starts immediately. You can move between questions and change answers "
                + "before you submit. The exam is auto-submitted when time runs out.</body></html>");

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.gridx = 0; c.gridy = 0; p.add(info, c);
        c.gridy = 1; p.add(cmbExam, c);
        c.gridy = 2; p.add(btnStart, c);

        btnStart.addActionListener(e -> startExam((Exam) cmbExam.getSelectedItem()));
        return p;
    }

    private void startExam(Exam exam) {
        if (exam == null) {
            JOptionPane.showMessageDialog(this, "No exams available yet.", "No exam", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Question> questions = questionDAO.getQuestionsByExam(exam.getId());
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This exam has no questions yet.", "Empty exam", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new ExamFrame(user, exam, questions).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JPanel buildResultsPanel() {
        JTable table = new JTable(resultTableModel);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadResults());
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnRefresh, BorderLayout.SOUTH);
        return p;
    }

    private void loadResults() {
        try {
            resultTableModel.setRowCount(0);
            for (Result r : resultDAO.getResultsByUser(user.getId())) {
                resultTableModel.addRow(new Object[]{r.getExamTitle(), r.getScore(), r.getTotal(),
                        String.format("%.1f%%", r.getPercentage()), r.getAttemptedOn()});
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }



    private void reloadExams() {
        try {
            examModel.removeAllElements();
            statusExamModel.removeAllElements();
            for (Exam e : examDAO.getAllExams()) {
                examModel.addElement(e);
                statusExamModel.addElement(e);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}