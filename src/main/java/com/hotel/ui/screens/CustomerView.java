package com.hotel.ui.screens;

import com.hotel.model.dao.CustomerDao;
import com.hotel.model.dao.ReservationDao;
import com.hotel.model.tables.Customer;
import com.hotel.model.tables.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class CustomerView {

    private final CustomerDao customerDao;//επικοινωνία με την βάση στην PostgreSQL μέσω της κλάσης CustomerDao
    private final ReservationDao reservationDao;
    private final TableView<Customer> table;//πίνακας που θα εμφανίζει όλους τους εγγεγραμμένους πελάτες
    private final ObservableList<Customer> customerList;//λίστα που ουσιαστικά τροφοδοτεί τον πίνακα table με τους πελάτες

    public CustomerView(CustomerDao customerDao, ReservationDao reservationDao) {
        this.customerDao = customerDao;
        this.reservationDao = reservationDao;
        this.table = new TableView<>();
        this.customerList = FXCollections.observableArrayList();
    }

    public VBox getView(){
        VBox layout = new VBox(18);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label title = new Label("Διαχείριση Πελατών");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        //πίνακας πελατών
        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");//στήλη 1 με το ID του πελάτη
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        idCol.setPrefWidth(60);

        TableColumn<Customer, String> fNameCol = new TableColumn<>("Όνομα");//στήλη 2 με το όνομα του πελάτη
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        fNameCol.setPrefWidth(160);

        TableColumn<Customer, String> lNameCol = new TableColumn<>("Επώνυμο");//στήλη 3 με το επώνυμο του πελάτη
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lNameCol.setPrefWidth(180);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Τηλέφωνο");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, fNameCol, lNameCol, emailCol, phoneCol);//προσθήκη όλων των στηλών στον πίνακα
        table.setItems(customerList);//σύνδεση του πίνακα με την λίστα πελατών
        table.setPrefHeight(360);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        refreshTable();//φόρτωση πελατών από το database μόλις ανοίξει η οθόνη

        VBox formCard = new VBox(12);
        formCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");

        Label formTitle = new Label("Προσθήκη Νέου Πελάτη");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        //πεδία εισαγωγής στοιχείων
        TextField firstNameInput = new TextField();
        firstNameInput.setPromptText("Όνομα");

        TextField lastNameInput = new TextField();
        lastNameInput.setPromptText("Επώνυμο");

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email (π.χ. george@email.gr)");

        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Τηλέφωνο");

        Button addBtn = new Button("Προσθήκη Νέου Πελάτη");
        addBtn.setMinWidth(Button.USE_PREF_SIZE);
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");

        Button deleteBtn = new Button("Διαγραφή Επιλεγμένου Πελάτη");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        addBtn.setOnAction(event ->{
            if(firstNameInput.getText().trim().isEmpty() || lastNameInput.getText().trim().isEmpty() || emailInput.getText().trim().isEmpty() || phoneInput.getText().trim().isEmpty()) {
                messageLabel.setText("Συμπλήρωσε Όνομα, Επώνυμο, Εmail & Τηλέφωνο");
                messageLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                return;
            }
            try{
                if (customerDao.findByEmail(emailInput.getText().trim()) != null) {
                    messageLabel.setText("Υπάρχει ήδη πελάτης με αυτό το email!");
                    messageLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    return;
                }
                Customer customer = new Customer(firstNameInput.getText().trim(), lastNameInput.getText().trim(),
                        emailInput.getText().trim(), phoneInput.getText().trim());
                customerDao.save(customer);
                refreshTable();

                firstNameInput.clear();
                lastNameInput.clear();
                emailInput.clear();
                phoneInput.clear();

                messageLabel.setText("Ο πελάτης προστέθηκε επιτυχώς!");
                messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }catch (Exception e){
                messageLabel.setText("Σφάλμα κατά την αποθήκευση: "+e.getMessage());
                messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        deleteBtn.setOnAction(event ->{
            Customer selectedCustomer = table.getSelectionModel().getSelectedItem();
            if(selectedCustomer == null) {
                messageLabel.setText("Επίλεξε πρώτα έναν πελάτη από τον πίνακα");
                messageLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                return;
            }

            List<Reservation> customerReservations = reservationDao.findAllReservationsWithServices().stream()
                    .filter(r -> r.getCustomerId()!=null && r.getCustomerId().getCustomerId() == selectedCustomer.getCustomerId()).toList();

            if(!customerReservations.isEmpty()){
                String details = customerReservations.stream().map(r -> "• Κράτηση #" + r.getReservationId() + " (Δωμάτιο " + r.getRoomId().getRoomNumber() + ", Κατάσταση: " + r.getStatus() + ")")
                        .collect(java.util.stream.Collectors.joining("\n"));

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Αδυναμία διαγραφής");
                alert.setContentText("Δεν επιτρέπεται η διαγραφή του/της "+selectedCustomer.getFirstName()+" "+selectedCustomer.getLastName() +" λόγω ιστορικού κρατήσεων:\n\n"+details);
                alert.setHeaderText("Ο πελάτης έχει καταχωρημένες κρατήσεις (" + customerReservations.size() + ")");
                alert.showAndWait();

                messageLabel.setText("Αδύνατη η διαγραφή: Ο πελάτης έχει ενεργό ιστορικό κρατήσεων.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Επιβεβαίωση Διαγραφής");
            confirmAlert.setHeaderText("Διαγραφή πελάτη: " + selectedCustomer.getFirstName() + " " + selectedCustomer.getLastName());
            confirmAlert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε οριστικά αυτόν τον πελάτη (" + selectedCustomer.getEmail() + ");");

            confirmAlert.showAndWait().ifPresent(response -> {
               if(response == ButtonType.OK) {
                   try{
                       customerDao.delete(selectedCustomer);
                       refreshTable();
                       messageLabel.setText("🗑️ Ο πελάτης διαγράφηκε.");
                       messageLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                   }catch (Exception e){
                       messageLabel.setText("Αδυναμία διαγραφής (ο πελάτης έχει καταχωρημένες κρατήσεις).");
                       messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                   }
               }
            });
        });

        HBox formBox = new HBox(10, firstNameInput, lastNameInput, emailInput, phoneInput, addBtn);

        HBox.setHgrow(firstNameInput, Priority.ALWAYS);
        HBox.setHgrow(lastNameInput, Priority.ALWAYS);
        HBox.setHgrow(emailInput, Priority.ALWAYS);
        HBox.setHgrow(phoneInput, Priority.ALWAYS);

        HBox actionBox = new HBox(15, deleteBtn, messageLabel);

        formCard.getChildren().addAll(formTitle, formBox, actionBox);

        layout.getChildren().addAll(title, table, formCard);
        return layout;

    }
    public void refreshTable(){
        customerList.clear();
        customerList.addAll(customerDao.findAll());
    }
}