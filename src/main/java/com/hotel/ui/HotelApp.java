package com.hotel.ui;

import com.hotel.controller.ReservationController;
import com.hotel.model.dao.CustomerDao;
import com.hotel.model.dao.ReservationDao;
import com.hotel.model.dao.RoomDao;
import com.hotel.model.dao.ServiceDao;
import com.hotel.model.database_connection.HibernateUtil;
import com.hotel.model.database_filler.DatabaseFiller;
import com.hotel.ui.screens.BookingView;
import com.hotel.ui.screens.BookingsListView;
import com.hotel.ui.screens.CustomerView;
import com.hotel.ui.screens.SearchQueriesView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.SessionFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class HotelApp extends Application {

    private BorderPane mainLayout;

    private RoomDao roomDao;
    private ServiceDao serviceDao;
    private CustomerDao customerDao;
    private ReservationDao reservationDao;
    private ReservationController reservationController;

    @Override
    public void start(Stage primaryStage){
        //Σύνδεση με το database και γέμισμα αν είναι άδειο
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        //DatabaseFiller.fill(sessionFactory);

        this.roomDao = new RoomDao(sessionFactory);
        this.customerDao = new CustomerDao(sessionFactory);
        this.serviceDao = new ServiceDao(sessionFactory);
        this.reservationDao = new ReservationDao(sessionFactory);
        this.reservationController = new ReservationController(sessionFactory,roomDao);

        mainLayout = new BorderPane();
        mainLayout.setLeft(sideMenu(sessionFactory));
        mainLayout.setCenter(placeholder("Καλώς ήρθατε στο Hotel Management System!"));

        Scene scene = new Scene(mainLayout, 1180, 740);
        primaryStage.setTitle("Hotel Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    private VBox sideMenu(SessionFactory sessionFactory) {
        VBox menu = new VBox(12);
        menu.setPadding(new Insets(20, 15, 20, 15));
        menu.setPrefWidth(230);
        menu.setStyle("-fx-background-color: #2c3e50;");

        Label brandLabel = new Label("HOTEL");
        brandLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");

        //αρχικοποίηση κουμπιών στο Menu
        Button btnBooking = MenuButton("Κάνε Κράτηση");
        Button btnBookingsList = MenuButton("Κρατήσεις");
        Button btnSearch = MenuButton("Αναζήτηση & Στατιστικά");
        Button btnCustomers = MenuButton("Πελάτες");
        Button btnLogs = MenuButton("Καταγραφή Ενεργειών");

        //ενέργειες κουμπιών
        btnBooking.setOnAction(event -> {
            mainLayout.setCenter(placeholder("Κάνε Κράτηση"));
            BookingView view = new BookingView(roomDao, customerDao, serviceDao, reservationController);
            mainLayout.setCenter(view.getView());
        });
        btnBookingsList.setOnAction(event -> {
            mainLayout.setCenter(placeholder("Λίστα Κρατήσεων"));
            BookingsListView view = new BookingsListView(reservationDao, reservationController);
            mainLayout.setCenter(view.getView());
        });
        btnSearch.setOnAction(event -> {
            mainLayout.setCenter(placeholder("Αναζήτηση & Στατιστικά"));
            SearchQueriesView view = new SearchQueriesView(sessionFactory);
            mainLayout.setCenter(view.getView());
        });
        btnCustomers.setOnAction(event -> {
            mainLayout.setCenter(placeholder("Πελάτες"));
            CustomerView view = new CustomerView(customerDao, reservationDao);
           mainLayout.setCenter(view.getView());
        });
        btnLogs.setOnAction(event -> mainLayout.setCenter(placeholder("Καταγραφή Ενεργειών")));

        menu.getChildren().addAll(brandLabel, btnBooking, btnBookingsList, btnSearch, btnCustomers, btnLogs);

        return menu;
    }

    private Button MenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-radius: 5; -fx-cursor: hand;";

        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        return btn;
    }

    private VBox placeholder(String text){
        VBox box = new VBox(new Label(text));
        box.setPadding(new Insets(30));
        box.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        return box;
    }

    @Override
    public void stop(){
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}