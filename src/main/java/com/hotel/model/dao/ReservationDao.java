package com.hotel.model.dao;

import com.hotel.model.tables.Reservation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class ReservationDao extends GenericDao<Reservation,Integer>{

    public ReservationDao(SessionFactory sessionFactory){
        super(sessionFactory, Reservation.class);
    }

    public List<Reservation> findAllReservationsWithServices(){
        try(Session session =  getSessionFactory().openSession()){
            String sql = "SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.services";
            return session.createQuery(sql,Reservation.class).list();
        }
    }

    public void calculateTotalBill(Integer reservationId){
        try(Session session =  getSessionFactory().openSession()){
            org.hibernate.Transaction transaction = session.beginTransaction();
            session.createNativeQuery("CALL calculate_reservation_total(:resId)",Object.class)
                    .setParameter("resId",reservationId)
                    .executeUpdate();
            transaction.commit();
        }
    }
}