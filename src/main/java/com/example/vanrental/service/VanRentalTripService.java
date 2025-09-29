package com.example.vanrental.service;

import com.example.vanrental.model.PaymentRecord;
import com.example.vanrental.model.VanRentalTrip;
import com.example.vanrental.repository.PaymentRecordRepository;
import com.example.vanrental.repository.VanRentalTripRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VanRentalTripService {

    private final VanRentalTripRepository repository;
    private final PaymentRecordRepository paymentRecordRepository;


    public VanRentalTripService(VanRentalTripRepository repository,
                                PaymentRecordRepository paymentRecordRepository) {
        this.repository = repository;
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @Cacheable(value = "trips")
    public List<VanRentalTrip> getAllTrips() {
        System.out.println("Hitting the DB");
        List<VanRentalTrip> trips = repository.findAll();

        // Sort ascending by date
        trips.sort(Comparator.comparing(VanRentalTrip::getDate).reversed());

        // For descending order:
        // trips.sort(Comparator.comparing(VanRentalTrip::getDate).reversed());

        return trips;
    }

    @CacheEvict(value = {"trips","vanNumbers"}, allEntries = true)
    public VanRentalTrip createTrip(VanRentalTrip trip) {
        recalc(trip);
        return repository.save(trip);
    }

    @CacheEvict(value = {"trips","vanNumbers"}, allEntries = true)
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

    @CacheEvict(value = {"trips","vanNumbers"}, allEntries = true)
    public void deleteTrip(String id) {
        repository.deleteById(id);
    }

    public List<VanRentalTrip> searchTrips(String query, String paymentStatus) {
        // Simple search against vanNumber, pickupLocation, dropoffLocation
        List<VanRentalTrip> searchTrips = repository.findAll().stream()
                .filter(t -> {
                    String lower = query.toLowerCase();
                    return (t.getVanNumber() != null && t.getVanNumber().toLowerCase().contains(lower))
                            || (t.getPickupLocation() != null && t.getPickupLocation().toLowerCase().contains(lower))
                            || (t.getDropoffLocation() != null && t.getDropoffLocation().toLowerCase().contains(lower));
                }).sorted(Comparator.comparing(VanRentalTrip::getDate).reversed()).collect(Collectors.toList());

        // no filter
        // fallback: no filter if unknown
        return searchTrips.stream()
                .filter(trip -> {
                    if ("ALL".equalsIgnoreCase(paymentStatus)) {
                        return true; // no filter
                    } else if ("UNPAID".equalsIgnoreCase(paymentStatus)) {
                        return !trip.isPaid();
                    } else if ("PAID".equalsIgnoreCase(paymentStatus)) {
                        return trip.isPaid();
                    } else {
                        return true; // fallback: no filter if unknown
                    }
                })
                .collect(Collectors.toList());
    }

    public List<VanRentalTrip> searchByDateAndVan(LocalDate start, LocalDate end, String vanNumber, String paymentStatus) {
        if (vanNumber == null || vanNumber.isBlank()) {
            return repository.findByDateBetween(start, end);
        }
        List<VanRentalTrip> trips = repository.findByDateBetweenAndVanNumberContainingIgnoreCase(start, end, vanNumber);
        trips.sort(Comparator.comparing(VanRentalTrip::getDate));
        return trips.stream()
                .filter(trip -> {
                    if ("ALL".equalsIgnoreCase(paymentStatus)) {
                        return true; // no filter
                    } else if ("UNPAID".equalsIgnoreCase(paymentStatus)) {
                        return !trip.isPaid();
                    } else if ("PAID".equalsIgnoreCase(paymentStatus)) {
                        return trip.isPaid();
                    } else {
                        return true; // fallback: no filter if unknown
                    }
                })
                .collect(Collectors.toList());
    }

    private void recalc(VanRentalTrip trip) {
        trip.setNoOfBags((int) Math.floor((double) trip.getWayment() / 78));
        trip.setTotalRent(Math.round((trip.getNoOfBags() * trip.getRent()) + trip.getMiscSpending()));
        trip.setVanNumber(trip.getVanNumber().toUpperCase());
    }

    @Cacheable(value = "vanNumbers")
    public List<String> getVanNumbers() {
        System.out.println("Hitting the DB for Van numbers");
        return repository.findDistinctVanNumbers();
    }

    public Integer calculateRate(String pickup, String dropOff) {
        List<Integer> rates = repository.findRentByPickupAndDropOff(pickup,dropOff);
        return rates!= null && !rates.isEmpty() ? rates.get(0):0;

    }

    @CacheEvict(value = {"trips","vanNumbers"}, allEntries = true)
    public void recordPayment(LocalDate fromDate, LocalDate toDate, String vanNumber, LocalDate transactionDate, int amount) {
        int numberOfRecords = repository.updatePaymentRecord(fromDate,toDate,vanNumber);
        System.out.println(numberOfRecords);
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setVanNumber(vanNumber);
        paymentRecord.setFromDate(fromDate);
        paymentRecord.setToDate(toDate);
        paymentRecord.setAmount(amount);
        paymentRecordRepository.save(paymentRecord);
    }
}
