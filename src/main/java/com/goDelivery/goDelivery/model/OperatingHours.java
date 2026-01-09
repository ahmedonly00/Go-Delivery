package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "operating_hours")
public class OperatingHours {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "monday_open")
    private String mondayOpen;
    
    @Column(name = "monday_close")
    private String mondayClose;
    
    @Column(name = "tuesday_open")
    private String tuesdayOpen;
    
    @Column(name = "tuesday_close")
    private String tuesdayClose;
    
    @Column(name = "wednesday_open")
    private String wednesdayOpen;
    
    @Column(name = "wednesday_close")
    private String wednesdayClose;
    
    @Column(name = "thursday_open")
    private String thursdayOpen;
    
    @Column(name = "thursday_close")
    private String thursdayClose;
    
    @Column(name = "friday_open")
    private String fridayOpen;
    
    @Column(name = "friday_close")
    private String fridayClose;
    
    @Column(name = "saturday_open")
    private String saturdayOpen;
    
    @Column(name = "saturday_close")
    private String saturdayClose;
    
    @Column(name = "sunday_open")
    private String sundayOpen;
    
    @Column(name = "sunday_close")
    private String sundayClose;
    
    @OneToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
    
    @OneToOne
    @JoinColumn(name = "branch_id")
    private Branches branch;
}
