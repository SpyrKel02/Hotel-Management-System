package com.hotel.model.dao;

import com.hotel.model.tables.Service;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class ServiceDao extends GenericDao<Service, Integer>{

    public ServiceDao(SessionFactory sessionFactory) {//διαχειρίζεται τις έξτρα υπηρεσίες του ξενοδοχείου

        super(sessionFactory, Service.class);
    }

    //συνάρτηση με query που βρίσκει και επιστρέφει όλες τις υπηρεσίες ταξινομημένες με βάση την τιμή τους από την πιο φθηνή έως την πιο ακριβή
    public List<Service> findAllServices(){
        try(Session session = getSessionFactory().openSession()){
            String sql = "FROM Service s ORDER BY s.servicePrice ASC";
            return session.createQuery(sql,Service.class).list();
        }
    }

}