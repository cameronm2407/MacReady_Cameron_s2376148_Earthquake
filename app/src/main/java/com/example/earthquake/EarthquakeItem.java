// Name                 Cameron MacReady
// Student ID           s2376148
// Programme of Study   BSc Computing

package com.example.earthquake;

import java.io.Serializable;

public class EarthquakeItem implements Serializable {
    private final String location;
    private final double magnitude;
    private final String description;
    private final String pubDate;
    private final double depth;

    public EarthquakeItem(String title, String description, String pubDate) {
        this.description = description;
        this.pubDate = pubDate;

        String tempLocation = "Unknown";
        double tempMagnitude = 0.0;
        double tempDepth = 0.0;

        try {
            String[] parts = title.split(":");
            if (parts.length >= 3) {
                // Extract magnitude (e.g., "M 0.5")
                String magPart = parts[1].trim(); // "M 0.5"
                tempMagnitude = Double.parseDouble(magPart.substring(2).trim());

                // Extract location (remove any date/time accidentally appended)
                String locationWithPossibleDate = parts[2].trim();

                // Sometimes the date is appended after a comma — remove it
                if (locationWithPossibleDate.contains(",")) {
                    // Extract everything up to last comma before the actual location ends
                    String[] locationParts = locationWithPossibleDate.split(",");
                    if (locationParts.length >= 2) {
                        tempLocation = locationParts[0].trim() + ", " + locationParts[1].trim();
                    } else {
                        tempLocation = locationParts[0].trim();
                    }
                } else {
                    tempLocation = locationWithPossibleDate;
                }
            }

            // Parse depth from description
            if (description.toLowerCase().contains("depth")) {
                String[] descParts = description.split(";");
                for (String part : descParts) {
                    if (part.toLowerCase().contains("depth")) {
                        String number = part.replaceAll("[^0-9.]", "");
                        tempDepth = Double.parseDouble(number);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace(); // Good for debugging crashes in parsing
        }

        this.location = tempLocation;
        this.magnitude = tempMagnitude;
        this.depth = tempDepth;
    }

    public String getLocation() { return location; }
    public double getMagnitude() { return magnitude; }
    public String getDescription() { return description; }
    public String getPubDate() { return pubDate; }
    public double getDepth() { return depth; }
}
