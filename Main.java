import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try (EmployeeService employeeService = new EmployeeService()) {
            System.out.println("Connected to MySQL successfully.");
            showMenu(employeeService);
        } catch (SQLException exception) {
            System.err.println("Failed to connect to MySQL.");
            System.err.println("Please ensure MySQL is running and update your database credentials in DatabaseConnection.java or environment variables.");
            exception.printStackTrace();
        }
    }

    private static void showMenu(EmployeeService employeeService) {
        while (true) {
            System.out.println("\n==============================");
            System.out.println("EMPLOYEE MANAGEMENT SYSTEM");
            System.out.println("==============================");
            System.out.println("1. Add Employee");
            System.out.println("2. List Employees");
            System.out.println("3. Search Employees");
            System.out.println("4. Search Employee By ID");
            System.out.println("5. Update Employee");
            System.out.println("6. Delete Employee");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    addEmployee(employeeService);
                    break;
                case 2:
                    viewAllEmployees(employeeService);
                    break;
                case 3:
                    searchEmployees(employeeService);
                    break;
                case 4:
                    searchEmployeeById(employeeService);
                    break;
                case 5:
                    updateEmployee(employeeService);
                    break;
                case 6:
                    deleteEmployee(employeeService);
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addEmployee(EmployeeService employeeService) {
        try {
            System.out.print("Name: ");
            String name = readRequiredText();

            System.out.print("Email: ");
            String email = readRequiredText();
            if (!isValidEmail(email)) {
                System.out.println("Invalid email format. Please enter a valid email address.");
                return;
            }

            System.out.print("Department: ");
            String department = readRequiredText();

            System.out.print("Salary: ");
            double salary = readDouble();
            if (!Double.isFinite(salary) || salary <= 0) {
                System.out.println("Salary must be a finite number greater than zero.");
                return;
            }

            Employee employee = new Employee(name.trim(), email.trim(), department.trim(), salary);
            employeeService.addEmployee(employee);
            System.out.println("Employee added successfully.");
        } catch (IllegalArgumentException exception) {
            System.err.println("Validation error: " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println("Error adding employee: " + exception.getMessage());
        }
    }

    private static void viewAllEmployees(EmployeeService employeeService) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            System.out.println("\nEmployee Records");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-5s %-20s %-30s %-20s %-12s %-25s%n", "ID", "Name", "Email", "Department", "Salary", "Created At");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            for (Employee employee : employees) {
                System.out.printf("%-5d %-20s %-30s %-20s %-12.2f %-25s%n",
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        employee.getDepartment(),
                        employee.getSalary(),
                        employee.getCreatedAt());
            }
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException exception) {
            System.err.println("Error retrieving employees: " + exception.getMessage());
        }
    }

    private static void searchEmployees(EmployeeService employeeService) {
        System.out.print("Search by name, email, or department: ");
        String keyword = SCANNER.nextLine().trim();

        try {
            List<Employee> employees = employeeService.searchEmployees(keyword);
            if (employees.isEmpty()) {
                System.out.println("No matching employees found.");
                return;
            }

            System.out.println("\nSearch results:");
            System.out.println("ID | Name | Email | Department | Salary | Created At");
            System.out.println("---------------------------------------------------------------");
            for (Employee employee : employees) {
                System.out.printf("%d | %s | %s | %s | %.2f | %s%n",
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        employee.getDepartment(),
                        employee.getSalary(),
                        employee.getCreatedAt());
            }
        } catch (SQLException exception) {
            System.err.println("Error searching employees: " + exception.getMessage());
        }
    }

    private static void searchEmployeeById(EmployeeService employeeService) {
        System.out.print("Employee ID: ");
        int id = readPositiveInt();

        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);
            if (employee.isPresent()) {
                Employee result = employee.get();
                System.out.println("\nEmployee found:");
                System.out.printf("%-12s %s%n", "ID:", result.getId());
                System.out.printf("%-12s %s%n", "Name:", result.getName());
                System.out.printf("%-12s %s%n", "Email:", result.getEmail());
                System.out.printf("%-12s %s%n", "Department:", result.getDepartment());
                System.out.printf("%-12s %.2f%n", "Salary:", result.getSalary());
                System.out.printf("%-12s %s%n", "Created At:", result.getCreatedAt());
            } else {
                System.out.println("Employee not found.");
            }
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid employee ID: " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println("Error retrieving employee: " + exception.getMessage());
        }
    }

    private static void updateEmployee(EmployeeService employeeService) {
        System.out.print("Employee ID to update: ");
        int id = readPositiveInt();

        try {
            Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);
            if (employeeOpt.isEmpty()) {
                System.out.println("Employee not found.");
                return;
            }

            Employee employee = employeeOpt.get();
            System.out.println("Leave blank to keep the current value.");

            System.out.print("New name [" + employee.getName() + "]: ");
            String nameInput = SCANNER.nextLine();
            String name = nameInput.isBlank() ? employee.getName() : nameInput.trim();

            System.out.print("New email [" + employee.getEmail() + "]: ");
            String emailInput = SCANNER.nextLine();
            String email = emailInput.isBlank() ? employee.getEmail() : emailInput.trim();
            if (!isValidEmail(email)) {
                System.out.println("Invalid email format. Update cancelled.");
                return;
            }

            System.out.print("New department [" + employee.getDepartment() + "]: ");
            String departmentInput = SCANNER.nextLine();
            String department = departmentInput.isBlank() ? employee.getDepartment() : departmentInput.trim();

            System.out.print("New salary [" + employee.getSalary() + "]: ");
            String salaryInput = SCANNER.nextLine();
            double salary;
            if (salaryInput.isBlank()) {
                salary = employee.getSalary();
            } else {
                salary = Double.parseDouble(salaryInput.trim());
            }

            if (!Double.isFinite(salary) || salary <= 0) {
                System.out.println("Salary must be a finite number greater than zero. Update cancelled.");
                return;
            }

            employee.setName(name);
            employee.setEmail(email);
            employee.setDepartment(department);
            employee.setSalary(salary);

            boolean updated = employeeService.updateEmployee(employee);
            if (updated) {
                System.out.println("Employee updated successfully.");
            } else {
                System.out.println("Failed to update employee.");
            }
        } catch (NumberFormatException exception) {
            System.err.println("Invalid salary format. Please enter a numeric value.");
        } catch (IllegalArgumentException exception) {
            System.err.println("Validation error: " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println("Error updating employee: " + exception.getMessage());
        }
    }

    private static void deleteEmployee(EmployeeService employeeService) {
        System.out.print("Employee ID to delete: ");
        int id = readPositiveInt();

        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);
            if (employee.isEmpty()) {
                System.out.println("Employee not found.");
                return;
            }

            System.out.print("Are you sure you want to delete employee " + employee.get().getName() + "? (y/n): ");
            String confirmation = SCANNER.nextLine().trim().toLowerCase();

            if (confirmation.equals("y") || confirmation.equals("yes")) {
                boolean deleted = employeeService.deleteEmployee(id);
                if (deleted) {
                    System.out.println("Employee deleted successfully.");
                } else {
                    System.out.println("Failed to delete employee.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (IllegalArgumentException exception) {
            System.err.println("Validation error: " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println("Error deleting employee: " + exception.getMessage());
        }
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

    private static String readRequiredText() {
        String value = SCANNER.nextLine().trim();
        while (value.isEmpty()) {
            System.out.print("This field cannot be empty. Please enter a value: ");
            value = SCANNER.nextLine().trim();
        }
        return value;
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static int readPositiveInt() {
        while (true) {
            int value = readInt();
            if (value > 0) {
                return value;
            }
            System.out.print("Please enter an ID greater than zero: ");
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(SCANNER.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid salary: ");
            }
        }
    }
}
