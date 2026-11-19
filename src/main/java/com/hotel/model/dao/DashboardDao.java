package com.hotel.model.dao;

import org.hibernate.SessionFactory;

public class DashboardDao{

    private final SessionFactory sessionFactory;

    public DashboardDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}