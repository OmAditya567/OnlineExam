package dao;

import model.User;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, full_name, role FROM users WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"),
                                    rs.getString("full_name"), rs.getString("role"));
                }
            }
        }
        return null;
    }

    public void addUser(String username, String password, String fullName, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    public List<User> getAllStudents() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, full_name, role FROM users WHERE role = 'STUDENT' ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(rs.getInt("id"), rs.getString("username"),
                                  rs.getString("full_name"), rs.getString("role")));
            }
        }
        return list;
    }

    public void deleteUser(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); 
            try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM results WHERE user_id = ?");
                 PreparedStatement ps2 = con.prepareStatement("DELETE FROM users WHERE id = ?")) {
                
                ps1.setInt(1, userId);
                ps1.executeUpdate();
                
                ps2.setInt(1, userId);
                ps2.executeUpdate();
                
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}