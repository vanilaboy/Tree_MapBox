import IO.CustomBD;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by root on 01.11.18 with love.
 */
public class Login extends HttpServlet {

    CustomBD customBD = new CustomBD();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String mainURI = "";

        boolean loggedIn = session != null && session.getAttribute("username") != null;
        if(loggedIn) {
            response.sendRedirect(mainURI);
        } else {
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if(username != null || password != null) {
            try {
                if(customBD.checkUser(username, password)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("username", username);
                    response.sendRedirect("/map");
                } else {
                    response.sendRedirect("wrongPassword.html");
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("wrongPassword.html");
        }
    }
}
