import java.sql.*;
import java.util.Scanner;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
/*This is my Project */
/**
 * =========================================================
 *   Employee Management System  —  JDBC + Oracle Database
 *   Version  : 2.0  (Interview Demo Build)
 *   Features : Login/Auth | Menu UI | CRUD | Search/Filter
 *              | Export CSV | Audit Log | Input Validation
 *              | PreparedStatements | Department Summary
 * =========================================================
 */
public class EmployeeManagementSystem {

    // ─────────────────────────────────────────────────────
    //  DATABASE CONFIGURATION
    // ─────────────────────────────────────────────────────
    private static final String DRIVER  = "oracle.jdbc.OracleDriver";
    private static final String DB_URL  = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DB_USER = "system";
    private static final String DB_PWD  = "LION";

    // ─────────────────────────────────────────────────────
    //  APPLICATION LOGIN CREDENTIALS
    //  (In production, use hashed passwords — e.g. BCrypt)
    // ─────────────────────────────────────────────────────
    private static final String APP_USERNAME = "admin";
    private static final String APP_PASSWORD = "Admin@123";
    private static final int    MAX_ATTEMPTS = 3;

    // ─────────────────────────────────────────────────────
    //  ANSI COLOR CODES  (for colored terminal output)
    // ─────────────────────────────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String WHITE  = "\u001B[37m";
    private static final String DIM    = "\u001B[2m";

    private final Scanner sc = new Scanner(System.in);

    // =========================================================
    //   SECTION 1 : UTILITY HELPERS
    // =========================================================

    /** Prints a styled horizontal rule */
    private void rule() {
        System.out.println(DIM + CYAN +
            "  ════════════════════════════════════════════════════" + RESET);
    }

    /** Prints a section header box */
    private void header(String title) {
        System.out.println();
        rule();
        System.out.println("  " + BOLD + CYAN + "  " + title + RESET);
        rule();
    }

    /** Prints a success message */
    private void success(String msg) {
        System.out.println("\n  " + GREEN + BOLD + "✔  " + RESET + GREEN + msg + RESET);
    }

    /** Prints an error message */
    private void error(String msg) {
        System.out.println("\n  " + RED + BOLD + "✘  " + RESET + RED + msg + RESET);
    }

    /** Prints an info/warning line */
    private void info(String msg) {
        System.out.println("  " + YELLOW + "▸  " + msg + RESET);
    }

    /** Safely reads a non-empty string from the user */
    private String readString(String prompt) {
        String val;
        do {
            System.out.print("  " + YELLOW + prompt + RESET + " ");
            val = sc.nextLine().trim();
            if (val.isEmpty()) error("This field cannot be empty.");
        } while (val.isEmpty());
        return val;
    }

