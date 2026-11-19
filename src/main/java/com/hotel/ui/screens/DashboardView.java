package com.hotel.ui.screens;

import com.hotel.model.dao.DashboardDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

        mainContainer.getChildren().addAll(header);
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
}