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
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        q1btn.setOnAction(e -> {
            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();
            String status = statusBox.getValue();

            try(Session session = sessionFactory.openSession()) {
                StringBuilder sql = new StringBuilder("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.services WHERE 1=1 ");//query όπου φέρνει κρατήσεις μαζί με τις υπηρεσίες τους
                if(from !=null){
                    sql.append(" AND r.checkInDate >= :fromDate ");
                }
                if(to !=null){
                    sql.append(" AND r.checkInDate <= :toDate ");
                }
                if(!"Όλες".equals(status)){
                    sql.append(" AND r.status = :status ");
                }
                else{
                    sql.append(" ORDER BY r.checkInDate ASC");
                }

                var query = session.createQuery(sql.toString(), Reservation.class);
                if(from!=null){
                    query.setParameter("fromDate",from);
                }
                if(to!=null){
                    query.setParameter("toDate",to);
                }
                if(!"Όλες".equals(status)){
                    query.setParameter("status",status);
                }
                List<Reservation> results = query.list();//εκτέλεση query και παίρνει σαν αποτέλεσμα την λίστα κρατήσεων
                setupColumns("ID", "Πελάτης", "Δωμάτιο", "Check-in", "Check-out", "Υπηρεσίες", "Σύνολο (€)", "Κατάσταση"); // Ορίζει τις 8 στήλες στον πίνακα
                List<String[]> rows = new ArrayList<>(); // Προσωρινή λίστα για τις νέες γραμμές του πίνακα
                BigDecimal sum = BigDecimal.ZERO; // Μετρητής συνολικών εσόδων περιόδου
                for (Reservation r : results) { // Διατρέχει κάθε κράτηση που βρέθηκε
                    String srv = (r.getServices() == null || r.getServices().isEmpty()) ? "Καμία"
                            : r.getServices().stream().map(Service::getServiceName).collect(Collectors.joining(", "));
                    rows.add(new String[]{
                            String.valueOf(r.getReservationId()), // στήλη 0 με το ID κράτησης
                            r.getCustomerId().getFirstName() + " " + r.getCustomerId().getLastName(), // στήλη 1 με το ονοματεπώνυμο πελάτη
                            r.getRoomId().getRoomNumber() + " (" + r.getRoomId().getRoomType() + ")", // στήλη 2 αριθμός & τύπος δωματίου
                            String.valueOf(r.getCheckInDate()), // στήλη 3 με την ημερομηνία άφιξης
                            String.valueOf(r.getCheckOutDate()), // στήλη 4 με την ημερομηνία αναχώρησης
                            srv, //στήλη 5 με τις έξτρα υπηρεσίες
                            r.getTotalPrice() + " €", // στήλη 6 με την συνολική τιμή
                            r.getStatus() // στήλη 7 κατάσταση κράτησης
                    });
                    if ("Confirmed".equalsIgnoreCase(r.getStatus())) {
                        sum = sum.add(r.getTotalPrice()); //προσθέτει το ποσό της στα συνολικά έσοδα
                    }
                }
                tableRows.setAll(rows);
                summaryLabel.setText("Q1 Αποτελέσματα: " + rows.size() + " κρατήσεις | Ενεργά Έσοδα περιόδου: " + sum + " €"); // Εμφανίζει σύνοψη
            }
        });

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