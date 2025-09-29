package com.example.vanrental.repository;

import com.example.vanrental.model.VanRentalTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VanRentalTripRepository extends JpaRepository<VanRentalTrip, String> {

    List<VanRentalTrip> findByVanNumberContainingIgnoreCase(String vanNumber);

    List<VanRentalTrip> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<VanRentalTrip> findByDateBetweenAndVanNumberContainingIgnoreCase(LocalDate startDate, LocalDate endDate, String vanNumber);

    @Query("SELECT DISTINCT v.vanNumber FROM VanRentalTrip v")
    List<String> findDistinctVanNumbers();

    @Query("SELECT v.rent FROM VanRentalTrip v " +
            "WHERE TRIM(v.pickupLocation) = TRIM(:pickup) " +
            "AND TRIM(v.dropoffLocation) = TRIM(:dropOff) " +
            "ORDER BY v.date DESC")
    List<Integer> findRentByPickupAndDropOff(@Param("pickup") String pickup,
                                             @Param("dropOff") String dropOff);

    @Modifying
    @Transactional
    @Query("UPDATE VanRentalTrip v SET v.paid = true " +
            "WHERE v.date >= :startDate AND v.date <= :endDate " +
            "AND v.vanNumber = :vanNumber")
    int  updatePaymentRecord(@Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("vanNumber") String vanNumber);
}
