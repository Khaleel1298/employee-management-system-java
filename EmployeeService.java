import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeService implements AutoCloseable {
    private final Connection connection;

    public EmployeeService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addEmployee(Employee employee) throws SQLException {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }

        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email is required.");
        }

        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee department is required.");
        }

        if (!Double.isFinite(employee.getSalary()) || employee.getSalary() <= 0) {
            throw new IllegalArgumentException("Salary must be a finite number greater than zero.");
        }

        if (employee.getId() > 0 && getEmployeeById(employee.getId()).isPresent()) {
            throw new IllegalArgumentException("Employee ID already exists.");
        }

        if (emailExists(employee.getEmail().trim())) {
            throw new IllegalArgumentException("Duplicate email address: " + employee.getEmail().trim());
        }

        String sql = "INSERT INTO employees (name, email, department, salary) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getName().trim());
            statement.setString(2, employee.getEmail().trim());
            statement.setString(3, employee.getDepartment().trim());
            statement.setDouble(4, employee.getSalary());
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (isDuplicateKeyError(exception)) {
                throw new SQLException("Duplicate employee record detected (email or ID already exists).", exception);
            }
            throw exception;
        }
    }

    public List<Employee> getAllEmployees() throws SQLException {
        String sql = "SELECT * FROM employees ORDER BY id";
        List<Employee> employees = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                employees.add(mapEmployee(resultSet));
            }
        }

        return employees;
    }

    public Optional<Employee> getEmployeeById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be greater than zero.");
        }

        String sql = "SELECT * FROM employees WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapEmployee(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<Employee> searchEmployees(String keyword) throws SQLException {
        String sql = "SELECT * FROM employees WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(department) LIKE ? ORDER BY id";
        List<Employee> employees = new ArrayList<>();
        String searchTerm = (keyword == null) ? "" : keyword.trim();
        String pattern = "%" + searchTerm.toLowerCase() + "%";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    employees.add(mapEmployee(resultSet));
                }
            }
        }

        return employees;
    }

    public boolean updateEmployee(Employee employee) throws SQLException {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        if (employee.getId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be greater than zero.");
        }

        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }

        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email is required.");
        }

        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee department is required.");
        }

        if (!Double.isFinite(employee.getSalary()) || employee.getSalary() <= 0) {
            throw new IllegalArgumentException("Salary must be a finite number greater than zero.");
        }

        String sql = "UPDATE employees SET name = ?, email = ?, department = ?, salary = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getName().trim());
            statement.setString(2, employee.getEmail().trim());
            statement.setString(3, employee.getDepartment().trim());
            statement.setDouble(4, employee.getSalary());
            statement.setInt(5, employee.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                return false;
            }
            return true;
        } catch (SQLException exception) {
            if (isDuplicateKeyError(exception)) {
                throw new SQLException("Duplicate employee record detected (email already exists).", exception);
            }
            throw exception;
        }
    }

    public boolean deleteEmployee(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be greater than zero.");
        }

        String sql = "DELETE FROM employees WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM employees WHERE email = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean isDuplicateKeyError(SQLException exception) {
        String sqlState = exception.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
    }

    private Employee mapEmployee(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        employee.setId(resultSet.getInt("id"));
        employee.setName(resultSet.getString("name"));
        employee.setEmail(resultSet.getString("email"));
        employee.setDepartment(resultSet.getString("department"));
        employee.setSalary(resultSet.getDouble("salary"));

        java.sql.Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            employee.setCreatedAt(timestamp.toLocalDateTime());
        } else {
            employee.setCreatedAt(LocalDateTime.now());
        }

        return employee;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
