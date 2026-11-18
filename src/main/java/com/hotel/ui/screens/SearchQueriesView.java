package com.hotel.ui.screens;

import com.hotel.model.tables.Reservation;
import com.hotel.model.tables.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.hibernate.SessionFactory;
import java.time.LocalDate;

public class SearchQueriesView {

    private final SessionFactory sessionFactory;
    private final TableView<String[]> resultsTable;
    private final ObservableList<String[]> tableRows;
    private Label summaryLabel;

    public SearchQueriesView(SessionFactory sessionFactory) {
        this.resultsTable = new TableView<>();
        this.sessionFactory = sessionFactory;
        this.tableRows = FXCollections.observableArrayList();
    }

    public ScrollPane getView(){
        VBox mainLayout = new VBox(16);// κύριο κάθετο container που μέσα σε αυτό θα βρίσκονται όλα
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");

        Label title = new Label("Αναζήτηση και Στατιστικά Ξενοδοχείου");
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox queriesBox = new VBox(12);//κάρτα που θα βρίσκονται τα 5 δυναμικά queries
        queriesBox.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");



        String runBtnStyle = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;";

        //Query 1 όπου που περιλαμβάνει εύρος ημερομηνιών & κατάσταση
        DatePicker fromPicker = new DatePicker(LocalDate.now().minusMonths(1));
        fromPicker.setPrefWidth(125);
        DatePicker toPicker = new DatePicker(LocalDate.now().plusMonths(3));
        toPicker.setPrefWidth(125);
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList());
        statusBox.setValue("Όλες");
        Button q1btn = new Button("Εκτέλεση");
        q1btn.setStyle(runBtnStyle);

        HBox row1 = new HBox(10,
                createQuery("Q1"),
                new Label("Κρατήσεις από:"), fromPicker,
                new Label("εώςL"),toPicker,
                statusBox,q1btn);
        row1.setAlignment(Pos.CENTER_LEFT);

        //Query 2 όπου περιλαμβάνει έσοδα και πλήθος ανα τύπο δωματίου
        ComboBox<String> roomTypeBox = new ComboBox<>(FXCollections.observableArrayList("Όλοι οι τύποι",
                "Single",
                "Double",
                "Double Deluxe",
                "Family",
                "Suite",
                "Presidential"));
        roomTypeBox.setValue("Όλοι οι τύποι");
        Button q2btn = new Button("Εκτέλεση ");
        q2btn.setStyle(runBtnStyle);
        HBox row2 = new HBox(10,
                createQuery("Q1"),
                new Label("Στατιστικά & Έσοδα ανά Τύπο Δωματίου"),
                roomTypeBox,q2btn);
        row2.setAlignment(Pos.CENTER_LEFT);

        //Query 3 κρατήσεις άνω ενός ποσού
        TextField minAmountField = new TextField("250");
        minAmountField.setPrefWidth(90);
        Button q3btn = new Button("Εκτέλεση");
        q3btn.setStyle(runBtnStyle);
        HBox row3 = new HBox(10,
                createQuery("Q3"),
                new Label("Κρατήσεις με συνολικό κόστος μεγαλύτερο ή ίσο από (€):"),
                minAmountField,q3btn);
        row3.setAlignment(Pos.CENTER_LEFT);

        //Query 4 δημοτικότητα και έσοδα έξτρα υπηρεσιών
        TextField serviceField = new TextField();
        serviceField.setPromptText("Όλες ή π.χ. Spa, Breakfast...");
        serviceField.setPrefWidth(200);
        Button  q4btn = new Button("Εκτέλεση");
        q4btn.setStyle(runBtnStyle);
        HBox row4 = new HBox(10,
                createQuery("Q4"),
                new Label("Ανάλυση Έξτρα Υπηρεσιών (φίλτρο ονόματος):"),
                serviceField,q4btn);
        row4.setAlignment(Pos.CENTER_LEFT);

        //Query 5 συνολική δαπάνη και ιστορικό ανά πελάτη
        TextField customerField = new TextField();
        customerField.setPrefWidth(210);
        customerField.setPromptText("Όλοι ή Επώνυμο / Email...");
        Button  q5btn = new Button("Εκτέλεση");
        q5btn.setStyle(runBtnStyle);
        HBox row5 = new HBox(10,
                createQuery("Q5"),
                new Label("Συνολική Δαπάνη & Κρατήσεις Πελατών:"),
                customerField,q5btn);
        row5.setAlignment(Pos.CENTER_LEFT);

        queriesBox.getChildren().addAll(row1,new Separator(),row2,new Separator(),row3,new Separator(),row4,new Separator(),row5);

        summaryLabel = new Label("Επιλέξτε παραμέτρους και πατήστε «Εκτέλεση» σε οποιοδήποτε από τα 5 δυναμικά queries.");
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        resultsTable.setItems(tableRows);
        resultsTable.setPrefHeight(310);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        mainLayout.getChildren().addAll(title,queriesBox);
        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        return scroll;
    }

    public Label createQuery(String query){
        Label text = new Label(query);
        text.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 4 9; -fx-background-radius: 5;");
        return text;
    }

    public void setupColumns(String... columns){//μέθοδος που χτίζει τις στήλες του πίνακα
        resultsTable.getColumns().clear();//διαγραφή των προηγούμενων στηλών του πίνακα
        for(int i = 0; i < columns.length; i++){
            final int colIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(columns[colIndex]);
            column.setCellValueFactory(cell->{
                String[] row = cell.getValue();
                return new SimpleStringProperty(colIndex < row.length ? row[colIndex] : "");
            });
            resultsTable.getColumns().add(column);
        }
    }
}