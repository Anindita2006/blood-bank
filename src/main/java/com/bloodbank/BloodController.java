package com.bloodbank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// REST API layer: maps URLs to service methods. Everything returns JSON.
@RestController
public class BloodController {

    private final BloodService service;

    public BloodController(BloodService service) {   // Spring injects the service
        this.service = service;
    }

    // GET /api/search?city=Hyderabad&group=A+
    @GetMapping("/api/search")
    public List<Stock> search(@RequestParam String city, @RequestParam String group) {
        return service.search(city, group);
    }

    // POST /api/stock  with JSON body {"bank":..,"city":..,"group":..,"units":..}
    @PostMapping("/api/stock")
    public Stock update(@RequestBody Stock stock) {
        return service.update(stock);
    }

    // Used by Docker/Kubernetes to check the app is alive
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    // Error handler: whenever the service throws IllegalArgumentException
    // (bad blood group, negative units...), Spring calls this method instead of crashing.
    // It sends back HTTP 400 (Bad Request) + JSON like {"error": "Units cannot be negative"}
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
