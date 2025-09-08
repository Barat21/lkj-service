package com.example.vanrental.controller;

import com.example.vanrental.model.VanRentalTrip;
import com.example.vanrental.service.VanRentalTripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class VanRentalTripController {

    private final VanRentalTripService service;

    public VanRentalTripController(VanRentalTripService service) {
        this.service = service;
    }

    @GetMapping
    public List<VanRentalTrip> getAllTrips() {
        return service.getAllTrips();
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
    public List<VanRentalTrip> searchTrips(@RequestParam String query) {
        return service.searchTrips(query);
    }

    @GetMapping("/filter")
    public List<VanRentalTrip> searchByDateAndVan(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String vanNumber
    ) {
        return service.searchByDateAndVan(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                vanNumber
        );
    }
}
