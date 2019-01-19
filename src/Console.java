import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.*;

public class Console {

    /**
     * This custom comparator is required to sort the JSON objects by the value of their 'price' fields.
     */
    private static class JSONComparator implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            int valA = a.getInt("price");
            int valB = b.getInt("price");
            return Integer.compare(valA, valB);
        }
    }

    /**
     * The main function is called from the command line, and parses the arguments.
     */
    public static void main(String[] args) {
        // Set up command line arguments.
        Options options = new Options();

        // The pickup and drop off points are required..
        Option pickupArg = new Option("p", "pickup", true, "pickup point in format latitude,longitude");
        pickupArg.setRequired(true);
        options.addOption(pickupArg);

        // ..and should be specified as latitude,longitude coordinates.
        Option dropArg = new Option("d", "drop", true, "drop off point in format latitude,longitude");
        dropArg.setRequired(true);
        options.addOption(dropArg);

        // We also have a parameter for the number of passengers, so we can exclude vehicles from the results if they are too small.
        Option passengersArg = new Option("n", "passengers", true, "integer number of passengers");
        passengersArg.setRequired(false);
        options.addOption(passengersArg);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String pickup = cmd.getOptionValue("pickup");
            String drop = cmd.getOptionValue("drop");

            // Trigger exception if pickup / drop off parameters are in an incorrect format.
            float pickupLatitude = Float.valueOf(pickup.split(",")[0]);
            float pickupLongitude = Float.valueOf(pickup.split(",")[1]);
            float dropLatitude = Float.valueOf(drop.split(",")[0]);
            float dropLongitude = Float.valueOf(drop.split(",")[1]);

            // If the number of passengers isn't specified, we use a default value of 1 to return all vehicle types.
            int passengers = cmd.hasOption("passengers") ? Integer.valueOf(cmd.getOptionValue("passengers")) : 1;

            searchAPIs(pickup, drop, passengers);

        } catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Display a help message if the arguments are missing / of the wrong type.
            System.out.println("Invalid / missing parameters.");
            formatter.printHelp("bookingGo", options);
            System.exit(1);
        }
    }

    /**
     * This method iterates over each supplier, adding the results from the queryAPI method to a list.
     * It then sorts the list and removes duplicate vehicle types, keeping only the cheapest supplier for each option.
     */
    public static List<JSONObject> searchAPIs(String pickup, String drop, int passengers) {
        if (passengers > 16) {
            System.out.println("There are no vehicles large enough for " + passengers + " passengers.");
            return null;
        }

        System.out.println("Searching for options to transport " + passengers + " person(s) from " + pickup + " to " + drop);

        // Get results from each supplier.
        List<JSONObject> results = new ArrayList<>();
        results.addAll(queryAPI("Dave", pickup, drop, passengers));
        results.addAll(queryAPI("Eric", pickup, drop, passengers));
        results.addAll(queryAPI("Jeff", pickup, drop, passengers));
        System.out.println("");

        // Sort the results by price.
        results.sort(new JSONComparator());

        // Keep track of which vehicle types we have already seen.
        ArrayList<String> vehicleTypes = new ArrayList<>();

        // Initialise final list of results.
        List<JSONObject> resultsToDisplay = new ArrayList<>();

        // Add the cheapest result from each vehicle type to the final list.
        for (JSONObject result : results) {
            String resultVehicle = result.getString("car_type");
            if (!vehicleTypes.contains(resultVehicle)) {
                vehicleTypes.add(resultVehicle);
                resultsToDisplay.add(result);
            }
        }

        // Results should be displayed to the console in descending price order, so reverse the list.
        Collections.reverse(resultsToDisplay);

        // Print the results.
        for (JSONObject result : resultsToDisplay) {
            System.out.println(result.getString("car_type") + " - " + result.getString("supplier") + " - " + result.getInt("price"));
        }

        return resultsToDisplay;
    }

    /**
     * The queryAPI function returns the results from a single supplier.
     */
    private static List<JSONObject> queryAPI(String supplier, String pickup, String drop, int passengers) {
        System.out.println("Retrieving deals from '" + supplier + "'s Taxis'..");

        // Create a list of strings, enumerating the vehicle types large enough for the specified number of passengers.
        List<String> vehiclesBigEnough = new ArrayList<>();
        vehiclesBigEnough.add("MINIBUS");
        if (passengers <= 6) {
            vehiclesBigEnough.add("LUXURY_PEOPLE_CARRIER");
            vehiclesBigEnough.add("PEOPLE_CARRIER");
        }
        if (passengers <= 4) {
            vehiclesBigEnough.add("LUXURY");
            vehiclesBigEnough.add("EXECUTIVE");
            vehiclesBigEnough.add("STANDARD");
        }

        try {
            // Establish connection to the API, using the pickup and drop off parameters specified.
            URL url = new URL("https://techtest.rideways.com/" + supplier + "?pickup=" + pickup + "&dropoff=" + drop);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);

            // Read the response, and store in a StringBuilder (content).
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            // Close streams/connections.
            in.close();
            con.disconnect();

            // Convert the response to a JSON object.
            JSONObject jsonObject = new JSONObject(content.toString());

            // Extract the vehicle options from the JSON object.
            JSONArray jsonArray = jsonObject.getJSONArray("options");

            // Iterate through the results, adding each JSON object to a list if the vehicle type is large enough.
            List<JSONObject> results = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String vehicle = jsonArray.getJSONObject(i).getString("car_type");
                if (vehiclesBigEnough.contains(vehicle)) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    // Before adding the JSON object to the list, include the supplier.
                    object.put("supplier", supplier + "'s Taxis");
                    results.add(object);
                }
            }

            return results;
        } catch (IOException e) {
            // Handle API errors / timeouts gracefully.
            System.out.println("'" + supplier + "'s Taxis' is unavailable, dropped the connection, or took too long to respond.");
            return new ArrayList<>();
        }
    }
}
