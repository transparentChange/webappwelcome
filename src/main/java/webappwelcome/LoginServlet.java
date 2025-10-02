package webappwelcome;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
			try (Connection conn = DBUtil.getConnection()) {
	            String sql = "SELECT * FROM users WHERE username=?";
	            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	                stmt.setString(1, username);
	
	                ResultSet rs = stmt.executeQuery();
	                String storedHash = null;
	                if (rs.next()) {
	                	storedHash = rs.getString("password");
	                } 
	                if ((storedHash != null) && BCrypt.checkpw(password, storedHash)) {
	                	String updateSql = "UPDATE users SET last_login = NOW() WHERE username=?";
	                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
	                        updateStmt.setString(1, username);
	                        updateStmt.executeUpdate();
	                    }

	                    HttpSession session = request.getSession();
	                    session.setAttribute("username", rs.getString("username"));
	                    response.sendRedirect("welcome");
                	} else {
	                	 response.sendRedirect("login.html?error=1&username=" + URLEncoder.encode(username, "UTF-8"));
	                }
	            }
	        }
        } catch (SQLException e) {
            throw new ServletException("DB error", e);
        }
    }
}
