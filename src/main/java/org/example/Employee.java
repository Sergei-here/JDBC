package org.example;

import java.sql.*;

public class Employee {

    public static void main(String[] args) {
        Employee manager = new Employee();

        try {
            System.out.println("\n--- Задача 1 ---");
            Integer annId = manager.findAndUpdateAnnDepartment();

            System.out.println("\n--- Задача 2 ---");
            int correctedNames = manager.correctEmployeeNames();

            System.out.println("\n--- Задача 3 ---");
            int itEmployees = manager.countItDepartmentEmployees();

            System.out.println("\n=== Итоги выполнения ===");
            System.out.println("ID Ann (после обработки): " + (annId != null ? annId : "не найден или несколько"));
            System.out.println("Исправлено имен: " + correctedNames);
            System.out.println("Сотрудников в IT-отделе: " + itEmployees);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 1: Найти ID сотрудника с именем Ann и установить департамент в HR
    public Integer findAndUpdateAnnDepartment() throws SQLException {
        String url = "jdbc:h2:file:" + MyService.dbPath + ";DB_CLOSE_DELAY=-1";
        Integer annId = null;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Проверяем, сколько сотрудников с именем Ann
            String countQuery = "SELECT COUNT(*) as ann_count FROM Employee WHERE UPPER(Name) = 'ANN'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    int annCount = rs.getInt("ann_count");
                    System.out.println("Найдено сотрудников с именем 'Ann': " + annCount);

                    if (annCount == 1) {
                        // Находим ID сотрудника Ann
                        String findQuery = "SELECT e.ID, e.Name, d.Name as DepartmentName " +
                                "FROM Employee e " +
                                "LEFT JOIN Department d ON e.DepartmentID = d.ID " +
                                "WHERE UPPER(e.Name) = 'ANN'";

                        try (Statement findStmt = conn.createStatement();
                             ResultSet findRs = findStmt.executeQuery(findQuery)) {

                            if (findRs.next()) {
                                annId = findRs.getInt("ID");
                                String currentDept = findRs.getString("DepartmentName");

                                System.out.println("Найден сотрудник Ann:");
                                System.out.println("  ID: " + annId);
                                System.out.println("  Текущий департамент: " +
                                        (currentDept != null ? currentDept : "не указан"));

                                // Находим ID департамента HR
                                String hrQuery = "SELECT ID FROM Department WHERE UPPER(Name) = 'HR'";
                                try (Statement hrStmt = conn.createStatement();
                                     ResultSet hrRs = hrStmt.executeQuery(hrQuery)) {

                                    if (hrRs.next()) {
                                        int hrDeptId = hrRs.getInt("ID");

                                        // Обновляем департамент сотрудника
                                        String updateQuery = "UPDATE Employee SET DepartmentID = ? WHERE ID = ?";
                                        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                                            pstmt.setInt(1, hrDeptId);
                                            pstmt.setInt(2, annId);

                                            int rowsUpdated = pstmt.executeUpdate();
                                            if (rowsUpdated > 0) {
                                                System.out.println("Успешно обновлен департамент на HR (DepartmentID: " + hrDeptId + ")");
                                            }
                                        }
                                    } else {
                                        System.out.println("Департамент 'HR' не найден в таблице Department");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return annId;
    }

    // Задача 2: Исправить имена с маленькой буквы
    public int correctEmployeeNames() throws SQLException {
        String url = "jdbc:h2:file:" + MyService.dbPath + ";DB_CLOSE_DELAY=-1";
        int correctedCount = 0;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Оптимизированный запрос для H2
            String updateQuery =
                    "UPDATE Employee " +
                            "SET Name = CONCAT(UPPER(SUBSTRING(Name, 1, 1)), SUBSTRING(Name, 2)) " +
                            "WHERE Name IS NOT NULL " +
                            "AND LENGTH(Name) > 0 " +
                            "AND SUBSTRING(Name, 1, 1) BETWEEN 'a' AND 'z'";

            try (Statement stmt = conn.createStatement()) {
                correctedCount = stmt.executeUpdate(updateQuery);
                System.out.println("Исправлено имен с маленькой буквы: " + correctedCount);

                // Показываем примеры исправлений
                if (correctedCount > 0) {
                    String examplesQuery =
                            "SELECT ID, Name FROM Employee " +
                                    "WHERE Name IS NOT NULL " +
                                    "ORDER BY ID " +
                                    "LIMIT 5";

                    try (Statement exStmt = conn.createStatement();
                         ResultSet exRs = exStmt.executeQuery(examplesQuery)) {

                        System.out.println("Примеры имен после исправления:");
                        while (exRs.next()) {
                            System.out.println("  ID: " + exRs.getInt("ID") +
                                    ", Имя: " + exRs.getString("Name"));
                        }
                    }
                }
            }
        }

        return correctedCount;
    }

    // Задача 3: Вывести количество сотрудников в IT-отделе
    public int countItDepartmentEmployees() throws SQLException {
        String url = "jdbc:h2:file:" + MyService.dbPath + ";DB_CLOSE_DELAY=-1";
        int itCount = 0;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Запрос с JOIN для подсчета сотрудников IT-отдела
            String countQuery =
                    "SELECT COUNT(*) as it_count " +
                            "FROM Employee e " +
                            "INNER JOIN Department d ON e.DepartmentID = d.ID " +
                            "WHERE UPPER(d.Name) LIKE '%IT%'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    itCount = rs.getInt("it_count");
                    System.out.println("Общее количество сотрудников в IT-отделе: " + itCount);
                }
            }
        }
        return itCount;
    }
}

/*

Подключитесь программно к базе данных и выполните следующие операции:

Найдите ID сотрудника с именем Ann. Если такой сотрудник только один, то установите его
департамент в HR.
Проверьте имена всех сотрудников. Если чьё-то имя написано с маленькой буквы, исправьте её на большую. Выведите на экран количество исправленных имён.
Выведите на экран количество сотрудников в IT-отделе
Каждая задача
оценивается в один балл. Ответ оформите в виде Java-метода c кодом запроса. Максимум 3 балла.

 */