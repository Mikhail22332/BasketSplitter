import java.io.FileReader;
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class BasketSplitter {
    private final SplitAlgorithm splitAlgorithm = new SplitAlgorithm();
    private Map<String, List<String>> deliveryConfigMap;
    private PriorityQueue<Pair<String, Integer>> deliveryQueuePriority;

    public BasketSplitter(String absolutePathToConfigFile) {
        try {
            this.deliveryConfigMap = createDeliveryMapFromJSON(absolutePathToConfigFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Splits the given list of items into delivery pools using a heuristic algorithm.
     *
     * @param items The list of items to be split.
     * @return A map representing the delivery pools.
     */
    public Map<String, List<String>> split(List<String> items){
        fillDeliveryPriorityQueue(items);

        return splitAlgorithm.runAlgorithm(items, deliveryConfigMap, deliveryQueuePriority);
    }

    /**
     * Fills the delivery queue priority with companies and the number of items they can deliver.
     *
     * @param items The list of items to be delivered.
     */
    private void fillDeliveryPriorityQueue(List<String> items) {
        HashMap<String, Integer> deliveryOccurrenceCount = countNumberOfItemsForDelivery(items);
        try{
            PriorityQueue<Pair<String, Integer>> maxHeap = new PriorityQueue<>(Comparator.comparingInt((Pair<String, Integer> pair) -> pair.getValue()).reversed());

            for (Map.Entry<String, Integer> entry : deliveryOccurrenceCount.entrySet()) {
                maxHeap.offer(new Pair<>(entry.getKey(), entry.getValue()));
            }

            deliveryQueuePriority = maxHeap;
        }catch (NullPointerException e){
            System.out.println("Delivery config is empty");
        }
    }
    /**
     * Counts the number of items each company can deliver.
     *
     * @param items The list of items to be delivered.
     * @return A map containing the count of items each company can deliver.
     */
    private HashMap<String, Integer> countNumberOfItemsForDelivery(List<String> items) {
        try {
            HashMap<String, Integer> deliveryOccurrenceCount = new HashMap<>();

            for (String item : items) {
                List<String> companies = deliveryConfigMap.get(item);
                if (companies != null) {
                    for (String company : companies) {
                        deliveryOccurrenceCount.put(company, deliveryOccurrenceCount.getOrDefault(company, 0) + 1);
                    }
                }
            }

            return deliveryOccurrenceCount;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Creates a delivery map from a JSON file.
     *
     * @param jsonFilePath The path to the JSON file containing delivery information.
     * @return A map representing the delivery configuration.
     * @throws Exception If an error occurs while parsing the JSON file.
     */
    private Map<String, List<String>> createDeliveryMapFromJSON(String jsonFilePath) throws Exception {
        if (!Files.exists(Paths.get(jsonFilePath))) {
            throw new IllegalArgumentException("File path does not exist: " + jsonFilePath);
        }
        final JSONParser jsonParser = new JSONParser();
        final JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(jsonFilePath));

        Map<String, List<String>> deliveryMap = new HashMap<>();

        for (Object key : jsonObject.keySet()) {
            String productName = (String) key;
            List<String> deliveryMethods = (List<String>) jsonObject.get(key);
            deliveryMap.put(productName, deliveryMethods);
        }

        return deliveryMap;
    }


}
