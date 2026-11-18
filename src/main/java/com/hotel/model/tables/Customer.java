package com.hotel.model.tables;

import jakarta.persistence.*;

@Entity
@Table(name="customers")
public class Customer {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="customer_id")
    private Integer id;

    @Column(name="first_name",nullable=false)
    private String firstName;

    @Column(name="last_name",nullable=false)
    private String lastName;

    @Column(name="email",nullable=false,unique=true)
    private String email;

    @Column(name="phone",nullable=false)
    private String phone;

    public Customer(String firstName, String lastName, String email, String phone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public Customer(){}

    public void setCustomerFirstName(String firstName){this.firstName=firstName;}
    public void setCustomerLastName(String lastName){this.lastName=lastName;}
    public void setCustomerEmail(String email){this.email=email;}
    public void setCustomerPhone(String phone){this.phone=phone;}

    public Integer getCustomerId(){return this.id;}
    public String getFirstName(){return this.firstName;}
    public String getLastName(){return this.lastName;}
    public String getEmail(){return this.email;}
    public String getPhone(){return this.phone;}
}