    /** Safely reads an integer from the user with validation */
    private int readInt(String prompt) {
        while (true) {
            System.out.print("  " + YELLOW + prompt + RESET + " ");
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val < 0) { error("Value cannot be negative."); continue; }
                return val;
            } catch (NumberFormatException e) {
                error("Please enter a valid whole number.");
            }
        }
    }

    /** Safely reads a double from the user */
    private double readDouble(String prompt) {
        while (true) {
            System.out.print("  " + YELLOW + prompt + RESET + " ");
            try {
                double val = Double.parseDouble(sc.nextLine().trim());
                if (val <= 0) { error("Value must be greater than 0."); continue; }
                return val;
            } catch (NumberFormatException e) {
                error("Please enter a valid number (e.g. 10.5).");
            }
        }
    }

    /** Asks a YES/NO confirmation, returns true for Y */
    private boolean confirm(String question) {
        System.out.print("\n  " + BOLD + YELLOW + "?  " + question + " (Y/N): " + RESET);
        return sc.nextLine().trim().equalsIgnoreCase("Y");
    }

    // =========================================================
    //   SECTION 2 : DATABASE CONNECTION
    // =========================================================

    /**
     * Establishes and returns a DB connection.
     * Uses Class.forName() for driver loading and
     * DriverManager.getConnection() with credentials.
     *
     * @return Connection object, or null on failure
     */
    private Connection connect() {
        try {
            Class.forName(DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
            return con;
        } catch (ClassNotFoundException e) {
            error("Oracle JDBC Driver not found in classpath.");
        } catch (SQLException e) {
            error("Database connection failed: " + e.getMessage());
        }
        return null;
    }

    /** Closes a connection safely (null-check included) */
    private void close(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    // =========================================================
    //   SECTION 3 : AUTHENTICATION SYSTEM
    // =========================================================

    /**
     * Simple login gate with attempt limiting.
     * Demonstrates: loop control, string comparison, security patterns.
     *
     * @return true if login succeeds, false after MAX_ATTEMPTS failures
     */
    private boolean login() {
        System.out.println();
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║        EMPLOYEE MANAGEMENT SYSTEM  v2.0         ║");
        System.out.println("  ║              Secure Login Portal                 ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println(RESET);

        int attemptsLeft = MAX_ATTEMPTS;

        while (attemptsLeft > 0) {
            System.out.print("  " + YELLOW + "Username : " + RESET);
            String user = sc.nextLine().trim();
            System.out.print("  " + YELLOW + "Password : " + RESET);
            String pass = sc.nextLine().trim();

            if (user.equals(APP_USERNAME) && pass.equals(APP_PASSWORD)) {
                success("Login successful!  Welcome, " + user.toUpperCase() + ".");
                auditLog("LOGIN", "—", "User '" + user + "' logged in");
                return true;
            }

            attemptsLeft--;
            if (attemptsLeft > 0) {
                error("Invalid credentials.  Attempts remaining: " + attemptsLeft);
            }
        }

        error("Account locked after " + MAX_ATTEMPTS + " failed attempts.");
        auditLog("LOGIN_FAILED", "—", "Account locked after " + MAX_ATTEMPTS + " failed attempts");
        return false;
    }

    // =========================================================
    //   SECTION 4 : MAIN MENU  (Menu-Driven UI)
    // =========================================================

    /**
     * Interactive loop-based menu.
     * Runs continuously until user chooses Exit (0).
     */
    private void showMenu() {
        while (true) {
            System.out.println();
            System.out.println(CYAN + BOLD +
                "  ╔══════════════════════════════════════════════════╗\n" +
                "  ║           EMPLOYEE MANAGEMENT — MAIN MENU       ║\n" +
                "  ╠══════════════════════════════════════════════════╣" + RESET);

            String[][] options = {
                {"1", "➕", "Insert New Employee"},
                {"2", "📋", "View All Employees"},
                {"3", "🔍", "Search / Filter Employees"},
                {"4", "✏️ ", "Update Employee Salary"},
                {"5", "📈", "Apply Salary Raise (%)"},
                {"6", "🗑️ ", "Delete Employee"},
                {"7", "📊", "Department-wise Summary"},
                {"8", "💾", "Export Employees to CSV"},
                {"9", "📜", "View Audit Log"},
                {"0", "🚪", "Logout & Exit"}
            };

            for (String[] opt : options) {
                System.out.printf("  " + CYAN + "║" + RESET +
                    "  [" + BOLD + YELLOW + "%s" + RESET + "]  %s  %-32s" +
                    CYAN + "║%n" + RESET,
                    opt[0], opt[1], opt[2]);
            }
            System.out.println(CYAN + BOLD +
                "  ╚══════════════════════════════════════════════════╝" + RESET);

            System.out.print("\n  " + BOLD + YELLOW + "›  Choose an option: " + RESET);
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> insertEmployee();
                case "2" -> viewAllEmployees();
                case "3" -> searchMenu();
                case "4" -> updateSalary();
                case "5" -> applySalaryRaise();
                case "6" -> deleteEmployee();
                case "7" -> departmentSummary();
                case "8" -> exportToCSV();
                case "9" -> viewAuditLog();
                case "0" -> {
                    auditLog("LOGOUT", "—", "User logged out");
                    success("Logged out successfully. Goodbye!");
                    return;
                }
                default  -> error("Invalid option. Please choose a number from the menu.");
            }
        }
    }

    // =========================================================
    //   SECTION 5 : INSERT EMPLOYEE
    // =========================================================

    /**
     * Inserts a new employee record.
     * Demonstrates: PreparedStatement (SQL injection prevention),
     *               input validation, duplicate key handling.
     */
    private void insertEmployee() {
        header("➕  INSERT NEW EMPLOYEE");

        Connection con = connect();
        if (con == null) return;

        try {
            String id     = readString("Employee ID    :");
            String fname  = readString("First Name     :");
            String lname  = readString("Last Name      :");
            int    salary = readInt   ("Salary (₹)     :");
            String addr   = readString("Address        :");
            String dept   = readString("Department     :");
            String email  = readString("Email          :");

            // Show summary before saving
            System.out.println();
            info("Review before saving:");
            printEmployeeBox(id, fname, lname, salary, addr, dept, email);

            if (!confirm("Save this employee?")) {
                info("Insert cancelled."); return;
            }

            String sql = "INSERT INTO employee(eid, efname, elname, esal, eaddr, edept, eemail) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setInt   (4, salary);
            ps.setString(5, addr);
            ps.setString(6, dept);
            ps.setString(7, email);

            int rows = ps.executeUpdate();
            if (rows == 1) {
                success("Employee inserted successfully!");
                auditLog("INSERT", id, fname + " " + lname + " added to " + dept);
            } else {
                error("Insert failed unexpectedly.");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            error("Duplicate Employee ID! That ID already exists in the database.");
            if (confirm("Try inserting again?")) insertEmployee();

        } catch (SQLException e) {
            error("Database error: " + e.getMessage());
        } finally {
            close(con);
        }
    }

    /** Prints a mini employee summary box */
    private void printEmployeeBox(String id, String fname, String lname,
                                   int sal, String addr, String dept, String email) {
        System.out.println(DIM + "  ┌─────────────────────────────────────────┐" + RESET);
        System.out.printf ("  │  %-10s %s %s%n", "ID:", id, "");
        System.out.printf ("  │  %-10s %s %s%n", "Name:", fname + " " + lname, "");
        System.out.printf ("  │  %-10s ₹%,d%n",  "Salary:", sal);
        System.out.printf ("  │  %-10s %s%n",    "Dept:", dept);
        System.out.printf ("  │  %-10s %s%n",    "Email:", email);
        System.out.println(DIM + "  └─────────────────────────────────────────┘" + RESET);
    }

    // =========================================================
    //   SECTION 6 : VIEW ALL EMPLOYEES
    // =========================================================

    private void viewAllEmployees() {
        header("📋  ALL EMPLOYEES");
        Connection con = connect();
        if (con == null) return;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery("SELECT * FROM employee ORDER BY edept, efname");
            printTable(rs);
        } catch (SQLException e) {
            error("Could not retrieve data: " + e.getMessage());
        } finally {
            close(con);
        }
    }

    /** Renders a formatted ASCII table from a ResultSet */
    private void printTable(ResultSet rs) throws SQLException {
        String line = "  " + "─".repeat(90);
        System.out.println(line);
        System.out.printf(BOLD + CYAN +
            "  %-8s  %-14s %-14s  %10s  %-18s %-14s %-20s%n" + RESET,
            "ID", "First Name", "Last Name", "Salary", "Address", "Department", "Email");
        System.out.println(line);

        int count = 0;
        while (rs.next()) {
            System.out.printf(
                "  %-8s  %-14s %-14s  %9s  %-18s %-14s %-20s%n",
                rs.getString("eid"),
                rs.getString("efname"),
                rs.getString("elname"),
                "₹" + String.format("%,d", rs.getInt("esal")),
                rs.getString("eaddr"),
                rs.getString("edept"),
                rs.getString("eemail"));
            count++;
        }
        System.out.println(line);
        System.out.println(DIM + "  Total records: " + count + RESET);
    }

    // =========================================================
    //   SECTION 7 : SEARCH & FILTER MENU
    // =========================================================

    /**
     * Sub-menu offering multiple search strategies.
     * Demonstrates: LIKE queries, WHERE clauses, parameterized search.
     */
    private void searchMenu() {
        header("🔍  SEARCH & FILTER EMPLOYEES");
        System.out.println("  [1]  Search by Employee ID");
        System.out.println("  [2]  Search by Name (partial match)");
        System.out.println("  [3]  Filter by Department");
        System.out.println("  [4]  Filter by Salary Range");
        System.out.println("  [0]  Back to Main Menu");

        System.out.print("\n  " + YELLOW + "›  Choose: " + RESET);
        switch (sc.nextLine().trim()) {
            case "1" -> searchById();
            case "2" -> searchByName();
            case "3" -> filterByDept();
            case "4" -> filterBySalaryRange();
            case "0" -> { }
            default  -> error("Invalid option.");
        }
    }

    private void searchById() {
        String id = readString("Enter Employee ID:");
        Connection con = connect();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM employee WHERE eid = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) printTable(rs);
            else error("No employee found with ID: " + id);
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    private void searchByName() {
        String keyword = readString("Enter name keyword:");
        Connection con = connect();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM employee WHERE LOWER(efname) LIKE ? OR LOWER(elname) LIKE ?");
            String pattern = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) printTable(rs);
            else error("No employees found matching: \"" + keyword + "\"");
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    private void filterByDept() {
        String dept = readString("Enter Department name:");
        Connection con = connect();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM employee WHERE LOWER(edept) LIKE ? ORDER BY efname");
            ps.setString(1, "%" + dept.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) printTable(rs);
            else error("No employees found in department: " + dept);
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    private void filterBySalaryRange() {
        int min = readInt("Minimum Salary (₹):");
        int max = readInt("Maximum Salary (₹):");
        if (min > max) { error("Minimum cannot exceed maximum."); return; }
        Connection con = connect();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM employee WHERE esal BETWEEN ? AND ? ORDER BY esal DESC");
            ps.setInt(1, min);
            ps.setInt(2, max);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) printTable(rs);
            else error("No employees found in salary range ₹" + min + " – ₹" + max);
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 8 : UPDATE SALARY
    // =========================================================

    private void updateSalary() {
        header("✏️   UPDATE SALARY");
        Connection con = connect();
        if (con == null) return;
        try {
            String id = readString("Employee ID:");

            // Fetch and show current salary
            PreparedStatement fetch = con.prepareStatement(
                "SELECT efname, elname, esal FROM employee WHERE eid = ?");
            fetch.setString(1, id);
            ResultSet rs = fetch.executeQuery();

            if (!rs.next()) { error("Employee not found: " + id); return; }

            String name   = rs.getString("efname") + " " + rs.getString("elname");
            int    curSal = rs.getInt("esal");
            info("Current salary of " + name + " : ₹" + String.format("%,d", curSal));

            int newSal = readInt("New Salary (₹)  :");
            if (!confirm("Update salary to ₹" + String.format("%,d", newSal) + "?")) {
                info("Update cancelled."); return;
            }

            PreparedStatement ps = con.prepareStatement(
                "UPDATE employee SET esal = ? WHERE eid = ?");
            ps.setInt   (1, newSal);
            ps.setString(2, id);

            if (ps.executeUpdate() == 1) {
                success("Salary updated: ₹" + String.format("%,d", curSal) +
                        "  →  ₹" + String.format("%,d", newSal));
                auditLog("UPDATE_SAL", id, name + " salary: " + curSal + " → " + newSal);
            } else {
                error("Update failed.");
            }
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 9 : SALARY RAISE %
    // =========================================================

    private void applySalaryRaise() {
        header("📈  APPLY SALARY RAISE");
        info("Enter an Employee ID  — OR —  type ALL to raise everyone's salary.");

        String id = readString("Employee ID / ALL:");
        double pct = readDouble("Raise percentage  (e.g. 10 for 10%):");

        if (!confirm("Apply " + pct + "% raise to " +
                (id.equalsIgnoreCase("ALL") ? "ALL employees" : "Employee #" + id) + "?")) {
            info("Raise cancelled."); return;
        }

        Connection con = connect();
        if (con == null) return;
        try {
            int rows;
            double multiplier = 1 + (pct / 100.0);

            if (id.equalsIgnoreCase("ALL")) {
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE employee SET esal = ROUND(esal * ?)");
                ps.setDouble(1, multiplier);
                rows = ps.executeUpdate();
                auditLog("RAISE_ALL", "ALL", pct + "% raise applied to " + rows + " employees");
            } else {
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE employee SET esal = ROUND(esal * ?) WHERE eid = ?");
                ps.setDouble(1, multiplier);
                ps.setString(2, id);
                rows = ps.executeUpdate();
                auditLog("RAISE", id, pct + "% salary raise applied");
            }

            if (rows > 0) success(pct + "% raise applied to " + rows + " employee(s).");
            else error("No records were updated. Check the Employee ID.");

        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 10 : DELETE EMPLOYEE
    // =========================================================

    private void deleteEmployee() {
        header("🗑️   DELETE EMPLOYEE");
        Connection con = connect();
        if (con == null) return;
        try {
            String id = readString("Employee ID to delete:");

            PreparedStatement fetch = con.prepareStatement(
                "SELECT * FROM employee WHERE eid = ?");
            fetch.setString(1, id);
            ResultSet rs = fetch.executeQuery();

            if (!rs.next()) { error("No employee found with ID: " + id); return; }

            info("You are about to permanently delete:");
            System.out.printf("  " + RED + "  %-8s  %s %s  |  Dept: %s%n" + RESET,
                rs.getString("eid"),
                rs.getString("efname"),
                rs.getString("elname"),
                rs.getString("edept"));

            System.out.print("\n  " + BOLD + RED +
                "⚠  Type DELETE to confirm, or anything else to cancel: " + RESET);
            String confirm = sc.nextLine().trim();

            if (!confirm.equals("DELETE")) {
                info("Delete cancelled."); return;
            }

            PreparedStatement ps = con.prepareStatement(
                "DELETE FROM employee WHERE eid = ?");
            ps.setString(1, id);

            if (ps.executeUpdate() == 1) {
                success("Employee " + id + " deleted successfully.");
                auditLog("DELETE", id, rs.getString("efname") + " " +
                         rs.getString("elname") + " removed");
            } else {
                error("Deletion failed.");
            }
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 11 : DEPARTMENT-WISE SUMMARY
    // =========================================================

    private void departmentSummary() {
        header("📊  DEPARTMENT-WISE SUMMARY");
        Connection con = connect();
        if (con == null) return;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(
                "SELECT edept, " +
                "       COUNT(*)          AS total, " +
                "       MIN(esal)         AS min_sal, " +
                "       MAX(esal)         AS max_sal, " +
                "       ROUND(AVG(esal))  AS avg_sal, " +
                "       SUM(esal)         AS payroll " +
                "FROM   employee " +
                "GROUP  BY edept " +
                "ORDER  BY edept");

            String line = "  " + "─".repeat(78);
            System.out.println(line);
            System.out.printf(BOLD + CYAN +
                "  %-18s  %6s  %12s  %12s  %12s  %14s%n" + RESET,
                "Department", "Staff", "Min Salary", "Max Salary", "Avg Salary", "Total Payroll");
            System.out.println(line);

            while (rs.next()) {
                System.out.printf(
                    "  %-18s  %6d  %12s  %12s  %12s  %14s%n",
                    rs.getString("edept"),
                    rs.getInt   ("total"),
                    "₹" + String.format("%,d", rs.getInt("min_sal")),
                    "₹" + String.format("%,d", rs.getInt("max_sal")),
                    "₹" + String.format("%,d", rs.getInt("avg_sal")),
                    "₹" + String.format("%,d", rs.getInt("payroll")));
            }
            System.out.println(line);
        } catch (SQLException e) {
            error(e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 12 : EXPORT TO CSV
    // =========================================================

    /**
     * Exports all employee records to a timestamped CSV file.
     * Demonstrates: File I/O, ResultSet iteration, data formatting.
     */
    private void exportToCSV() {
        header("💾  EXPORT TO CSV");

        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "employees_export_" + timestamp + ".csv";

        Connection con = connect();
        if (con == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(
                "SELECT * FROM employee ORDER BY edept, efname");

            // Write CSV header
            pw.println("Employee_ID,First_Name,Last_Name,Salary,Address,Department,Email");

            int count = 0;
            while (rs.next()) {
                pw.printf("%s,%s,%s,%d,%s,%s,%s%n",
                    rs.getString("eid"),
                    rs.getString("efname"),
                    rs.getString("elname"),
                    rs.getInt   ("esal"),
                    rs.getString("eaddr").replace(",", ";"),  // escape commas in address
                    rs.getString("edept"),
                    rs.getString("eemail"));
                count++;
            }

            success("Exported " + count + " records to: " + BOLD + filename);
            auditLog("EXPORT_CSV", "ALL", count + " records → " + filename);

        } catch (IOException e) {
            error("Could not write file: " + e.getMessage());
        } catch (SQLException e) {
            error("Database error: " + e.getMessage());
        } finally { close(con); }
    }

    // =========================================================
    //   SECTION 13 : AUDIT LOG
    // =========================================================

    /**
     * Appends a timestamped entry to audit_log.txt.
     * Every write, update, delete, and login is recorded here.
     * Demonstrates: File append mode, LocalDateTime formatting.
     */
    private void auditLog(String action, String empId, String details) {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s]  %-14s  EMP=%-10s  %s",
            timestamp, action, empId, details);

        try (FileWriter fw = new FileWriter("audit_log.txt", true)) {
            fw.write(entry + System.lineSeparator());
        } catch (IOException ignored) { }
    }

    private void viewAuditLog() {
        header("📜  AUDIT LOG");
        File logFile = new File("audit_log.txt");

        if (!logFile.exists()) {
            info("No audit log found yet. Actions will be recorded here once you start."); return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            // Read all lines, print last 50 (most recent)
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);

            int start = Math.max(0, lines.size() - 50);
            if (start > 0) info("Showing last 50 of " + lines.size() + " entries:");

            for (int i = start; i < lines.size(); i++) {
                System.out.println("  " + DIM + lines.get(i) + RESET);
            }

            System.out.println();
            info("Log file: audit_log.txt  (" + lines.size() + " total entries)");

        } catch (IOException e) {
            error("Could not read audit log: " + e.getMessage());
        }
    }

    // =========================================================
    //   MAIN  —  Entry Point
    // =========================================================

    public static void main(String[] args) {
        EmployeeManagementSystem ems = new EmployeeManagementSystem();
        if (ems.login()) {
            ems.showMenu();
        }
    }
}
