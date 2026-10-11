# Employee Management System

A simple Java CLI application for managing employees with MySQL and JDBC.

## Features
- Add an employee
- List all employees
- Search employees by name, email, or department
- View a single employee by ID
- Update employee details
- Delete an employee

## Project Structure
- `Employee.java` – employee model
- `EmployeeService.java` – business logic and database operations
- `DatabaseConnection.java` – JDBC connection setup
- `Main.java` – CLI menu and user interaction
- `sql/database.sql` – database schema and seed data

## Setup
1. Install MySQL and start the MySQL server.
2. Create the database schema:
   ```bash
   mysql -u root -p < sql/database.sql
   ```
3. Download the MySQL Connector/J JAR and place it in the project root or a `lib` folder.
4. Update the database credentials in `DatabaseConnection.java` or set these environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USERNAME="root"
   export DB_PASSWORD="root"
   ```

## Compile
```bash
javac -cp .:mysql-connector-j-*.jar *.java
```

## Run
```bash
java -cp .:mysql-connector-j-*.jar Main
```

## Windows commands
```powershell
javac -cp ".;mysql-connector-j-*.jar" *.java
java -cp ".;mysql-connector-j-*.jar" Main
```

## Notes
- On Windows, use `;` instead of `:` in the classpath.
- If your MySQL username/password differ, update the environment variables or `DatabaseConnection.java` before running.
- The app now supports searching employees by name, email, or department.
