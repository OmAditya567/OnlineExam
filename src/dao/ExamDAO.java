package dao;

import model.Exam;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    public List<Exam> getAllExams() throws SQLException {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT id, title, duration_minutes FROM exams ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Exam(rs.getInt("id"), rs.getString("title"), rs.getInt("duration_minutes")));
            }
        }
        return list;
    }

    public void addExam(String title, int durationMinutes) throws SQLException {
        String sql = "INSERT INTO exams (title, duration_minutes) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, durationMinutes);
            ps.executeUpdate();
        }
    }
}
