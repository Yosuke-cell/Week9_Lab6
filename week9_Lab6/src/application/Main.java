package application;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	// ----- database helper -----
	private final PatientCRUD crud = new PatientCRUD();

	// ----- form controls -----
	private final TextField idField = new TextField();
	private final TextField nameField = new TextField();
	private final TextField ageField = new TextField();
	private final ComboBox<String> genderBox = new ComboBox<>();
	private final TextField phoneField = new TextField();
	private final TextField diagnosisField = new TextField();
	private final DatePicker admissionPicker = new DatePicker();

	// ----- records area -----
	private final TextField searchField = new TextField();
	private final TableView<Patient> table = new TableView<>();
	private final ObservableList<Patient> patients = FXCollections.observableArrayList();

	private final Label statusLabel = new Label("Enter patient details.");

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(20));

		// ===== TOP: title + subtitle =====
		Label heading = new Label("Hospital Management System");
		heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
		Label subtitle = new Label("Patient Registration • JavaFX • JDBC • MySQL");
		VBox header = new VBox(5, heading, subtitle);
		header.setPadding(new Insets(0, 0, 15, 0));
		root.setTop(header);

		// ===== CENTER: form (left) + records (right) =====
		HBox center = new HBox(20, buildFormPane(), buildRecordsPane());
		HBox.setHgrow(center.getChildren().get(1), Priority.ALWAYS);
		root.setCenter(center);

		// ===== BOTTOM: status =====
		statusLabel.setPadding(new Insets(10, 0, 0, 0));
		root.setBottom(statusLabel);

		primaryStage.setTitle("Hospital Management System");
		primaryStage.setScene(new Scene(root, 1100, 560));
		primaryStage.show();

		loadAllPatients(); 
	}

	// ---------------------------------------------------------------
	// UI building
	// ---------------------------------------------------------------

	private VBox buildFormPane() {
		GridPane form = new GridPane();
		form.setHgap(12);
		form.setVgap(12);
		form.setPadding(new Insets(15));
		form.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6;");

		idField.setPromptText("Auto-generated");
		idField.setEditable(false);
		idField.setStyle("-fx-background-color: #eeeeee;");
		nameField.setPromptText("Full name");
		ageField.setPromptText("0 - 125");
		genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
		genderBox.setPromptText("Select gender");
		phoneField.setPromptText("e.g. 416-555-0123");
		diagnosisField.setPromptText("Reason for visit (fictional)");
		admissionPicker.setPromptText("Admission date");

		addRow(form, 0, "Patient ID:", idField);
		addRow(form, 1, "Full name:", nameField);
		addRow(form, 2, "Age:", ageField);
		addRow(form, 3, "Gender:", genderBox);
		addRow(form, 4, "Phone:", phoneField);
		addRow(form, 5, "Diagnosis:", diagnosisField);
		addRow(form, 6, "Admission date:", admissionPicker);

		ColumnConstraints labels = new ColumnConstraints(110);
		ColumnConstraints fields = new ColumnConstraints();
		fields.setHgrow(Priority.ALWAYS);
		form.getColumnConstraints().addAll(labels, fields);

		Button addButton = new Button("Add Patient");
		addButton.setDefaultButton(true);
		addButton.setOnAction(e -> addPatient());

		Button updateButton = new Button("Update");
		updateButton.setOnAction(e -> updatePatient());

		Button deleteButton = new Button("Delete");
		deleteButton.setOnAction(e -> deletePatient());

		Button clearButton = new Button("Clear");
		clearButton.setOnAction(e -> clearForm());

		HBox row1 = new HBox(10, addButton, updateButton);
		HBox row2 = new HBox(10, deleteButton, clearButton);
		row1.setAlignment(Pos.CENTER_RIGHT);
		row2.setAlignment(Pos.CENTER_RIGHT);

		Label formTitle = new Label("Patient Details");
		formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		VBox pane = new VBox(10, formTitle, form, row1, row2);
		pane.setPrefWidth(400);
		return pane;
	}

	private VBox buildRecordsPane() {
		Label title = new Label("Patient Records");
		title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		searchField.setPromptText("Search by name or phone");
		HBox.setHgrow(searchField, Priority.ALWAYS);
		Button searchButton = new Button("Search");
		searchButton.setOnAction(e -> searchPatients());
		Button showAllButton = new Button("Show All");
		showAllButton.setOnAction(e -> loadAllPatients());
		HBox searchRow = new HBox(10, searchField, searchButton, showAllButton);

		// ---- TableView columns (names must match Patient getters) ----
		TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
		idCol.setPrefWidth(50);

		TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setPrefWidth(150);

		TableColumn<Patient, Integer> ageCol = new TableColumn<>("Age");
		ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
		ageCol.setPrefWidth(50);

		TableColumn<Patient, String> genderCol = new TableColumn<>("Gender");
		genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
		genderCol.setPrefWidth(110);

		TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		phoneCol.setPrefWidth(120);

		TableColumn<Patient, String> diagCol = new TableColumn<>("Diagnosis");
		diagCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
		diagCol.setPrefWidth(160);

		TableColumn<Patient, LocalDate> dateCol = new TableColumn<>("Admitted");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
		dateCol.setPrefWidth(100);

		table.getColumns().add(idCol);
		table.getColumns().add(nameCol);
		table.getColumns().add(ageCol);
		table.getColumns().add(genderCol);
		table.getColumns().add(phoneCol);
		table.getColumns().add(diagCol);
		table.getColumns().add(dateCol);
		table.setItems(patients);
		table.setPlaceholder(new Label("No patients to display."));
		VBox.setVgrow(table, Priority.ALWAYS);

		// When the user clicks a row, copy it into the form (needed for Update/Delete)
		table.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldSel, newSel) -> {
					if (newSel != null) {
						populateForm(newSel);
					}
				});

		return new VBox(10, title, searchRow, table);
	}

	private void addRow(GridPane grid, int row, String text, Control control) {
		Label label = new Label(text);
		label.setStyle("-fx-font-weight: bold;");
		control.setMaxWidth(Double.MAX_VALUE);
		grid.add(label, 0, row);
		grid.add(control, 1, row);
	}

	// ---------------------------------------------------------------
	// Validation  (returns a Patient if OK, otherwise null + status msg)
	// ---------------------------------------------------------------

	private Patient readAndValidateForm(int patientId) {
		String name = nameField.getText().trim();
		String ageValue = ageField.getText().trim();
		String gender = genderBox.getValue();
		String phone = phoneField.getText().trim();
		String diagnosis = diagnosisField.getText().trim();
		LocalDate admission = admissionPicker.getValue();

		if (name.isEmpty() || ageValue.isEmpty() || gender == null
				|| phone.isEmpty() || diagnosis.isEmpty() || admission == null) {
			setStatus("Please complete all fields.", true);
			return null;
		}

		int age;
		try {
			age = Integer.parseInt(ageValue);
		} catch (NumberFormatException ex) {
			setStatus("Age must be a whole number.", true);
			return null;
		}
		if (age < 0 || age > 125) {
			setStatus("Age must be between 0 and 125.", true);
			return null;
		}

		// digits, spaces, dashes, dots, parentheses, optional leading +; at least 7 digits
		if (!phone.matches("^\\+?[0-9 ().-]{7,20}$") || phone.replaceAll("\\D", "").length() < 7) {
			setStatus("Enter a valid phone number (at least 7 digits).", true);
			return null;
		}

		if (admission.isAfter(LocalDate.now())) {
			setStatus("Admission date cannot be in the future.", true);
			return null;
		}

		return new Patient(patientId, name, age, gender, phone, diagnosis, admission);
	}

	// ---------------------------------------------------------------
	// CRUD actions (UI side - each one calls the DAO)
	// ---------------------------------------------------------------

	/** CREATE */
	private void addPatient() {
		Patient p = readAndValidateForm(0); // id 0 = not yet assigned by DB
		if (p == null) {
			return;
		}
		try {
			int count = crud.insert(p);
			if (count == 1) {
				setStatus("Patient added successfully.", false);
				clearFields();
				loadAllPatients();
			} else {
				setStatus("No record was inserted.", true);
			}
		} catch (SQLException ex) {
			showDatabaseError(ex);
		}
	}

	/** READ */
	private void loadAllPatients() {
		try {
			List<Patient> list = crud.findAll();
			patients.setAll(list);
			setStatus("Showing " + list.size() + " patient(s).", false);
		} catch (SQLException ex) {
			showDatabaseError(ex);
		}
	}

	/** SEARCH */
	private void searchPatients() {
		String keyword = searchField.getText().trim();
		if (keyword.isEmpty()) {
			loadAllPatients();
			return;
		}
		try {
			List<Patient> list = crud.search(keyword);
			patients.setAll(list);
			setStatus(list.size() + " patient(s) match \"" + keyword + "\".", false);
		} catch (SQLException ex) {
			showDatabaseError(ex);
		}
	}

	/** UPDATE */
	private void updatePatient() {
		Patient selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			setStatus("Select a patient in the table first.", true);
			return;
		}
		Patient p = readAndValidateForm(selected.getPatientId());
		if (p == null) {
			return;
		}
		try {
			int count = crud.update(p);
			if (count == 1) {
				setStatus("Patient #" + p.getPatientId() + " updated.", false);
				clearFields();
				loadAllPatients();
			} else {
				setStatus("No record was updated.", true);
			}
		} catch (SQLException ex) {
			showDatabaseError(ex);
		}
	}

	/** DELETE */
	private void deletePatient() {
		Patient selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			setStatus("Select a patient in the table first.", true);
			return;
		}

		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("Confirm delete");
		confirm.setHeaderText("Delete patient #" + selected.getPatientId() + "?");
		confirm.setContentText(selected.getName() + " will be permanently removed.");
		Optional<ButtonType> result = confirm.showAndWait();
		if (result.isEmpty() || result.get() != ButtonType.OK) {
			setStatus("Delete cancelled.", false);
			return;
		}

		try {
			int count = crud.delete(selected.getPatientId());
			if (count == 1) {
				setStatus("Patient #" + selected.getPatientId() + " deleted.", false);
				clearFields();
				loadAllPatients();
			} else {
				setStatus("No record was deleted.", true);
			}
		} catch (SQLException ex) {
			showDatabaseError(ex);
		}
	}

	// ---------------------------------------------------------------
	// Form helpers
	// ---------------------------------------------------------------

	private void populateForm(Patient p) {
		idField.setText(String.valueOf(p.getPatientId()));
		nameField.setText(p.getName());
		ageField.setText(String.valueOf(p.getAge()));
		genderBox.setValue(p.getGender());
		phoneField.setText(p.getPhone());
		diagnosisField.setText(p.getDiagnosis());
		admissionPicker.setValue(p.getAdmissionDate());
		setStatus("Editing patient #" + p.getPatientId() + ". Change fields then click Update.", false);
	}

	private void clearForm() {
		clearFields();
		setStatus("Form cleared.", false);
	}

	private void clearFields() {
		idField.clear();
		nameField.clear();
		ageField.clear();
		genderBox.getSelectionModel().clearSelection();
		genderBox.setValue(null);
		phoneField.clear();
		diagnosisField.clear();
		admissionPicker.setValue(null);
		table.getSelectionModel().clearSelection();
	}

	private void setStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #167545;");
	}

	private void showDatabaseError(SQLException ex) {
		setStatus("Database error: " + ex.getMessage(), true);
		ex.printStackTrace();
	}

	public static void main(String[] args) {
		launch(args);
	}
}