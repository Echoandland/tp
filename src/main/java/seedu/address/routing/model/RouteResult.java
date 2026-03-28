package seedu.address.routing.model;

import java.util.List;

/**
 * Holds the result of a route optimization request.
 * Passed from the routing service back to the UI (MainWindow / WebView).
 */
public class RouteResult {

    public final List<VehicleRoute> routes;
    public final List<Integer> unassigned; // delivery indices that couldn't be assigned

    public RouteResult(List<VehicleRoute> routes, List<Integer> unassigned) {
        this.routes = routes;
        this.unassigned = unassigned;
    }

    /** One vehicle's ordered list of stops. */
    public static class VehicleRoute {
        public final int vehicleId;
        public final List<Stop> stops;

        public VehicleRoute(int vehicleId, List<Stop> stops) {
            this.vehicleId = vehicleId;
            this.stops = stops;
        }
    }

    /** One delivery stop within a vehicle's route. */
    public static class Stop {
        public final int deliveryIndex;   // index in the original delivery list
        public final String address;
        public final double lat;
        public final double lon;
        public final int arrivalTime;     // seconds from midnight
        public final String arrivalTimeFormatted; // e.g. "09:30"

        public Stop(int deliveryIndex, String address, double lat, double lon,
                    int arrivalTime, String arrivalTimeFormatted) {
            this.deliveryIndex = deliveryIndex;
            this.address = address;
            this.lat = lat;
            this.lon = lon;
            this.arrivalTime = arrivalTime;
            this.arrivalTimeFormatted = arrivalTimeFormatted;
        }
    }
}
