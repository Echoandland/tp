package seedu.address.routing.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.delivery.Delivery;
import seedu.address.model.user.User;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.routing.client.OrsHttpClient;
import seedu.address.routing.model.Coordinate;
import seedu.address.routing.model.RouteResult;

/**
 * Orchestrates the full routing pipeline:
 *   1. Geocode depot address from User's company
 *   2. Geocode all delivery addresses
 *   3. Call ORS optimization using User's vehicle profile
 *
 * If ORS is unavailable, the service falls back to a local nearest-neighbor
 * route plan so the app can still display a usable route order.
 */
public class DeliveryRouterService {

    private static final Logger logger = LogsCenter.getLogger(DeliveryRouterService.class);

    // Default service time per stop: 5 minutes
    private static final int DEFAULT_SERVICE_SECS = 300;

    private final GeocodingService geocodingService;
    private final OptimizationService optimizationService;

    /**
     * Creates instance that contains the necessary routing features.
     */
    public DeliveryRouterService() {
        OrsHttpClient client = new OrsHttpClient();
        this.geocodingService = new GeocodingService(client);
        this.optimizationService = new OptimizationService(client);
    }

    /**
     * Plans optimized routes for today's deliveries using the default sample user.
     * Convenience overload for when no user has been set up yet.
     */
    public RouteResult planRoutes(List<Delivery> deliveries) throws IOException {
        return planRoutes(deliveries, SampleDataUtil.getSampleUser());
    }

    /**
     * Plans an optimized route for today's deliveries.
     *
     * @param deliveries the full delivery list from the model
     * @param user       the logged-in user (provides depot address and vehicle profile)
     */
    public RouteResult planRoutes(List<Delivery> deliveries, User user) throws IOException {
        if (deliveries.isEmpty()) {
            throw new IOException("No deliveries to route.");
        }

        validateNoOverdueDeliveries(deliveries);

        List<String> addresses = new ArrayList<>();
        for (Delivery d : deliveries) {
            addresses.add(d.getCompany().getAddress().value);
        }

        Coordinate depot = null;
        List<Coordinate> deliveryCoords = null;

        try {
            // Step 1: geocode depot from user's company address
            depot = geocodingService.geocode(user.getDepotAddress());
            List<Coordinate> vehicleCoords = new ArrayList<>();
            vehicleCoords.add(depot);

            // Step 2: geocode all delivery addresses
            deliveryCoords = geocodingService.geocodeAll(addresses);

            // Step 3: build time windows + service durations
            List<int[]> timeWindows = buildTimeWindows(deliveries);
            List<Integer> serviceDurations = buildServiceDurations(deliveries.size());

            // Step 4: optimize using user's vehicle profile
            return optimizationService.optimize(
                    vehicleCoords,
                    deliveryCoords,
                    timeWindows,
                    serviceDurations,
                    user.getVehicleProfile()
            );
        } catch (IOException e) {
            logger.warning("ORS routing failed, falling back to local nearest-neighbor routing. Reason: "
                    + e.getMessage());
            return buildFallbackRouteResult(deliveries, user, depot, deliveryCoords);
        }
    }

    /**
     * Validates that no delivery is already overdue.
     */
    private void validateNoOverdueDeliveries(List<Delivery> deliveries) throws IOException {
        int earliest;
        int latest;
        boolean overdue = false;
        List<Delivery> overdueDeliveries = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            earliest = (int) LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            latest = (int) delivery.getDeadline().getValue()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();

            if (latest <= earliest) {
                overdue = true;
                overdueDeliveries.add(delivery);
            }
        }

