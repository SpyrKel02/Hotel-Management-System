package com.hotel.model.database_filler;

import com.hotel.model.dao.CustomerDao;
import com.hotel.model.dao.ReservationDao;
import com.hotel.model.dao.RoomDao;
import com.hotel.model.dao.ServiceDao;
import com.hotel.model.tables.Customer;
import com.hotel.model.tables.Room;
import com.hotel.model.tables.Service;
import com.hotel.controller.ReservationController;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//η συγκεκριμένη κλάση είναι υπεύθυνη για να γεμίζει τους πίνακες της βάσης δεδομένων με στοιχεία και πληροφορίες
//έτσι ώστε να μην είναι τελείως κενή όταν γίνεται η εκκίνηση της εφαρμογής
//ουσιαστικά η εφαρμογή γίνεται πιο "ζωντανή"

public class DatabaseFiller {
    public static void fill(SessionFactory sessionFactory) {
        CustomerDao customerDao = new CustomerDao(sessionFactory);
        RoomDao roomDao = new RoomDao(sessionFactory);
        ServiceDao serviceDao = new ServiceDao(sessionFactory);
        ReservationDao reservationDao = new ReservationDao(sessionFactory);

        if(roomDao.findAvailableRooms().isEmpty()){
            roomDao.save(new Room("101","Available",new BigDecimal("65.00"),"Single"));
            roomDao.save(new Room("102","Available",new BigDecimal("65.00"),"Single"));
            roomDao.save(new Room("103","Available",new BigDecimal("65.00"),"Single Deluxe"));

            roomDao.save(new Room("201","Available",new BigDecimal("100.00"),"Double"));
            roomDao.save(new Room("202","Available",new BigDecimal("100.00"),"Double"));
            roomDao.save(new Room("203","Available",new BigDecimal("130.00"),"Double Deluxe"));

            roomDao.save(new Room("301","Available",new BigDecimal("160.00"),"Family"));
            roomDao.save(new Room("302","Under Maintenance",new BigDecimal("160.00"),"Family"));

            roomDao.save(new Room("401","Available",new BigDecimal("260.00"),"Suite"));
            roomDao.save(new Room("402","Available",new BigDecimal("360.00"),"Presidential"));

            System.out.println("[Database] 10 rooms were added successfully.");
        }

        if(serviceDao.count() == 0){
            serviceDao.save(new Service("Breakfast Buffet",new BigDecimal("35.00")));
            serviceDao.save(new Service("Spa, Sauna & Jacuzzi",new BigDecimal("40.00")));
            serviceDao.save(new Service("VIP Taxi/Limousine",new BigDecimal("60.00")));

            System.out.println("[Database] 3 services have been added successfully.");
        }

        if(customerDao.count() == 0){
            customerDao.save(new Customer("George", "Papadopoulos", "giorgos@email.gr", "6971111111"));
            customerDao.save(new Customer("Eleni", "Vasileiou", "eleni@email.gr", "6982222222"));
            customerDao.save(new Customer("Kostas", "Nikolaou", "kostas@email.gr", "6943333333"));

            System.out.println("[Database] 3 customers were added successfully.");
        }

        if(reservationDao.count() == 0){
            List<Customer> customers = customerDao.findAll();
            List<Room> availableRooms = roomDao.findAvailableRooms();
            List<Service> availableServices = serviceDao.findAllServices();

            Room targetRoom = availableRooms.stream()
                    .filter(r -> "203".equals(r.getRoomNumber()))
                    .findFirst()
                    .orElse(null);

            ReservationController resHandler = new ReservationController(sessionFactory, roomDao);

            if(targetRoom != null && !customers.isEmpty() && !availableServices.isEmpty()){
                Customer customer = customers.get(0);
                Service cheapestService = availableServices.get(0);
                BigDecimal totalPrice = targetRoom.getPricePerNight().multiply(BigDecimal.valueOf(2)).add(cheapestService.getServicePrice());

                resHandler.makeReservation(customer, targetRoom, LocalDate.now(),LocalDate.now().plusDays(2),
                        2,0,List.of(cheapestService));

                System.out.println("[Database] 1 reservation created successfully.");
            }
        }
    }
}