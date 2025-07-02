import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class Main extends Application {

    // ✅ DB Credentials
    private final String URL = "jdbc:mysql://localhost:3306/studentdb";
    private final String USER = "root";         // Change if needed
    private final String PASSWORD = "password"; // Change if needed

    // ✅ UI Components
    private TableView<Student> tableView = new TableView<>();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private TextField idField = new TextField();
    private TextField nameField = new TextField();
    private TextField emailField = new TextField();

    @Override
    public void start(Stage stage) {
        // 🧱 TableView Columns
        TableColumn<Student, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.getColumns().addAll(idCol, nameCol, emailCol);

        // 🧾 Input Fields
        idField.setPromptText("ID");
        nameField.setPromptText("Name");
        emailField.setPromptText("Email");

        HBox inputFields = new HBox(10, idField, nameField, emailField);

        // 🖱️ Buttons
        Button viewBtn = new Button("View Data");
        Button insertBtn = new Button("Insert");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        viewBtn.setOnAction(e -> loadData());
        insertBtn.setOnAction(e -> insertStudent());
        updateBtn.setOnAction(e -> updateStudent());
        deleteBtn.setOnAction(e -> deleteStudent());

        HBox buttonRow = new HBox(10, viewBtn, insertBtn, updateBtn, deleteBtn);

        // 📝 Footer
        Label footer = new Label("Name: Nimesh Neupane | ID: 23090879 | Date: 02-July-2025");

        VBox layout = new VBox(15, tableView, inputFields, buttonRow, footer);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 700, 400);
        stage.setTitle("JavaFX CRUD - Nimesh");
        stage.setScene(scene);
        stage.show();
    }

    // 📡 Connect to DB
    private Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Connection Failed", e.getMessage());
            return null;
        }
    }

    // ⚠️ Show Alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 🔁 Load Data
    private void loadData() {
        studentList.clear();
        Connection conn = connect();
        if (conn == null) return;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }

            tableView.setItems(studentList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Load Error", "Unable to load data.");
        }
    }

    // ➕ Insert Student
    private void insertStudent() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert("Input Error", "Please enter name and email.");
            return;
        }

        String sql = "INSERT INTO students (name, email) VALUES (?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();

            showAlert("Success", "Student inserted!");
            loadData();

            nameField.clear();
            emailField.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Insert Error", "Could not insert.");
        }
    }

    // ✏️ Update Student
    private void updateStudent() {
        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (idText.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert("Input Error", "Please enter ID, Name, and Email.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            String sql = "UPDATE students SET name = ?, email = ? WHERE id = ?";

            try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setInt(3, id);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    showAlert("Success", "Student updated!");
                    loadData();
                    idField.clear();
                    nameField.clear();
                    emailField.clear();
                } else {
                    showAlert("Not Found", "No student found with ID " + id);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid ID", "ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Update Error", "Could not update student.");
        }
    }

    // ❌ Delete Student
    private void deleteStudent() {
        String idText = idField.getText().trim();

        if (idText.isEmpty()) {
            showAlert("Input Error", "Please enter the ID to delete.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            String sql = "DELETE FROM students WHERE id = ?";

            try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);

                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    showAlert("Success", "Student deleted!");
                    loadData();
                    idField.clear();
                    nameField.clear();
                    emailField.clear();
                } else {
                    showAlert("Not Found", "No student found with ID " + id);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid ID", "ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Delete Error", "Could not delete student.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
