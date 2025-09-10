package com.example.vanrental.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "van_rental_trip")
public class VanRentalTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    private String vanNumber;
    private LocalDate date;
    private String pickupLocation;
    private String dropoffLocation;
    private int wayment;
    private int noOfBags;
    private double rent;
    private double miscSpending;
    private double totalRent;
}
