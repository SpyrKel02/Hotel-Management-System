package com.hotel.ui.screens;

import com.hotel.controller.ReservationController;
import com.hotel.model.dao.ReservationDao;
import com.hotel.model.tables.Reservation;
import com.hotel.model.tables.Service;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookingsListView {

    private final ReservationDao reservationDao;
    private final ReservationController resController;
    private final TableView<Reservation> tableView;//ο πίνακας που θα εμφανίζει τις κρατήσεις
    private final ObservableList<Reservation> bookingsList;//λίστα των κρατήσεων που θα εμφανίζεται και θα τροφοδοτεί το table view

    public BookingsListView(ReservationDao reservationDao, ReservationController resController) {
        this.reservationDao = reservationDao;
        this.resController = resController;
        this.tableView = new TableView<>();
        this.bookingsList = FXCollections.observableArrayList();
    }

    public VBox getView(){
        VBox layout = new VBox(18);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label title = new Label("Διαχείριση & Ιστορικό Κρατήσεων");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        //Αναζήτηση κρατήσεων ενός πελάτη με βάση το email του
        TextField emailSearchField = new TextField();
        emailSearchField.setPromptText("Αναζήτησημ Email πελάτη");
        emailSearchField.setPrefWidth(280);

        ComboBox<String> statusComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Όλες","Confirmed","Cancelled")
        );
        statusComboBox.setValue("Όλες");
        statusComboBox.setStyle(SearchQueriesView.modernComboBoxStyle());

        Button searchBtn = new Button("Αναζήτηση");
        searchBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");

        Button resetBtn = new Button("Επαναφορά (προβολή όλων)");
        resetBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");

        HBox filterBar = new HBox(10,emailSearchField, new Label("Κατάσταση"),statusComboBox,searchBtn,resetBtn);

        //πίνακας κρατήσεων
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");//στήλη με το ID της κράτησης
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        idCol.setPrefWidth(55);

        TableColumn<Reservation, String> customerCol = new TableColumn<>("Ονοματεπώνυμο");//στήλη με το ονοματεπώνυμο του πελάτη
        customerCol.setCellValueFactory(cell ->{
            if(cell.getValue().getCustomerId()!=null){
                return new SimpleStringProperty(
                        cell.getValue().getCustomerId().getFirstName()+" "+
                           cell.getValue().getCustomerId().getLastName()
                );
            }
            return new SimpleStringProperty("-");
        });
        customerCol.setPrefWidth(150);

        TableColumn<Reservation, String> emailCol = new TableColumn<>("Email");//στήλη με το email του πελάτη
        emailCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCustomerId().getEmail()));
        emailCol.setPrefWidth(150);

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Δωμάτιο");//στήλη με τα στοιχεία του δωματίου
        roomCol.setCellValueFactory(cell ->{
            if(cell.getValue().getRoomId()!=null){
                return new SimpleStringProperty(
                        cell.getValue().getRoomId().getRoomNumber()+" ("+
                           cell.getValue().getRoomId().getRoomType()+")"
                );
            }
            return new SimpleStringProperty("-");
        });
        roomCol.setPrefWidth(150);

        TableColumn<Reservation, LocalDate> checkInCol = new TableColumn<>("Check-In");//στήλη με την ημέρα προσέλευσης
        checkInCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCheckInDate()));
        checkInCol.setPrefWidth(100);

        TableColumn<Reservation, LocalDate> checkOutCol = new TableColumn<>("Check-Out");//στήλη με την ημέρα αποχώρησης
        checkOutCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCheckOutDate()));
        checkOutCol.setPrefWidth(100);

        TableColumn<Reservation, String> servicesCol = new TableColumn<>("Εξτρά Υπηρεσίες");//στήλη με τις εξτρά υπηρεσίες
        servicesCol.setCellValueFactory(cell ->{
            if(cell.getValue().getServices() == null || cell.getValue().getServices().isEmpty()){
                return new SimpleStringProperty("Καμία");
            }
            String extraServices = cell.getValue().getServices().stream()
                    .map(Service::getServiceName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(extraServices);
        });
        servicesCol.setPrefWidth(200);

        TableColumn<Reservation, BigDecimal> priceCol = new TableColumn<>("Σύνολο (€)");//στήλη με το συνολικό κόστος της κράτησης
        priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTotalPrice()));
        priceCol.setPrefWidth(95);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Κατάσταση");
        statusCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStatus()));
        statusCol.setPrefWidth(105);

        tableView.getColumns().addAll(idCol, customerCol, emailCol,roomCol, checkInCol, checkOutCol, servicesCol, priceCol, statusCol);
        tableView.setItems(bookingsList);
        tableView.setPrefHeight(390);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);//οι στήλες προσαρμόζονται ομοιόμοργα στο πλάτος του παραθύρου

       Button cancelButton = new Button("Ακύρωση επιλεγμένης κράτησης");//κουμπί για ακύρωση κράτησης
       cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 15; -fx-cursor: hand;");

       Label statusLabel = new Label();
       statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

       //αναζήτηση κρατήσεων με βάση email και κατάσταση
       searchBtn.setOnAction(event -> {
           String emailQuery = emailSearchField.getText().trim().toLowerCase();
           String selectedStatus = statusComboBox.getValue();

           List<Reservation> filtered = reservationDao.findAllReservationsWithServices().stream()
                   .filter(r -> emailQuery.isEmpty() || (r.getCustomerId() != null && r.getCustomerId().getEmail().toLowerCase().contains(emailQuery)))
                   .filter(r -> "Όλες".equals(selectedStatus) || selectedStatus.equalsIgnoreCase(r.getStatus()))
                   .collect(Collectors.toList());
           bookingsList.setAll(filtered);
           statusLabel.setText("Βρέθηκαν "+filtered.size()+" κρατήσεις");
           statusLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
       });

       //επαναφορά όλων των κρατήσεων
        resetBtn.setOnAction(event -> {
            emailSearchField.clear();
            statusComboBox.setValue("Όλες");
            refreshTable();
            statusLabel.setText("Εμφάνιση όλων των κρατήσεων ("+bookingsList.size()+")");
            statusLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        });

        cancelButton.setOnAction(event -> {
            Reservation selected = tableView.getSelectionModel().getSelectedItem();
            if(selected == null){
                statusLabel.setText("Επίλεξε πρώτα μια κράτηση από τον πίνακα");
                statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                return;
            }
            if("Cancelled".equalsIgnoreCase(selected.getStatus())){
                statusLabel.setText("Η κράτηση είναι ήδη ακυρωμένη");
                statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Επιβεβαίωση Ακύρωσης");
            alert.setHeaderText("Ακύρωση κράτησης #"+selected.getReservationId());
            alert.setContentText("Είστε σίγουροι ότι θέλετε να ακυρώσετε την κράτηση για το δωμάτιο "
            +selected.getRoomId().getRoomNumber()+" ;Το δωμάτιο θα γίνει ξανά διαθέσιμο");

            alert.showAndWait().ifPresent(response -> {
                if(response == ButtonType.OK){
                    boolean canceled = resController.cancelReservation(selected.getReservationId());
                    if(canceled){
                        statusLabel.setText("Η κράτηση #"+selected.getReservationId()+" ακυρώθηκε");
                        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        searchBtn.fire();
                    }else{
                        statusLabel.setText("Αποτυχία ακύρωσης κράτησης.");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            });

        });

        refreshTable();
        layout.getChildren().addAll(title,filterBar,tableView, new HBox(15, cancelButton, statusLabel));
        return layout;

    }

    public void refreshTable(){
        bookingsList.setAll(reservationDao.findAllReservationsWithServices());
    }

}