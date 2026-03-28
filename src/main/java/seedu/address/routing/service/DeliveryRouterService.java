package seedu.address.routing.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import seedu.address.model.delivery.Delivery;
import seedu.address.routing.client.OrsHttpClient;
import seedu.address.routing.model.Coordinate;
import seedu.address.routing.model.RouteResult;

/**
 * Orchestrates the full routing pipeline:
 *   1. Geocode all delivery addresses
 *   2. Geocode vehicle start address (depot)
 *   3. Call ORS optimization
 *
 * This is the single entry point for the UI to call.
 */
public class DeliveryRouterService {

    // Default depot address — the starting point for all vehicles.
    // In a real app this would come from user settings.
    private static final String DEFAULT_DEPOT = "3 Temasek Boulevard, Singapore 038983";

    // Default time window: 8am–6pm
    private static final int DEFAULT_EARLIEST = 8 * 3600;
    private static final int DEFAULT_LATEST = 18 * 3600;

    // Default service time per stop: 5 minutes
    private static final int DEFAULT_SERVICE_SECS = 300;

    private final GeocodingService geocodingService;
    private final OptimizationService optimizationService;

    public DeliveryRouterService() {
        OrsHttpClient client = new OrsHttpClient();
        this.geocodingService = new GeocodingService(client);
        this.optimizationService = new OptimizationService(client);
    }

    /**
     * Plans optimized routes for today's deliveries.
     *
     * @param deliveries  the full delivery list from the model
     * @param numVehicles how many vehicles/drivers are available
     * @return optimized RouteResult ready to be displayed on the map
     */
    public RouteResult planRoutes(List<Delivery> deliveries, int numVehicles) throws IOException {
        if (deliveries.isEmpty()) {
            throw new IOException("No deliveries to route.");
        }

        // Step 1: geocode depot and replicate for each vehicle
        Coordinate depot = geocodingService.geocode(DEFAULT_DEPOT);
        List<Coordinate> vehicleCoords = new ArrayList<>();
        for (int i = 0; i < numVehicles; i++) {
            vehicleCoords.add(depot);
        }

        // Step 2: geocode all delivery addresses
        List<String> addresses = new ArrayList<>();
        for (Delivery d : deliveries) {
            addresses.add(d.getAddress().value);
        }
        List<Coordinate> deliveryCoords = geocodingService.geocodeAll(addresses);

        // Step 3: build time windows + service durations
        // Using defaults — could be extended to read from Delivery model fields
        List<int[]> timeWindows = new ArrayList<>();
        List<Integer> serviceDurations = new ArrayList<>();
        for (int i = 0; i < deliveries.size(); i++) {
            timeWindows.add(new int[]{DEFAULT_EARLIEST, DEFAULT_LATEST});
            serviceDurations.add(DEFAULT_SERVICE_SECS);
        }

        // Step 4: optimize
        return optimizationService.optimize(vehicleCoords, deliveryCoords,
                timeWindows, serviceDurations);
    }
}
