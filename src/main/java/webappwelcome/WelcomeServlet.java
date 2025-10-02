package webappwelcome;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/welcome")
public class WelcomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        if (session == null || session.getAttribute("username") == null) {
            // not logged in → redirect to login
            response.sendRedirect("login.html");
            return;
        }

        String username = (String) session.getAttribute("username");

		try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT name, email, role, location, last_login FROM users WHERE username=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String location = rs.getString("location");
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    String formattedTime = WelcomeServlet.formatTime(lastLogin);

                    response.getWriter().println("<!DOCTYPE html>");
                    response.getWriter().println("<html><head><meta charset='UTF-8'><title>Welcome</title>");
                    response.getWriter().println("<link rel='stylesheet' href='css/style.css'>");
                    response.getWriter().println("</head><body>");
                    response.getWriter().println("<div class='card welcome-container'>");
                    response.getWriter().println("<h2>Welcome, " + name + "!</h2>");
                    response.getWriter().println("<div class='user-info'>");
                    response.getWriter().println("<p><span>Email:</span> " + (email != null ? email : "N/A") + "</p>");
                    response.getWriter().println("<p><span>Role:</span> " + (role != null ? role : "N/A") + "</p>");
                    response.getWriter().println("<p><span>Location:</span> " + (location != null ? location : "N/A") + "</p>");
                    response.getWriter().println("<p><span>Last login:</span> " + formattedTime + "</p>");
                    response.getWriter().println("</div>");
                    response.getWriter().println("<a href='login.html'>Logout</a>");
                    response.getWriter().println("</div></body></html>");

                } else {
                    response.sendRedirect("login.html");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("DB error", e);
        }
    }
    
    private static String formatTime(Timestamp time) {
        if (time == null) {
            return "First login!";
        }

        ZonedDateTime userTime = time.toInstant()
            .atZone(ZoneId.of("America/New_York"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        return userTime.format(formatter);
    }

}
