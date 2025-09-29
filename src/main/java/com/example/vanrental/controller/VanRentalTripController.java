package com.example.vanrental.controller;

import com.example.vanrental.model.PaymentRecord;
import com.example.vanrental.model.VanRentalTrip;
import com.example.vanrental.repository.PaymentRecordRepository;
import com.example.vanrental.service.VanRentalTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class VanRentalTripController {

    private final VanRentalTripService service;

    @Autowired
    private final PaymentRecordRepository paymentRecordRepository;

    public VanRentalTripController(VanRentalTripService service, PaymentRecordRepository paymentRecordRepository) {
        this.service = service;
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @GetMapping
    public List<VanRentalTrip> getAllTrips() {
        return service.getAllTrips();
    }

    @GetMapping("/getPaymentRecords")
    public List<PaymentRecord> getAllPaymentRecords() {
        return paymentRecordRepository.findAll();
    }

    @DeleteMapping("/payment/{id}")
    public ResponseEntity<Void> deletePaymentRecord(@PathVariable String id) {
        paymentRecordRepository.deleteById(Long.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getVanNumbers")
    public List<String> getVanNumberSuggestion() {
        return service.getVanNumbers();
    }

    @PostMapping
    public ResponseEntity<VanRentalTrip> createTrip(@RequestBody VanRentalTrip trip) {
        VanRentalTrip created = service.createTrip(trip);
        return ResponseEntity.created(URI.create("/api/trips/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public VanRentalTrip updateTrip(@PathVariable String id, @RequestBody VanRentalTrip updates) {
        return service.updateTrip(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable String id) {
        service.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<VanRentalTrip> searchTrips(@RequestParam String query,
                                           @RequestParam(required = false) String paymentStatus) {
        return service.searchTrips(query,paymentStatus);
    }

    @GetMapping("/filter")
    public List<VanRentalTrip> searchByDateAndVan(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String vanNumber,
            @RequestParam(required = false) String paymentStatus
    ) {
        return service.searchByDateAndVan(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                vanNumber,
                paymentStatus
        );
    }
    @Cacheable(
            value = "rates",  // cache name
            key = "T(org.springframework.util.StringUtils).trimAllWhitespace(#pickup) + '_' + T(org.springframework.util.StringUtils).trimAllWhitespace(#dropOff)"
    )
    @GetMapping("/calculateRate")
    public ResponseEntity<Integer> getRate(@RequestParam String pickup,
                                           @RequestParam String dropOff) {
        Integer rate = service.calculateRate(pickup, dropOff);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/recordPayment")
    public ResponseEntity<Integer> recordPayment(@RequestParam String startDate,
                                                 @RequestParam String endDate,
                                                 @RequestParam String vanNumber,
                                                 @RequestParam String transactionDate,
                                                 @RequestParam int amount) {
        service.recordPayment(LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                vanNumber,
                LocalDate.parse(transactionDate),
                amount);
        return ResponseEntity.noContent().build();
    }
}
