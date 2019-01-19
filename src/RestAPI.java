import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static spark.Spark.*;

public class RestAPI {

    public static void main(String[] args) {

        // Print the address of the API.
        System.out.println("\nhttp://localhost:4567/");

        // The root address should provide information about the API.
        String example1 = "http://localhost:4567/prices?dropoff=51.470020,-0.454295&pickup=52.167241,-0.443187";
        String example2 = "http://localhost:4567/prices?pickup=52.167241,-0.443187&dropoff=51.470020,-0.454295&passengers=5";
        String defaultResponse = "To make a query, send a GET request to http://localhost:4567/prices <br>" +
                "The required parameters <b>pickup</b> and <b>dropoff</b> should be given in the form latitude,longitude.<br>" +
                "The optional parameter <b>passengers</b> should be a positive integer.<br><br>" +
                "Example queries: <br>" +
                "<a href=" + example1 + ">" + example1 + "</a>" +
                "<br>" +
                "<a href=" + example2 + ">" + example2 + "</a>";
        get("/", (req, res) -> defaultResponse);

        // This is the page that actually handles API requests.
        get("/prices", (req, res) -> {
            // Set response type to JSON.
            res.type("application/json");
            JSONObject response = new JSONObject();

            // Extract parameters from query.
            String pickup = req.queryParams("pickup");
            String dropoff = req.queryParams("dropoff");
            String passengersParam = req.queryParams("passengers");

            // Perform error checking on the pickup and drop off parameters.
            if (pickup == null || dropoff == null) {
                response.put("error", "both 'pickup' and 'dropoff' need to be specified");
                return response;
            } else {
                try {
                    // Trigger exception if pickup / drop off parameters are in an incorrect format.
                    float pickupLatitude = Float.valueOf(pickup.split(",")[0]);
                    float pickupLongitude = Float.valueOf(pickup.split(",")[1]);
                    float dropLatitude = Float.valueOf(dropoff.split(",")[0]);
                    float dropLongitude = Float.valueOf(dropoff.split(",")[1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    response.put("error", "'pickup' and 'dropoff' should be given by latitude,longitude");
                    return response;
                }
            }

            // Perform error checking on the passengers parameter, defaulting to 1.
            int passengers = 1;
            if (passengersParam != null) {
                try {
                    passengers = Integer.parseInt( passengersParam );
                } catch( NumberFormatException e ) {
                    response.put("error", "'passengers' should be an integer");
                    return response;
                }
            }

            // If the parameters meet the requirements, use the function designed in Part 1 to fetch results.
            List<JSONObject> results = Console.searchAPIs(pickup, dropoff, passengers);

            // Setup the JSON to include information about the query.
            response.put("pickup", pickup);
            response.put("dropoff", dropoff);
            response.put("passengers", passengers);

            // Add the options to the response, or an error message if no rides were found.
            if (results == null || results.isEmpty()) {
                response.put("error", "no results found.");
            } else {
                JSONArray resultsArray = new JSONArray(results);
                response.put("options", resultsArray);
            }

            // Return the JSON.
            return response;
        });
    }
}
