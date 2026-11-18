package com.hotel.model.tables;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name="services")
@Check(constraints = "service_price >=30 ")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="service_id")
    private Integer id;

    @Column(name="service_name", nullable = false)
    private String serviceName;

    @Column(name="service_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal servicePrice;

    public Service(){}

    public Service(String serviceName, BigDecimal servicePrice){
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
    }

    public void setServiceName(String serviceName){this.serviceName = serviceName;}
    public void setServicePrice(BigDecimal servicePrice){this.servicePrice = servicePrice;}

    public Integer getServiceId(){return this.id;}
    public String getServiceName(){return this.serviceName;}
    public BigDecimal getServicePrice(){return this.servicePrice;}
}
