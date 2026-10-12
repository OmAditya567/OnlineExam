package dao;

import model.Question;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    public void addQuestion(Question q, int examId) throws SQLException {
        String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, q.getText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, String.valueOf(q.getCorrectOption()));
            ps.executeUpdate();
        }
    }

    public void updateQuestion(int questionId, String text, String optA, String optB, String optC, String optD, char correct) throws SQLException {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_option = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setString(2, optA);
            ps.setString(3, optB);
            ps.setString(4, optC);
            ps.setString(5, optD);
            ps.setString(6, String.valueOf(correct));
            ps.setInt(7, questionId);
            ps.executeUpdate();
        }
    }

    public void deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        }
    }

    public List<Question> getQuestionsByExam(int examId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Question(rs.getInt("id"), rs.getString("question_text"),
                            rs.getString("option_a"), rs.getString("option_b"),
                            rs.getString("option_c"), rs.getString("option_d"),
                            rs.getString("correct_option").charAt(0)));
                }
            }
        }
        return list;
    }

    public List<Object[]> getQuestionRows() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT q.id, e.title, q.question_text, q.option_a, q.option_b, q.option_c, q.option_d, q.correct_option "
                   + "FROM questions q LEFT JOIN exams e ON q.exam_id = e.id ORDER BY q.id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String examTitle = rs.getString(2);
                rows.add(new Object[]{rs.getInt(1), examTitle == null ? "-" : examTitle, rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)});
            }
        }
        return rows;
    }
}