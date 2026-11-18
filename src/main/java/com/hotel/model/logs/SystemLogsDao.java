package com.hotel.model.logs;

import com.hotel.model.dao.GenericDao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class SystemLogsDao extends GenericDao<SystemLogs, Integer> {
    public SystemLogsDao(SessionFactory sessionFactory){
        super(sessionFactory, SystemLogs.class);
    }
    public List<SystemLogs> returnLatestLogs(){
        try(Session session = getSessionFactory().openSession()){
            String sql = "FROM SystemLogs l ORDER BY l.actionCreatedAt DESC";
            return session.createQuery(sql,SystemLogs.class).list();
        }
    }
}