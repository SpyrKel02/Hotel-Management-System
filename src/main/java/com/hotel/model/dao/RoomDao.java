package com.hotel.model.dao;

import com.hotel.model.tables.Room;
import jakarta.persistence.LockModeType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class RoomDao extends GenericDao<Room, Integer> {

    public RoomDao(SessionFactory sessionFactory) {
        super(sessionFactory, Room.class);
    }

    ////ουσιαστικά μέσω του PESSIMISTIC_WRITE αποτρέπεται το διάβασμα, η τροποποίηση και η διαγραφή. Οπότε η περίπτωση double booking το δωματίου θα αποτραπεί
    public Room findRoomByID(Session session, Integer roomID){
        return session.find(Room.class, roomID, LockModeType.PESSIMISTIC_WRITE);
    }

    public List<Room> findAvailableRooms(){//επιστρέφει όλα τα διαθέσιμα δωμάτια
        try(Session session = getSessionFactory().openSession()){
            String sql = "FROM Room room WHERE room.status = :status";
            return session.createQuery(sql,Room.class).setParameter("status","Available").list();
        }
    }
}