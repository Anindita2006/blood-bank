package com.bloodbank;

import org.springframework.stereotype.Service;
import java.util.*;

// All the business logic lives here (the controller only handles HTTP).
@Service
public class BloodService {

    // Medical rule: which donor groups a patient can safely RECEIVE from.
    // e.g. an A+ patient can take A+, A-, O+ or O- blood.
    private static final Map<String, List<String>> CAN_RECEIVE_FROM = Map.of(
        "O-",  List.of("O-"),
        "O+",  List.of("O+", "O-"),
        "A-",  List.of("A-", "O-"),
        "A+",  List.of("A+", "A-", "O+", "O-"),
        "B-",  List.of("B-", "O-"),
        "B+",  List.of("B+", "B-", "O+", "O-"),
        "AB-", List.of("AB-", "A-", "B-", "O-"),
        "AB+", List.of("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-")
    );

    // In-memory "database" with some sample data (resets when the app restarts)
    private final List<Stock> stocks = new ArrayList<>(List.of(
        new Stock("Red Cross",       "Hyderabad", "O-", 3),
        new Stock("Red Cross",       "Hyderabad", "A+", 12),
        new Stock("NIMS Blood Bank", "Hyderabad", "B+", 8),
        new Stock("NIMS Blood Bank", "Hyderabad", "O+", 20),
        new Stock("Lions Club",      "Delhi",     "AB+", 6)
    ));

    public List<String> compatibleDonors(String group) {
        List<String> donors = CAN_RECEIVE_FROM.get(group);
        if (donors == null) throw new IllegalArgumentException("Invalid blood group: " + group);
        return donors;
    }

    // Find banks in the city that have blood this patient can use
    public List<Stock> search(String city, String group) {
        List<String> donors = compatibleDonors(group);
        List<Stock> result = new ArrayList<>();
        for (Stock s : stocks) {
            if (s.getCity().equalsIgnoreCase(city) && donors.contains(s.getGroup()) && s.getUnits() > 0) {
                result.add(s);
            }
        }
        return result;
    }

    // A blood bank adds/updates its stock. Same bank + group => replace old entry.
    public Stock update(Stock s) {
        if (!CAN_RECEIVE_FROM.containsKey(s.getGroup())) throw new IllegalArgumentException("Invalid blood group: " + s.getGroup());
        if (s.getUnits() < 0) throw new IllegalArgumentException("Units cannot be negative");
        if (s.getBank() == null || s.getBank().isBlank()) throw new IllegalArgumentException("Bank name is required");

        // If this bank already has an entry for this group, remove the old one
        for (int i = 0; i < stocks.size(); i++) {
            Stock old = stocks.get(i);
            if (old.getBank().equalsIgnoreCase(s.getBank()) && old.getGroup().equals(s.getGroup())) {
                stocks.remove(i);
                break;
            }
        }
        stocks.add(s);
        return s;
    }
}
