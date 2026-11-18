package com.hotel.model.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class GenericDao <T,ID>{
    private final SessionFactory sessionFactory;//διαβάζει το hibernate.cfg.xml αρχείο και συνδέεται με την PostgreSQL, δηλαδή κρατάει ανοικτή την σύνδεση
    private final Class<T> entityType;//μηχανισμός της Java που επιτρέπει στην κλάση να γνωρίζει ποιος είναι ο τύπος του Entity πχ Customer,Room κτλ

    public GenericDao(SessionFactory sessionFactory, Class<T> entityType){
        this.sessionFactory = sessionFactory;
        this.entityType = entityType;
    }

    public SessionFactory getSessionFactory() { return sessionFactory; }

    public void save(T entity){// δημιουργία και αποθήκευση entity
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
        }
    }

    public void update(T entity){
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        }
    }

    public void delete(T entity){
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
        }
    }

    public T findById(ID id){//αναζήτηση με βάση το primary key
        try(Session session = sessionFactory.openSession()){
            return session.get(entityType, id);
        }
    }

    public long count(){
        try(Session session = sessionFactory.openSession()){
            String sql = "SELECT COUNT(e) FROM "+entityType.getSimpleName()+" e";
            return session.createQuery(sql,Long.class).uniqueResult();
        }
    }

    public List<T> findAll(){
        try(Session session = sessionFactory.openSession()){
            String sql = "FROM "+entityType.getSimpleName();//δημιουργεί το SQL Query το οποίο ισοδυναμεί με SELECT * FROM. Το entityType μπορεί να είναι Customer,Room κτλ
            return session.createQuery(sql, entityType).list();//λέει στο Hibernate ότι τα αποτελέσματα θα είναι αντικείμενα τύπου entityType και με το list() οι εγγραφές/αποτελέσματα μετατρέπονται σε Java αντικείμενα
        }
    }

}