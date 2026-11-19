package com.hotel.model.dao;

import com.hotel.model.tables.Reservation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StatisticsDao {

    private final SessionFactory sessionFactory;
    public StatisticsDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Reservation> Query1(LocalDate from, LocalDate to) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.services WHERE 1=1 "
            );
            if (from != null) sql.append(" AND r.checkInDate >= :fromDate ");
            if (to != null) sql.append(" AND r.checkOutDate <= :toDate ");
            sql.append(" ORDER BY r.checkInDate ASC");

            var query = session.createQuery(sql.toString(), Reservation.class);
            if (from != null) query.setParameter("fromDate", from);
            if (to != null) query.setParameter("toDate", to);

            return query.list();
        }
    }

    public List<Object[]> Query2(String selectedType){
        try(Session session = sessionFactory.openSession()) {
            String sql = "SELECT r.room.roomType, COUNT(r), SUM(r.totalPrice), AVG(r.totalPrice) " +
                    " FROM Reservation r WHERE r.status = 'Confirmed' " +
                    ("Όλοι οι τύποι".equals(selectedType) ? "" : "AND LOWER(r.room.roomType) LIKE LOWER(:rType) ") +
                    "GROUP BY r.room.roomType ORDER BY SUM(r.totalPrice) DESC";
            var query = session.createQuery(sql, Object[].class);
            if (!"Όλοι οι τύποι".equals(selectedType)) {
                query.setParameter("rType", "%" + selectedType + "%");
            }
            return query.list();
        }
    }

    public List<Reservation> Query3(BigDecimal minAmount){
        try(Session session = sessionFactory.openSession()){
            String sql = "SELECT r FROM Reservation r WHERE r.totalPrice >= :minVal ORDER BY r.totalPrice DESC";
            return session.createQuery(sql, Reservation.class).setParameter("minVal", minAmount).list();
        }
    }

    public List<Object[]> Query4(String service){
        try(Session session = sessionFactory.openSession()){
            String sql = "SELECT s.serviceName, s.servicePrice, COUNT(r), SUM(s.servicePrice) " +
                    "FROM Reservation r JOIN r.services s " +
                    "WHERE r.status = 'Confirmed' " +
                    (service.isEmpty() ? "" : "AND LOWER(s.serviceName) LIKE LOWER(:sName) ") +
                    "GROUP BY s.serviceName, s.servicePrice ORDER BY COUNT(r) DESC";

            var query = session.createQuery(sql, Object[].class);
            if(!service.isEmpty()){
                query.setParameter("sName","%"+service+"%");
            }
            return query.list();
        }
    }

    public List<Object[]> Query5(String customer){
        try(Session session = sessionFactory.openSession()){
            String sql = "SELECT c.firstName, c.lastName, c.email, COUNT(r), " +
                    "COALESCE(SUM(CASE WHEN r.status = 'Confirmed' THEN r.totalPrice ELSE 0 END), 0) " +
                    "FROM Customer c LEFT JOIN Reservation r ON r.customer = c " +
                    (customer.isEmpty() ? "" :
                            "WHERE LOWER(c.lastName) LIKE LOWER(:kw) " +
                                    "OR LOWER(c.firstName) LIKE LOWER(:kw) " +
                                    "OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(:kw) " +
                                    "OR LOWER(CONCAT(c.lastName, ' ', c.firstName)) LIKE LOWER(:kw) " +
                                    "OR LOWER(c.email) LIKE LOWER(:kw) ") +
                    "GROUP BY c.id, c.firstName, c.lastName, c.email " +
                    "ORDER BY COALESCE(SUM(CASE WHEN r.status = 'Confirmed' THEN r.totalPrice ELSE 0 END), 0) DESC";

            var query = session.createQuery(sql, Object[].class);
            if (!customer.isEmpty()) {
                query.setParameter("kw", "%"+customer+ "%");
            }
            return query.list();
        }
    }
}
