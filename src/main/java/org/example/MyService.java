package org.example;
import java.sql.*;


public class MyService {

    public static final String dbPath = "./src/main/resources/Office/Office";

    static void setupConnection() throws SQLException, ClassNotFoundException {

        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:file:" + dbPath + ";DB_CLOSE_DELAY=-1";
        try (Connection con = DriverManager.getConnection(url)) {
            if (con != null) {
                System.out.println("Connection opened");
            } else {
                System.out.println("Failed to make connection");
            }
        }
    }
}
