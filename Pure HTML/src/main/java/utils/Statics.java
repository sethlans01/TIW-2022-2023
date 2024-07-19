package utils;

import beans.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

public class Statics {

    // Check if the current client is logged in
    public static boolean checkAccess(HttpSession session) throws ServletException {

        User current = (User) session.getAttribute("currentUser");
        return current != null;

    }


}