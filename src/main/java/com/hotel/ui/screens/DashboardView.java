package com.hotel.ui.screens;

import com.hotel.model.dao.DashboardDao;
import com.hotel.model.tables.Reservation;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardView {
    private DashboardDao dashboardDao;
    public DashboardView(DashboardDao  dashboardDao) {
        this.dashboardDao = dashboardDao;
    }
    public ScrollPane getView(){
        VBox mainContainer = new VBox(22);
        mainContainer.setPadding(new Insets(28));
        mainContainer.setStyle("-fx-background-color: #f4f6f9");

        //header και ημερομηνία
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(4);
        Label label1 = new Label("Επισκόπηση Ξενοδοχείου");
        label1.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label label2 = new Label("Live στατιστικά & πρόσφατη δραστηριότητα • Ημερομηνία: "+today);
        title.getChildren().addAll(label1,label2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Ανανέωση");
        refreshBtn.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-cursor: hand;");

        header.getChildren().addAll(title, spacer, refreshBtn);

        //4 κάρτες μετρήσεων
        GridPane gridPane = new GridPane();
        gridPane.setHgap(18);
        gridPane.setVgap(18);
        for(int i=0;i<4;i++){
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            gridPane.getColumnConstraints().add(column);
        }
        Label availRoomsLabel = new Label();
        Label activeResLabel = new Label();
        Label revenueLabel = new Label();
        Label customersLabel = new Label();

        gridPane.add(createStatCard("Διαθέσιμα Δωμάτια",availRoomsLabel,"Έτοιμα προς κράτηση","#27ae60"),0,0);
        gridPane.add(createStatCard("Ενεργές Κρατήσεις", activeResLabel, "Επιβεβαιωμένες (Confirmed)", "#2980b9"), 1, 0);
        gridPane.add(createStatCard("Συνολικά Έσοδα", revenueLabel, "Από ενεργές κρατήσεις", "#8e44ad"), 2, 0);
        gridPane.add(createStatCard("Εγγεγραμμένοι Πελάτες", customersLabel, "Στη βάση πελατών", "#e67e22"), 3, 0);

        //πίνακας πρόσφατων κρατήσεων
        VBox recentCard = new VBox(14);
        recentCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        Label recentCardTitle = new Label("Τελευταίες 5 κρατήσεις");
        recentCardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TableView<Reservation> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(270);

        TableColumn<Reservation, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getReservationId())));
        idCol.setMinWidth(65);

        TableColumn<Reservation, String> custCol = new TableColumn<>("Πελάτης");
        custCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCustomerId().getFirstName()+
                " "+cell.getValue().getCustomerId().getLastName()));

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Δωμάτιο");
        roomCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomId().getRoomNumber()+
                " ("+cell.getValue().getRoomId().getRoomType()+")"));

        TableColumn<Reservation, String> datesCol = new TableColumn("Check-In/Check-Out");
        datesCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(String.valueOf(cell.getValue().getCheckInDate()+
                " ➔ "+cell.getValue().getCheckOutDate())));

        TableColumn<Reservation, String> totalPriceCol = new TableColumn<>("Σύνολο");
        totalPriceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(String.valueOf(cell.getValue().getTotalPrice()+"€")));

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Κατάσταση");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        tableView.getColumns().addAll(idCol,custCol,roomCol,datesCol,totalPriceCol,statusCol);
        recentCard.getChildren().addAll(recentCardTitle,tableView);

        Runnable loadDashboardData = () -> {
            long availableRooms = dashboardDao.countAvailableRooms();
            long totalRooms = dashboardDao.countTotalRooms();
            long activeReservations = dashboardDao.countActiveReservations();
            BigDecimal countRevenue = dashboardDao.countTotalRevenue();
            long countCustomers = dashboardDao.countCustomers();

            availRoomsLabel.setText(availableRooms+" / "+totalRooms);
            activeResLabel.setText(String.valueOf(activeReservations));
            revenueLabel.setText(String.format("%.2f €",countRevenue));
            customersLabel.setText(String.valueOf(countCustomers));

            tableView.setItems(FXCollections.observableArrayList(dashboardDao.getRecentReservations(5)));
        };

        loadDashboardData.run();
        refreshBtn.setOnAction(e->loadDashboardData.run());

        mainContainer.getChildren().addAll(header, gridPane, recentCard);
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    public VBox createStatCard(String title, Label labelValue, String subText, String color){
        VBox statCard = new VBox();
        statCard.setPadding(new Insets(18));
        statCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: "+color+"; -fx-border-width: 0 0 0 5; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        Label topLabel = new Label(title);

        labelValue.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subLabel = new Label(subText);
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: "+color+"; -fx-font-weight: bold;");

        statCard.getChildren().addAll(topLabel,labelValue,subLabel);
        return statCard;
    }
}