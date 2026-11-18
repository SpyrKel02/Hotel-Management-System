package com.hotel.model.tables;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//μέσω του πίνακα Reservation, γίνεται η σύνδεση μεταξύ των πινάκων Customer και Room
@Entity
@Table(name="reservations")
@Check(constraints = "check_out_date > check_in_date AND adults_count > 0 AND children_count >= 0 AND total_price >= 0")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="reservation_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name="check_in_date")
    private LocalDate checkInDate;

    @Column(name="check_out_date")
    private LocalDate checkOutDate;

    @Column(name="adults_count", nullable = false)
    private Integer adultsCount;

    @Column(name="children_count",nullable = false)
    private Integer childrenCount;

    @Column(name="total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name="status", nullable = false, length = 20)
    private String status;

    public Reservation(){}

    public Reservation(Customer customer, Room room, LocalDate checkInDate,
                       LocalDate checkOutDate, Integer adultsCount, Integer childrenCount,
                       BigDecimal totalPrice, String status){
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.adultsCount = adultsCount;
        this.childrenCount = childrenCount;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public void setCustomerId(Customer customed) {this.customer = customed;}
    public void setRoomId(Room roomId) {this.room = room;}
    public void setCheckInDate(LocalDate checkInDate) {this.checkInDate = checkInDate;}
    public void setCheckOutDate(LocalDate checkOutDate) {this.checkOutDate = checkOutDate;}
    public void setAdultsCount(Integer adultsCount) {this.adultsCount = adultsCount;}
    public void setChildrenCount(Integer childrenCount) {this.childrenCount = childrenCount;}
    public void setTotalPrice(BigDecimal totalPrice) {this.totalPrice = totalPrice;}
    public void setStatus(String status) {this.status = status;}

    public Integer getReservationId(){return this.id;}
    public Customer getCustomerId(){return this.customer;}
    public Room getRoomId(){return this.room;}
    public  LocalDate getCheckInDate(){return this.checkInDate;}
    public  LocalDate getCheckOutDate(){return this.checkOutDate;}
    public  Integer getAdultsCount(){return this.adultsCount;}
    public  Integer getChildrenCount(){return this.childrenCount;}
    public  BigDecimal getTotalPrice(){return this.totalPrice;}
    public  String getStatus(){return this.status;}


    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "reservation_services", joinColumns = @JoinColumn(name = "reservation_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<Service> services = new ArrayList<>();

    public void setServices(List<Service> services){this.services=services;}
    public List<Service> getServices(){return this.services;}
}