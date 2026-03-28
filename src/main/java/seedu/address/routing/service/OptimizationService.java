package seedu.address.routing.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import seedu.address.routing.client.OrsHttpClient;
import seedu.address.routing.model.Coordinate;
import seedu.address.routing.model.RouteResult;

/**
 * Calls the ORS Optimization API (VROOM) to solve the
 * Vehicle Routing Problem with Time Windows (VRPTW).
 *
 * ORS handles the distance matrix internally — no separate matrix call needed.
 */
public class OptimizationService {

    private final OrsHttpClient client;

    public OptimizationService(OrsHttpClient client) {
        this.client = client;
    }

    /**
     * Plans optimized routes for the given vehicles and delivery stops.
     *
     * @param vehicleCoords   geocoded start locations for each vehicle (index = vehicle id)
     * @param deliveryCoords  geocoded delivery stop locations
     * @param timeWindows     parallel array to deliveryCoords: [earliestSec, latestSec] per stop
     * @param serviceDurations parallel array: service time in seconds per stop
     */
    public RouteResult optimize(
            List<Coordinate> vehicleCoords,
            List<Coordinate> deliveryCoords,
            List<int[]> timeWindows,
            List<Integer> serviceDurations) throws IOException {

        String body = buildRequest(vehicleCoords, deliveryCoords, timeWindows, serviceDurations);
        String response = client.post("/optimization", body);
        return parseResponse(response, deliveryCoords);
    }

    // ── Request builder ───────────────────────────────────────────────────────

    private String buildRequest(
            List<Coordinate> vehicleCoords,
            List<Coordinate> deliveryCoords,
            List<int[]> timeWindows,
            List<Integer> serviceDurations) {

        JSONObject root = new JSONObject();

        // vehicles
        JSONArray vehicles = new JSONArray();
        for (int i = 0; i < vehicleCoords.size(); i++) {
            Coordinate c = vehicleCoords.get(i);
            JSONObject v = new JSONObject();
            v.put("id", i + 1);
            v.put("start", new JSONArray().put(c.lon).put(c.lat));
            v.put("end", new JSONArray().put(c.lon).put(c.lat));
            // Full working day
            v.put("time_window", new JSONArray().put(new JSONArray().put(0).put(86399)));
            vehicles.put(v);
        }
        root.put("vehicles", vehicles);

        // jobs (delivery stops)
        JSONArray jobs = new JSONArray();
        for (int i = 0; i < deliveryCoords.size(); i++) {
            Coordinate c = deliveryCoords.get(i);
            JSONObject job = new JSONObject();
            job.put("id", i + 1);
            job.put("location", new JSONArray().put(c.lon).put(c.lat));

            int[] tw = timeWindows.get(i);
            job.put("time_windows", new JSONArray().put(new JSONArray().put(tw[0]).put(tw[1])));
            job.put("service", serviceDurations.get(i));
            jobs.put(job);
        }
        root.put("jobs", jobs);

        return root.toString();
    }

    // ── Response parser ───────────────────────────────────────────────────────

    private RouteResult parseResponse(String response, List<Coordinate> deliveryCoords) {
        JSONObject json = new JSONObject(response);

        List<RouteResult.VehicleRoute> routes = new ArrayList<>();
        List<Integer> unassigned = new ArrayList<>();

        // unassigned deliveries
        if (json.has("unassigned")) {
            JSONArray ua = json.getJSONArray("unassigned");
            for (int i = 0; i < ua.length(); i++) {
                unassigned.add(ua.getJSONObject(i).getInt("id") - 1); // back to 0-based
            }
        }

        // routes per vehicle
        if (json.has("routes")) {
            JSONArray jsonRoutes = json.getJSONArray("routes");
            for (int r = 0; r < jsonRoutes.length(); r++) {
                JSONObject route = jsonRoutes.getJSONObject(r);
                int vehicleId = route.getInt("vehicle");
                List<RouteResult.Stop> stops = new ArrayList<>();

                JSONArray steps = route.getJSONArray("steps");
                for (int s = 0; s < steps.length(); s++) {
                    JSONObject step = steps.getJSONObject(s);
                    if (!"job".equals(step.getString("type"))) {
                        continue; // skip depot start/end steps
                    }

                    int jobId = step.getInt("id");
                    int deliveryIdx = jobId - 1; // back to 0-based
                    int arrival = step.getInt("arrival");
                    Coordinate coord = deliveryCoords.get(deliveryIdx);

                    stops.add(new RouteResult.Stop(
                            deliveryIdx,
                            coord.originalAddress,
                            coord.lat,
                            coord.lon,
                            arrival,
                            formatTime(arrival)
                    ));
                }
                routes.add(new RouteResult.VehicleRoute(vehicleId, stops));
            }
        }

        return new RouteResult(routes, unassigned);
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 3600, (seconds % 3600) / 60);
    }
}
