package com.hotel.controller;

import com.hotel.model.dao.RoomDao;
import com.hotel.model.tables.Customer;
import com.hotel.model.tables.Reservation;
import com.hotel.model.tables.Room;
import com.hotel.model.tables.Service;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReservationController {

    private final SessionFactory sessionFactory;
    private final RoomDao roomDao;

    public ReservationController(SessionFactory sessionFactory, RoomDao roomDao){
        this.sessionFactory = sessionFactory;
        this.roomDao = roomDao;
    }

    public int makeReservation(Customer customer, Room targetRoom, LocalDate checkInDate,
                               LocalDate checkOutDate, Integer adultsCount, Integer childrenCount,
                               List<Service> selectedServices){

        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();

            System.out.println("[" + customer.getEmail() + "] Προσπάθεια ανάκτησης και κλειδώματος του Δωματίου ID: " + targetRoom.getRoomId());

            Room room = roomDao.findRoomByID(session,targetRoom.getRoomId());

            if(room == null){
                System.out.println("[" + customer.getEmail() + "] Το δωμάτιο δεν βρέθηκε: ");
                transaction.rollback();
                return -1;
            }
            if(!"Available".equalsIgnoreCase(room.getStatus())){
                System.out.println("["+ customer.getEmail() + "] Το δωμάτιο "+ room.getRoomNumber()+" δεν είναι διαθέσιμο: "+room.getStatus());
                transaction.rollback();
                return -1;
            }

            //έλεγχος αν ο πελάτης έχει ήδη ενεργή κράτηση
            Long activeReservation = session.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.status = 'Confirmed' AND ("+
                            "LOWER(r.customer.email) = LOWER(:email) OR "+
                            "(LOWER(r.customer.firstName) = LOWER(:firstName) AND LOWER(r.customer.lastName) = LOWER(:lastName)) OR "+
                            "(:customerId IS NOT NULL AND r.customer.id = :customerId))",Long.class)
                    .setParameter("email",customer.getEmail().trim())
                    .setParameter("firstName",customer.getFirstName().trim())
                    .setParameter("lastName",customer.getLastName().trim())
                    .setParameter("customerId",customer.getCustomerId()).uniqueResult();
            if(activeReservation != null && activeReservation > 0){
                System.out.println("[" + customer.getEmail() + "] Αποτυχία: Ο πελάτης (" + customer.getFirstName() + " " + customer.getLastName() + ") έχει ήδη ενεργή κράτηση!");
                transaction.rollback();
                return -2;
            }

            Customer managedCustomer = session.merge(customer);

            Reservation reservation = new Reservation(managedCustomer, room, checkInDate, checkOutDate,
                    adultsCount, childrenCount,BigDecimal.ZERO,"Confirmed");

            if(selectedServices != null && !selectedServices.isEmpty()){
                reservation.setServices(selectedServices);
            }

            Reservation savedReservation = session.merge(reservation);
            session.flush();
            int newReservationId = savedReservation.getReservationId();

            session.createNativeMutationQuery("CALL calculate_reservation_total(:resId)")//μέσω της Stored Procedures γίνεται ο αυτόματος υπολογισμός του κόστους total_price
                    .setParameter("resId", newReservationId)
                    .executeUpdate();

            room.setStatus("Booked");
            session.merge(room);
            transaction.commit();
            System.out.println("Επιτυχής κράτηση δωματίου "+room.getRoomNumber()+" στον πελάτη με email "+customer.getEmail()+" με reservation id: "+newReservationId);
            return newReservationId;
        }catch(Exception e){
            if(transaction != null && transaction.getStatus().canRollback()){
                transaction.rollback();
            }
            System.out.println("Αποτυχία συναλλαγής: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean cancelReservation(int reservationId){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();

            Reservation reservation = session.get(Reservation.class, reservationId);
            if(reservation == null || "Cancelled".equals(reservation.getStatus())){
                transaction.rollback();
                return false;
            }

            reservation.setStatus("Cancelled");
            Room room = reservation.getRoomId();
            if(room!=null){
               room.setStatus("Available");
               session.merge(room);
            }
            session.merge(reservation);
            transaction.commit();
            return true;
        }catch(Exception e){
            if(transaction != null && transaction.getStatus().canRollback()){
                transaction.rollback();
            }
            System.out.println("Αποτυχία ακύρωσης: "+e.getMessage());
            return false;
        }
    }
}