package com.hotel.model.dao;

import com.hotel.model.tables.Customer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class CustomerDao extends GenericDao<Customer,Integer> {
    public CustomerDao(SessionFactory sessionFactory) {
        super(sessionFactory, Customer.class);
    }

    public Customer findByEmail(String email){
        try(Session session = getSessionFactory().openSession()){
            String sql = "FROM Customer customer WHERE customer.email = :email";
            return session.createQuery(sql,Customer.class).setParameter("email",email).uniqueResult();
        }
    }
}