        if (overdue) {
            throw new IOException("Overdue Deliveries, please update the deadline of:\n"
                    + overdueDeliveries.stream().map(x -> x.toString() + "\n").toList());
        }
    }

    /**
     * Builds ORS-compatible time windows for each delivery.
     */
    private List<int[]> buildTimeWindows(List<Delivery> deliveries) {
        List<int[]> timeWindows = new ArrayList<>();
        int earliest;

        for (Delivery delivery : deliveries) {
            earliest = (int) LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            int latest = (int) delivery.getDeadline().getValue()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            timeWindows.add(new int[]{earliest, latest});
        }

        return timeWindows;
    }

    /**
     * Builds service durations with a fixed service time per stop.
     */
    private List<Integer> buildServiceDurations(int numberOfStops) {
        List<Integer> serviceDurations = new ArrayList<>();
        for (int i = 0; i < numberOfStops; i++) {
            serviceDurations.add(DEFAULT_SERVICE_SECS);
        }
        return serviceDurations;
    }

    /**
     * Builds a local fallback route result when ORS is unavailable.
     *
     * Fallback strategy:
     * - start from depot
     * - repeatedly choose the nearest unvisited delivery
     * - reuse already-geocoded coordinates when available
     * - if a delivery cannot be geocoded, fall back to depot coordinates
     */
    private RouteResult buildFallbackRouteResult(List<Delivery> deliveries,
                                                 User user,
                                                 Coordinate depot,
                                                 List<Coordinate> deliveryCoords) {
        Coordinate depotCoord = resolveDepotCoordinate(user, depot);
        List<Coordinate> resolvedDeliveryCoords = resolveDeliveryCoordinates(deliveries, deliveryCoords, depotCoord);

        List<Integer> visitOrder = buildNearestNeighborOrder(depotCoord, resolvedDeliveryCoords);

        List<RouteResult.Stop> stops = new ArrayList<>();
        long estimatedArrival = Instant.now().getEpochSecond();
        Coordinate current = depotCoord;

        for (Integer originalIndex : visitOrder) {
            Delivery delivery = deliveries.get(originalIndex);
            Coordinate stopCoord = resolvedDeliveryCoords.get(originalIndex);

            estimatedArrival += estimateTravelSeconds(current, stopCoord);

            stops.add(new RouteResult.Stop(
                    originalIndex,
                    delivery.getCompany().getAddress().value,
                    stopCoord.lat,
                    stopCoord.lon,
                    (int) estimatedArrival,
                    formatTime(estimatedArrival)
            ));

            estimatedArrival += DEFAULT_SERVICE_SECS;
            current = stopCoord;
        }

        List<double[]> geometry = new ArrayList<>();
        List<RouteResult.VehicleRoute> routes = new ArrayList<>();
        routes.add(new RouteResult.VehicleRoute(1, stops, geometry, depotCoord.lat, depotCoord.lon));

        return new RouteResult(routes, new ArrayList<>());
    }

    /**
     * Resolves depot coordinate, preferring the already geocoded value.
     */
    private Coordinate resolveDepotCoordinate(User user, Coordinate depot) {
        if (depot != null) {
            return depot;
        }

        try {
            return geocodingService.geocode(user.getDepotAddress());
        } catch (IOException e) {
            logger.warning("Fallback route could not geocode depot; using placeholder coordinates.");
            return new Coordinate(0.0, 0.0, user.getDepotAddress());
        }
    }

    /**
     * Resolves all delivery coordinates, preferring already geocoded values.
     */
    private List<Coordinate> resolveDeliveryCoordinates(List<Delivery> deliveries,
                                                        List<Coordinate> deliveryCoords,
                                                        Coordinate depotCoord) {
        List<Coordinate> resolved = new ArrayList<>();

        for (int i = 0; i < deliveries.size(); i++) {
            if (deliveryCoords != null && i < deliveryCoords.size() && deliveryCoords.get(i) != null) {
                resolved.add(deliveryCoords.get(i));
                continue;
            }

            try {
                Coordinate coord = geocodingService.geocode(deliveries.get(i).getCompany().getAddress().value);
                resolved.add(coord);
            } catch (IOException e) {
                logger.warning("Fallback route could not geocode delivery address: "
                        + deliveries.get(i).getCompany().getAddress().value
                        + ". Using depot coordinates instead.");
                resolved.add(depotCoord);
            }
        }

        return resolved;
    }

    /**
     * Builds a visit order using the nearest-neighbor heuristic.
     */
    private List<Integer> buildNearestNeighborOrder(Coordinate depotCoord, List<Coordinate> deliveryCoords) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[deliveryCoords.size()];
        Coordinate current = depotCoord;

        for (int visitedCount = 0; visitedCount < deliveryCoords.size(); visitedCount++) {
            int nearestIndex = -1;
            double nearestDistance = Double.MAX_VALUE;

            for (int i = 0; i < deliveryCoords.size(); i++) {
                if (visited[i]) {
                    continue;
                }

                double distance = squaredDistance(current, deliveryCoords.get(i));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestIndex = i;
                }
            }

            visited[nearestIndex] = true;
            order.add(nearestIndex);
            current = deliveryCoords.get(nearestIndex);
        }

        return order;
    }

    /**
     * Uses squared Euclidean distance for comparing proximity.
     * Square root is omitted because only relative ordering matters.
     */
    private double squaredDistance(Coordinate a, Coordinate b) {
        double dLat = a.lat - b.lat;
        double dLon = a.lon - b.lon;
        return dLat * dLat + dLon * dLon;
    }

    /**
     * Produces a rough travel-time estimate for fallback display.
     * This is only used for approximate arrival times in fallback mode.
     */
    private int estimateTravelSeconds(Coordinate from, Coordinate to) {
        double distance = Math.sqrt(squaredDistance(from, to));

        // Very rough conversion for display purposes only.
        // Keeps arrival times increasing in a more realistic way than fixed increments.
        int estimated = (int) (distance * 10000);

        // Ensure a minimum travel time so nearby points do not collapse to zero.
        return Math.max(estimated, 300);
    }

    private String formatTime(long unixTimestamp) {
        LocalTime time = Instant.ofEpochSecond(unixTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}
