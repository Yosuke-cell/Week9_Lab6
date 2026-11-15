module week9_Lab {
	requires javafx.controls;
	requires java.sql;
	
	opens application to javafx.graphics, javafx.base;
	exports application;
}
