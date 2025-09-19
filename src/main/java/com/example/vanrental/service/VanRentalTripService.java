package com.example.vanrental.service;

import com.example.vanrental.model.VanRentalTrip;
import com.example.vanrental.repository.VanRentalTripRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VanRentalTripService {

    private final VanRentalTripRepository repository;

    public VanRentalTripService(VanRentalTripRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "trips")
    public List<VanRentalTrip> getAllTrips() {
        System.out.println("Hitting the DB");
        List<VanRentalTrip> trips = repository.findAll();

        // Sort ascending by date
        trips.sort(Comparator.comparing(VanRentalTrip::getDate));

        // For descending order:
        // trips.sort(Comparator.comparing(VanRentalTrip::getDate).reversed());

        return trips;
    }

    @CacheEvict(value = "trips", allEntries = true)
    public VanRentalTrip createTrip(VanRentalTrip trip) {
        recalc(trip);
        return repository.save(trip);
    }

    @CacheEvict(value = "trips", allEntries = true)
    public VanRentalTrip updateTrip(String id, VanRentalTrip updates) {
        Optional<VanRentalTrip> opt = repository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Trip not found: " + id);
        }
        VanRentalTrip existing = opt.get();
        if (updates.getVanNumber() != null) existing.setVanNumber(updates.getVanNumber());
        if (updates.getDate() != null) existing.setDate(updates.getDate());
        if (updates.getPickupLocation() != null) existing.setPickupLocation(updates.getPickupLocation());
        if (updates.getDropoffLocation() != null) existing.setDropoffLocation(updates.getDropoffLocation());
        if (updates.getWayment() != 0) existing.setWayment(updates.getWayment());
        if (updates.getRent() != 0) existing.setRent(updates.getRent());
        existing.setMiscSpending(updates.getMiscSpending());
        recalc(existing);
        return repository.save(existing);
    }

    @CacheEvict(value = "trips", allEntries = true)
    public void deleteTrip(String id) {
        repository.deleteById(id);
    }

    public List<VanRentalTrip> searchTrips(String query) {
        // Simple search against vanNumber, pickupLocation, dropoffLocation
        List<VanRentalTrip> trips = repository.findAll().stream()
                .filter(t -> {
                    String lower = query.toLowerCase();
                    return (t.getVanNumber() != null && t.getVanNumber().toLowerCase().contains(lower))
                            || (t.getPickupLocation() != null && t.getPickupLocation().toLowerCase().contains(lower))
                            || (t.getDropoffLocation() != null && t.getDropoffLocation().toLowerCase().contains(lower));
                })
                .toList();
        trips.sort(Comparator.comparing(VanRentalTrip::getDate));
        return trips;
    }

    public List<VanRentalTrip> searchByDateAndVan(LocalDate start, LocalDate end, String vanNumber) {
        if (vanNumber == null || vanNumber.isBlank()) {
            return repository.findByDateBetween(start, end);
        }
        List<VanRentalTrip> trips = repository.findByDateBetweenAndVanNumberContainingIgnoreCase(start, end, vanNumber);
        trips.sort(Comparator.comparing(VanRentalTrip::getDate));
        return trips;
    }

    private void recalc(VanRentalTrip trip) {
        trip.setNoOfBags((int) Math.floor((double) trip.getWayment() / 78));
        trip.setTotalRent(Math.round((trip.getNoOfBags() * trip.getRent()) + trip.getMiscSpending()));
        trip.setVanNumber(trip.getVanNumber().toUpperCase());
    }
}
