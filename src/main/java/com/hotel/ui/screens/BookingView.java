package com.hotel.ui.screens;

import com.hotel.controller.ReservationController;
import com.hotel.model.dao.CustomerDao;
import com.hotel.model.dao.RoomDao;
import com.hotel.model.dao.ServiceDao;
import com.hotel.model.tables.Customer;
import com.hotel.model.tables.Room;
import com.hotel.model.tables.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingView {

    private final RoomDao roomDao;
    private final CustomerDao customerDao;
    private final ServiceDao serviceDao;
    private final ReservationController resController;

    private ObservableList<Room> roomList;
    private Map<CheckBox, Service> servicesCheckBoxes;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;
    private Spinner adultsSpinner;
    private Spinner childrenSpinner;
    private TableView<Room> availableRoomsTable;
    private Label estimatedPriceLabel;

    public BookingView(RoomDao roomDao, CustomerDao customerDao, ServiceDao serviceDao, ReservationController resController) {
        this.roomDao = roomDao;
        this.customerDao = customerDao;
        this.serviceDao = serviceDao;
        this.resController = resController;
        this.roomList = FXCollections.observableArrayList();
        this.servicesCheckBoxes = new HashMap<>();
    }

    public ScrollPane getView(){
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #f4f6f9;");

        Label headerLabel = new Label("Κάντε την κράτηση σας");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox columnsBox = new HBox(25);//οριζόντιο container όπου θα χωρίζει της οθόνες σε δύο στήλες

        // αρχή κάθετου container για την αριστερή στήλη με τις ημερομηνίες και τα δωμάτια
        VBox leftColumn = new VBox(15);
        leftColumn.setPrefWidth(550);
        leftColumn.setStyle("-fx-background-color: white;" +
                "-fx-padding: 20; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");//ορίζει λευκό φόντο, γωνίες και σκιά
        Label label1 = new Label("1: Επιλογή Ημερομηνιών & Ατόμων");
        label1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        //αρχή δημιουργίας των ημερομηνιών
        checkInPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkOutPicker = new DatePicker(LocalDate.now().plusDays(3));

        adultsSpinner = new Spinner<>(1,6,2);//Spinner για επιλογή ενηλίκων από 1 εώς 6 με default τιμή 2
        adultsSpinner.setPrefWidth(80);

        childrenSpinner = new Spinner<>(0,4,0);//Spinner για επιλογή παιδιών από 0 εώς 4 με default τιμή 0
        checkInPicker.setPrefWidth(80);

        checkInPicker.setOnAction(event -> updateEstimatedPrice());
        checkOutPicker.setOnAction(event -> updateEstimatedPrice());

        GridPane datesGrid = new GridPane();
        datesGrid.setHgap(15);
        datesGrid.setVgap(10);
        datesGrid.add(new Label("Check-in:"), 0, 0);
        datesGrid.add(checkInPicker, 1, 0);
        datesGrid.add(new Label("Check-out:"), 0, 1);
        datesGrid.add(checkOutPicker, 1, 1);
        datesGrid.add(new Label("Ενήλικες:"), 2, 0);
        datesGrid.add(adultsSpinner, 3, 0);
        datesGrid.add(new Label("Παιδιά:"), 2, 1);
        datesGrid.add(childrenSpinner, 3, 1);

        Label label2 = new Label("Επιλέξτε Διαθέσιμο Δωμάτιο");
        label2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 10 0 0 0;");

        //αρχή δημιουργίας του πίνακα δωματίων
        availableRoomsTable = new TableView<>();

        TableColumn<Room, String> numCol = new TableColumn<>("Αριθμός Δωματίου");//στήλη όπου διαβάζει το getRoomNumber από την κλάση Room
        numCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        numCol.setPrefWidth(145);

        TableColumn<Room, String> typeCol = new TableColumn<>("Τύπος δωματίου");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        typeCol.setPrefWidth(180);

        TableColumn<Room, BigDecimal> priceCol = new TableColumn<>("Τιμή / Νύχτα (€)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        priceCol.setPrefWidth(130);

        availableRoomsTable.getColumns().addAll(numCol, typeCol, priceCol);//προσθήκη των τριών στηλών στον πίνακα
        availableRoomsTable.setItems(roomList);
        availableRoomsTable.setPrefHeight(260);
        availableRoomsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateEstimatedPrice());
        refreshAvailableRooms();
        leftColumn.getChildren().addAll(label1, datesGrid, label2, availableRoomsTable);

        // αρχή κάθετου container για την δεξία στήλη με τις υπηρεσίες, τα στοιχεία και την κράτηση
        VBox rightColumn = new VBox(15);
        rightColumn.setPrefWidth(450);
        rightColumn.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");
        Label label3 = new Label("Προσθέστε Έξτρα Υπηρεσίες (Προαιρετικά)");
        label3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        VBox servicesBox = new VBox(8);
        List<Service> services = serviceDao.findAllServices();
        for(Service s:services){
            CheckBox checkBox = new CheckBox(s.getServiceName()+" (+"+s.getServicePrice()+"€");
            checkBox.setStyle("-fx-font-size: 13px");
            checkBox.setOnAction(event -> updateEstimatedPrice());
            servicesCheckBoxes.put(checkBox,s);
            servicesBox.getChildren().add(checkBox);
        }

        Label label4 = new Label("Τα Στοιχεία σας");
        label4.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 10 0 0 0;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email (π.χ giorgos@email.gr");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Όνομα");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Επώνυμο");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Τηλέφωνο");

        //κουμπί αυτόματης συμπλήρωσης σε περίπτωση που υπάρχει ο πελάτης μέσω email
        Button checkEmailBtn =  new Button("Έλεγχος υπάρχοντος πελάτη");
        checkEmailBtn.setOnAction(event -> {
            String email = emailField.getText().trim();//πέρνει το email χωρίς κενά μέσω του trim()
            Customer existingCustomer = customerDao.findByEmail(email);
            if(existingCustomer!=null){
                firstNameField.setText(existingCustomer.getFirstName());
                lastNameField.setText(existingCustomer.getLastName());
                phoneField.setText(existingCustomer.getPhone());
            }
        });

        estimatedPriceLabel = new Label("Εκτιμώμενο Συνολικό Κόστος: 0.00 €");//Ετικέτα εμφάνισης εκτιμώμενου κόστους
        estimatedPriceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-padding: 10 0;");

        Button resButton = new Button("Ολοκλήρωση Κράτησης");//κουμπί για ολοκλήρωση κράτησης
        resButton.setMaxWidth(Double.MAX_VALUE);
        resButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand;");

        Label statusLabel = new Label();//ετικέτα για μήνυμα επιτυχίας ή σφάλματος κάτω από το κουμπί ολοκλήρωσης κράτησης
        statusLabel.setWrapText(true);

        resButton.setOnAction(event -> {
            Room selectedRoom =  availableRoomsTable.getSelectionModel().getSelectedItem();
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if(selectedRoom==null){
                showStatus(statusLabel,"Παρακαλώ επιλέξτε ένα διαθέσιμο δωμάτιο","#e67e22");
                return;
            }
            if(checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)){
                showStatus(statusLabel,"H ημερομηνία Check-out πρέπει να είναι μετά την ημερομηνία Check-in", "#e67e22");
                return;
            }
            if(emailField.getText().isEmpty() || firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || phoneField.getText().isEmpty()){
                showStatus(statusLabel,"Παρακαλώ συμπληρώστε email, όνομα, επώνυμο και αριθμό τηλεφώνου","#e67e22");
                return;
            }
            try{
                String email = emailField.getText().trim();
                Customer customer = customerDao.findByEmail(email);//ελέγχει αν υπάρχει ο πελάτης μέσω email
                if(customer == null){//αν δεν υπάρχει τότε τον δημιουργεί
                    customer = new Customer(firstNameField.getText().trim(),lastNameField.getText().trim(),email, phoneField.getText().trim());
                }
                List<Service> chosenServices = new ArrayList<>();//συλλογή λίστας με βάση τις υπηρεσίες που επέλεξε ο πελάτης
                for(Map.Entry<CheckBox, Service> entry: servicesCheckBoxes.entrySet()){
                    if(entry.getKey().isSelected()){
                        chosenServices.add(entry.getValue());
                    }
                }
                int reservationId = resController.makeReservation(customer,selectedRoom,checkIn,checkOut,
                        (Integer) adultsSpinner.getValue(), (Integer) childrenSpinner.getValue(),chosenServices);

                if(reservationId>0){//σημαίνει ότι η κράτηση πέτυχε
                    refreshAvailableRooms();

                    firstNameField.setText("");
                    lastNameField.setText("");
                    phoneField.setText("");
                    emailField.setText("");

                    servicesCheckBoxes.keySet().forEach(checkBox->checkBox.setSelected(false));
                    updateEstimatedPrice();
                    showStatus(statusLabel,"Η κράτηση #"+reservationId+" ολοκληρώθηκε επιτυχώς! Το δωμάτιο "+selectedRoom.getRoomNumber()+" κατοχυρώθηκε.","#27ae60");
                }else if(reservationId == -2){
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("Περιορισμός Κράτησης");
                    warning.setHeaderText("Υπάρχει ήδη ενεργή κράτηση");
                    warning.setContentText("Ο πελάτης " + customer.getFirstName() + " " + customer.getLastName()
                            + " (" + customer.getEmail() + ") έχει ήδη μία ενεργή (Confirmed) κράτηση και δεν μπορεί να πραγματοποιήσει δεύτερη.");
                    warning.showAndWait();
                }else{//επιστρέφει -1
                    showStatus(statusLabel," Το δωμάτιο που επιλέξατε δεν είναι διαθέσιμο ή προέκυψε σφάλμα","#e74c3c");
                }
            }catch (Exception e){
                showStatus(statusLabel,"Αποτυχία κράτησης"+e.getMessage(),"#e74c3c");
            }
        });

        //πρόσθεση όλων των στοιχείων στην δεξιά κάρτα
        rightColumn.getChildren().addAll(
                label3, servicesBox, label4, new HBox(10,emailField,checkEmailBtn),
                firstNameField, lastNameField, phoneField, estimatedPriceLabel, resButton, statusLabel
        );

        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        mainContainer.getChildren().addAll(headerLabel,columnsBox);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    public void refreshAvailableRooms(){
        roomList.clear();
        roomList.addAll(roomDao.findAvailableRooms());
    }

    public void updateEstimatedPrice(){
        Room selected = availableRoomsTable.getSelectionModel().getSelectedItem();
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();

        // αν δεν έχει επιλεγεί δωμάτιο ή οι ημερομηνίες είναι λάθος, δείχνει 0.00 €
        if (selected == null || in == null || out == null || !out.isAfter(in)) {
            estimatedPriceLabel.setText("Εκτιμώμενο Σύνολο: 0.00 €");
            return;
        }

        // uπολογίζει τις ημέρες διαμονής
        long nights = ChronoUnit.DAYS.between(in, out);
        // Πολλαπλασιάζει την τιμή δωματίου επί τις νύχτες
        BigDecimal total = selected.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // προσθέτει την τιμή κάθε τσεκαρισμένης υπηρεσίας
        for (Map.Entry<CheckBox, Service> entry : servicesCheckBoxes.entrySet()) {
            if (entry.getKey().isSelected()) {
                total = total.add(entry.getValue().getServicePrice());
            }
        }
        estimatedPriceLabel.setText(String.format("Εκτιμώμενο Σύνολο (%d νύχτες): %.2f €", nights, total));
    }

    public void showStatus(Label statusLabel,String message, String colorHex){
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: "+colorHex+"; -fx-font-weight: bold; -fx-font-size: 13px;");
    }
}
