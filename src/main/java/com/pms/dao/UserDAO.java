package com.pms.dao;

import com.pms.model.User;
import com.pms.util.DBConnection;
import com.pms.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Data Access Object: all SQL for the users table lives here. */
public class UserDAO {

    /** True if a user with this email already exists. */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new user. The plain password is hashed before it is saved. */
    public boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, mobile, password) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getMobile());
            ps.setString(4, PasswordUtil.hash(user.getPassword()));
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Checks the email and password.
     * Returns the User (without the password hash) if correct, otherwise null.
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT uid, name, email, mobile, password FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                    User u = new User();
                    u.setUid(rs.getInt("uid"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setMobile(rs.getString("mobile"));
                    return u;          // password field deliberately left empty
                }
            }
        }
        return null;
    }
}
