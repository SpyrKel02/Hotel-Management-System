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

        //εκτέλεση δυναμικού query 1
        q1btn.setOnAction(e -> {
            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();
            String status = statusBox.getValue();

            try (Session session = sessionFactory.openSession()) {
                StringBuilder sql = new StringBuilder(
                        "SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.services WHERE 1=1 "
                );
                if (from != null) sql.append(" AND r.checkInDate >= :fromDate ");
                if (to != null) sql.append(" AND r.checkOutDate <= :toDate ");
                if (!"Όλες".equals(status)) sql.append(" AND r.status = :status ");
                sql.append(" ORDER BY r.checkInDate ASC");

                System.out.println("DEBUG Q1 HQL: " + sql);
                System.out.println("DEBUG Q1 Params -> from: " + from + ", to: " + to + ", status: " + status);

                var query = session.createQuery(sql.toString(), Reservation.class);
                if (from != null) query.setParameter("fromDate", from);
                if (to != null) query.setParameter("toDate", to);
                if (!"Όλες".equals(status)) query.setParameter("status", status);

                List<Reservation> results = query.list();
                System.out.println("DEBUG Q1 Results count: " + results.size());

                setupColumns("ID", "Πελάτης", "Δωμάτιο", "Check-in", "Check-out", "Υπηρεσίες", "Σύνολο (€)", "Κατάσταση");

                List<String[]> rows = new ArrayList<>();
                BigDecimal sum = BigDecimal.ZERO;
                for (Reservation r : results) {
                    String srv = (r.getServices() == null || r.getServices().isEmpty()) ? "Καμία"
                            : r.getServices().stream().map(Service::getServiceName).collect(Collectors.joining(", "));
                    rows.add(new String[]{
                            String.valueOf(r.getReservationId()),
                            r.getCustomerId().getFirstName() + " " + r.getCustomerId().getLastName(),
                            r.getRoomId().getRoomNumber() + " (" + r.getRoomId().getRoomType() + ")",
                            String.valueOf(r.getCheckInDate()),
                            String.valueOf(r.getCheckOutDate()),
                            srv,
                            r.getTotalPrice() + " €",
                            r.getStatus()
                    });
                    if ("Confirmed".equalsIgnoreCase(r.getStatus())) {
                        sum = sum.add(r.getTotalPrice());
                    }
                }
                tableRows.setAll(rows);
                summaryLabel.setText("Αποτελέσματα: " + rows.size() + " κρατήσεις | Ενεργά Έσοδα περιόδου: " + sum + " €");
            } catch (Exception ex) {
                ex.printStackTrace();
                summaryLabel.setText("Σφάλμα: " + ex.getMessage());
            }
        });

        q2btn.setOnAction(event ->{
            String selectedType = roomTypeBox.getValue();
            try(Session session = sessionFactory.openSession()){
                String sql = "SELECT r.room.roomType, COUNT(r), SUM(r.totalPrice), AVG(r.totalPrice) "+
                        " FROM Reservation r WHERE r.status = 'Confirmed' "+
                        ("Όλοι οι τύποι".equals(selectedType) ? "" : "AND LOWER(r.room.roomType) LIKE LOWER(:rType) ") +
                        "GROUP BY r.room.roomType ORDER BY SUM(r.totalPrice) DESC";
                var query = session.createQuery(sql, Object[].class);
                if(!"Όλοι οι τύποι".equals(selectedType)){
                    query.setParameter("rType", "%" + selectedType + "%");
                }

                List<Object[]> results = query.list(); // εκτελεί το query και παίρνει τα ομαδοποιημένα αποτελέσματα
                setupColumns("Τύπος Δωματίου", "Πλήθος Ενεργών Κρατήσεων", "Συνολικά Έσοδα (€)", "Μέση Αξία Κράτησης (€)");

                List<String[]> rows = new ArrayList<>(); // λίστα για τις γραμμές αποτελεσμάτων
                for (Object[] row : results) {
                    double avg = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0; // μετατρέπει τον μέσο όρο (4η στήλη) σε double
                    rows.add(new String[]{ // Δημιουργεί τη γραμμή κειμένου για το TableView
                            String.valueOf(row[0]), // στήλη 0 τύπος δωματίου
                            String.valueOf(row[1]), // στήλη 1 πλήθος κρατήσεων (COUNT)
                            row[2] + " €", // στήλη 2 συνολικά έσοδα (SUM)
                            String.format("%.2f €", avg) // Στήλη 3: Μέση αξία με 2 δεκαδικά (AVG)
                    });
                }
                tableRows.setAll(rows); // Ενημερώνει τον πίνακα UI
                summaryLabel.setText("Στατιστικά ανά Τύπο Δωματίου (" + selectedType + "): " + rows.size() + " κατηγορίες"); // Ενημερώνει τη σύνοψη
            }catch(Exception ex){
                ex.printStackTrace();
                summaryLabel.setText("Σφάλμα: " + ex.getMessage());
            }
        });

        mainLayout.getChildren().addAll(title,queriesBox, summaryLabel, resultsTable);
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
        resultsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        resultsTable.getColumns().clear();

        double tableWidth = resultsTable.getWidth() > 0 ? resultsTable.getWidth() - 20 : 880;
        double equalWidth = Math.max(120, tableWidth / columns.length);
        for(int i = 0; i < columns.length; i++){
            final int colIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(columns[colIndex]);
            column.setCellValueFactory(cell->{
                String[] row = cell.getValue();
                return new SimpleStringProperty(colIndex < row.length ? row[colIndex] : "");
            });
            if ("ID".equalsIgnoreCase(columns[i])) {
                column.setPrefWidth(65);
                column.setMinWidth(55);
            } else {
                column.setPrefWidth(equalWidth);
                column.setMinWidth(115);
            }
            resultsTable.getColumns().add(column);
        }
    }
}