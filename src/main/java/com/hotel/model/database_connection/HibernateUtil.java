package com.hotel.model.database_connection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    //Η κλάση HibernateUtil διαχειρίζεται τη σύνδεση της Java εφαρμογής με τη βάση δεδομένων μέσω του Hibernate
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory(){
        try{
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            return configuration.buildSessionFactory();
        }catch(Throwable ex){
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
    public static void shutdown(){
        getSessionFactory().close();
    }
}