package dao;

import model.Result;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {

    public void saveResult(int userId, int examId, int score, int total) throws SQLException {
        String sql = "INSERT INTO results (user_id, exam_id, score, total) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, examId);
            ps.setInt(3, score);
            ps.setInt(4, total);
            ps.executeUpdate();
        }
    }

    public List<Result> getResultsByUser(int userId) throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT e.title, r.score, r.total, r.attempted_on FROM results r "
                   + "JOIN exams e ON r.exam_id = e.id WHERE r.user_id = ? ORDER BY r.attempted_on DESC, r.id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("attempted_on");
                    list.add(new Result(rs.getString("title"), rs.getInt("score"), rs.getInt("total"),
                            ts == null ? "" : ts.toString()));
                }
            }
        }
        return list;
    }

    public List<Object[]> getAllResults() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.username, u.full_name, e.title, r.score, r.total, r.attempted_on " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN exams e ON r.exam_id = e.id " +
                     "ORDER BY r.attempted_on DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("attempted_on");
                int score = rs.getInt("score");
                int total = rs.getInt("total");
                double pct = total == 0 ? 0 : (score * 100.0) / total;
                list.add(new Object[]{
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("title"),
                    score + " / " + total,
                    String.format("%.1f%%", pct),
                    ts == null ? "" : ts.toString()
                });
            }
        }
        return list;
    }
}