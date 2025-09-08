package com.example.vanrental.repository;

import com.example.vanrental.model.VanRentalTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VanRentalTripRepository extends JpaRepository<VanRentalTrip, String> {

    List<VanRentalTrip> findByVanNumberContainingIgnoreCase(String vanNumber);

    List<VanRentalTrip> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<VanRentalTrip> findByDateBetweenAndVanNumberContainingIgnoreCase(LocalDate startDate, LocalDate endDate, String vanNumber);
}
