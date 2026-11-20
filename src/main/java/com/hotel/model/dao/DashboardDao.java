package com.hotel.model.dao;

import com.hotel.model.tables.Reservation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDao{

    private final SessionFactory sessionFactory;

    public DashboardDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public long countAvailableRooms() {
        try (Session session = sessionFactory.openSession()) {
            List<String> statuses = session.createQuery("SELECT rm.status FROM Room rm", String.class).list();
            System.out.println("Statuses από Hibernate: " + statuses);

            Long count = session.createQuery(
                    "SELECT COUNT(rm) FROM Room rm WHERE TRIM(LOWER(rm.status)) = 'available'",
                    Long.class
            ).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public long countTotalRooms(){
        try(Session session = sessionFactory.openSession()){
            Long count = session.createQuery("SELECT COUNT(room) FROM Room room", Long.class).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public long countActiveReservations(){
        try(Session session = sessionFactory.openSession()){
            Long count = session.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.status = 'Confirmed'",Long.class).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public BigDecimal countTotalRevenue(){
        try(Session session = sessionFactory.openSession()){
            BigDecimal revenue = session.createQuery("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'Confirmed'",BigDecimal.class).uniqueResult();
            return revenue != null ? revenue : BigDecimal.ZERO;
        }
    }

    public long countCustomers(){
        try(Session session = sessionFactory.openSession()){
            Long count = session.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public List<Reservation> getRecentReservations(int limit){
        try(Session session = sessionFactory.openSession()){
            return session.createQuery("SELECT r FROM Reservation r JOIN FETCH r.customer JOIN FETCH r.room ORDER BY r.id DESC",
                    Reservation.class).setMaxResults(limit).list();
        }
    }

}