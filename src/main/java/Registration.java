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
public class Registration extends HttpServlet {

    CustomBD customBD = new CustomBD();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String address = request.getParameter("address");

        if (email != null && username != null && password != null) {
            try {
                if (customBD.addUser(username, password, email, name, address)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("username", username);
                    response.sendRedirect("/map");
                } else {
                    response.sendRedirect("usernameExist.html");
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("wrongPassword.html");
        }
    }
}
