package application;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientCRUD {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_db";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "class123";

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	}

	// ---------- CREATE ----------
	public int insert(Patient p) throws SQLException {
		String sql = "INSERT INTO patients (name, age, gender, phone, diagnosis, admission_date) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, p.getName());
			ps.setInt(2, p.getAge());
			ps.setString(3, p.getGender());
			ps.setString(4, p.getPhone());
			ps.setString(5, p.getDiagnosis());
			ps.setDate(6, Date.valueOf(p.getAdmissionDate()));
			return ps.executeUpdate(); // number of rows inserted (should be 1)
		}
	}

	// ---------- READ (all rows) ----------
	public List<Patient> findAll() throws SQLException {
		String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
				+ "FROM patients ORDER BY patient_id";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			return toList(rs);
		}
	}

	// ---------- SEARCH (by name OR phone, parameterised LIKE) ----------
	public List<Patient> search(String keyword) throws SQLException {
		String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
				+ "FROM patients WHERE name LIKE ? OR phone LIKE ? ORDER BY patient_id";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			String pattern = "%" + keyword + "%";
			ps.setString(1, pattern);
			ps.setString(2, pattern);
			try (ResultSet rs = ps.executeQuery()) {
				return toList(rs);
			}
		}
	}

	// ---------- UPDATE ----------
	public int update(Patient p) throws SQLException {
		String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, "
				+ "diagnosis = ?, admission_date = ? WHERE patient_id = ?";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, p.getName());
			ps.setInt(2, p.getAge());
			ps.setString(3, p.getGender());
			ps.setString(4, p.getPhone());
			ps.setString(5, p.getDiagnosis());
			ps.setDate(6, Date.valueOf(p.getAdmissionDate()));
			ps.setInt(7, p.getPatientId());
			return ps.executeUpdate();
		}
	}

	// ---------- DELETE ----------
	public int delete(int patientId) throws SQLException {
		String sql = "DELETE FROM patients WHERE patient_id = ?";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, patientId);
			return ps.executeUpdate();
		}
	}

	private List<Patient> toList(ResultSet rs) throws SQLException {
		List<Patient> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new Patient(
					rs.getInt("patient_id"),
					rs.getString("name"),
					rs.getInt("age"),
					rs.getString("gender"),
					rs.getString("phone"),
					rs.getString("diagnosis"),
					rs.getDate("admission_date").toLocalDate()));
		}
		return list;
	}
}