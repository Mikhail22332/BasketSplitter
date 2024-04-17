import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;

public class Utils {
    /**
     * Tries to find repeated elements in a map of lists.
     *
     * @param map The map containing lists of elements.
     * @return True if there are repeated elements, false otherwise.
     */
    public static boolean hasRepeatedElements(Map<String, List<String>> map) {
        Set<String> allElements = new HashSet<>();
        Set<String> repeatedElements = new HashSet<>();

        // Iterate over the map
        for (List<String> valueList : map.values()) {
            for (String element : valueList) {
                // Check if the element has been encountered before
                if (!allElements.add(element)) {
                    // If it has, add it to the set of repeated elements
                    repeatedElements.add(element);
                }
            }
        }

        // Return true if there are repeated elements, false otherwise
        return !repeatedElements.isEmpty();
    }

    /**
     * Validates whether items in the result map are assigned to legal companies based on the configuration file.
     *
     * @param deliveryConfigMap The delivery configuration map containing items as keys and the list of allowed companies as values.
     * @param resultMap         The result map containing companies and their associated items.
     * @return True if all items are assigned to legal companies according to the configuration, false otherwise.
     */
    public static boolean validateResultCompanies(Map<String, List<String>> deliveryConfigMap, Map<String, List<String>> resultMap) {
        for (Map.Entry<String, List<String>> entry : resultMap.entrySet()) {
            String company = entry.getKey();
            List<String> items = entry.getValue();
            for (String item : items) {
                List<String> allowedCompanies = deliveryConfigMap.get(item);
                if (!isItemAssignedToCompany(allowedCompanies, company)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether an item is assigned to a specific company based on the configuration file.
     *
     * @param allowedCompanies The list of companies allowed to deliver the item.
     * @param company          The company to check.
     * @return True if the item is assigned to the company, false otherwise.
     */
    public static boolean isItemAssignedToCompany( List<String> allowedCompanies, String company) {
        return allowedCompanies != null && allowedCompanies.contains(company);
    }
    /**
     * Reads items map from a JSON file where the keys represent companies
     * and the values are lists of items.
     *
     * @param filePath The path to the JSON file.
     * @return A map where keys are company names and values are lists of items.
     */
    public static Map<String, List<String>> readItemsMapFromJsonFile(String filePath) {
        Map<String, List<String>> itemsMap = new HashMap<>();
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            for (Object key : jsonObject.keySet()) {
                String company = (String) key;
                JSONArray itemsArray = (JSONArray) jsonObject.get(company);

                List<String> items = new ArrayList<>();
                for (Object item : itemsArray) {
                    items.add((String) item);
                }

                itemsMap.put(company, items);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemsMap;
    }
    /**
     * Reads items from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @return A list of items read from the JSON file.
     */
    public static List<String> readItemsFromJsonFile(String filePath) {
        List<String> items = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONArray jsonArray = (JSONArray) obj;

            for (Object o : jsonArray) {
                String item = (String) o;
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
    /**
     * Prints the contents of a map where the keys are strings representing company names
     * and the values are lists of strings representing items associated with each company.
     * Each entry in the map is printed in the following format:
     *
     * Company: [Company Name]
     * Items:
     * - [Item 1]
     * - [Item 2]
     * ...
     *
     * @param map The map to be printed.
     */
    public static void printMap(Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String company = entry.getKey();
            List<String> items = entry.getValue();

            System.out.println("Company: " + company);
            System.out.println("Items:");
            for (String item : items) {
                System.out.println("- " + item);
            }
            System.out.println();
        }
    }
    /**
     * Compares two maps of lists for equality.
     *
     * @param first  The first map to compare.
     * @param second The second map to compare.
     * @return true if the maps are equal, false otherwise.
     */
    public static boolean compareTwoMaps(Map<String, List<String>> first, Map<String, List<String>> second) {
        if (first.size() != second.size()) {
            return false;
        }

        for (Map.Entry<String, List<String>> entry : first.entrySet()) {
            String key = entry.getKey();
            List<String> firstList = entry.getValue();
            List<String> secondList = second.get(key);
            if (secondList == null || firstList.size() != secondList.size() || !new HashSet<>(firstList).containsAll(secondList)) {
                return false;
            }
        }

        return true;
    }
}
