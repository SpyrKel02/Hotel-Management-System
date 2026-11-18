package com.hotel.model.tables;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name="rooms")
@Check(constraints = "price_per_night >= 50")
public class Room {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="room_id")
    private Integer id;

    @Column(name="room_number", nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Column(name="status", nullable = false, length = 20)
    private String status;

    @Column(name="price_per_night",nullable = false, precision = 10, scale = 2)//precision δηλαδή πόσα συνολικά ψηφία μπορεί να έχει ο αριθμός και scale πόσα δεκαδικά ψηφία
    private BigDecimal pricePerNight;

    @Column(name="room_type",nullable = false, length=20)
    private String roomType;

    public Room(){}

    public Room(String roomNumber, String status, BigDecimal pricePerNight, String roomType){
        this.roomNumber = roomNumber;
        this.status = status;
        this.pricePerNight = pricePerNight;
        this.roomType = roomType;
    }

    public void setRoomNumber(String roomNumber){this.roomNumber = roomNumber;}
    public void setStatus(String status){this.status = status;}
    public void setRoomType(String roomType){this.roomType = roomType;}
    public void setPricePerNight(BigDecimal pricePerNight){this.pricePerNight = pricePerNight;}

    public Integer getRoomId() {return id;}
    public String getRoomNumber(){return this.roomNumber;}
    public String getStatus(){return this.status;}
    public BigDecimal getPricePerNight(){return this.pricePerNight;}
    public String getRoomType(){return this.roomType;}
}