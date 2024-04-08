import java.io.FileReader;
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class BasketSplitter {
    private Map<String, List<String>> deliveryConfigMap;
    private PriorityQueue<Pair<String, Integer>> deliveryQueuePriority;

    public BasketSplitter(String absolutePathToConfigFile) {
        try {
            this.deliveryConfigMap = createDeliveryMapFromJSON(absolutePathToConfigFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public BasketSplitter(){}
    /**
     * Splits the given list of items into delivery pools using a three-phase algorithm.
     *
     * Phase One:
     * Populates the delivery pool based on the maximum heap for the given basket.
     * Each item is assigned to the company that can deliver the maximum number of items.
     *
     * Phase Two:
     * Tries to decrease the number of companies required to deliver all items from the basket.
     * It starts from the smallest groups and goes to the largest, rearranging the delivery pool as necessary.
     *
     * Phase Three:
     * Rearranges the delivery pool to maximize the number of elements in each group.
     * It iterates until there are no more maximum groups found.
     * A tabu list is used to avoid repeating actions during this phase.
     *
     * @param items The list of items to be split.
     * @return A map representing the delivery pools.
     */
    public Map<String, List<String>> split(List<String> items){
        fillDeliveryQueuePriority(items);
        //FIRST PHASE
        Map<String, List<String>> deliveryPool = firstPhaseOfAlgo(items);
        //SECOND/THIRD PHASE
        deliveryPool = runSecondAndThirdPhase(deliveryPool);
        return deliveryPool;
    }
    /**
     * First phase of the algorithm populates a dictionary based on the max heap for the given basket.
     * It iterates over each item in the basket and assigns it to the company that can deliver the maximum number of items.
     * The deliveryPool dictionary contains company names as keys and lists of items as values, representing which items each company will deliver.
     *
     * @param items The list of items to be delivered.
     * @return A dictionary mapping company names to the list of items they will deliver.
     */
    protected Map<String, List<String>> firstPhaseOfAlgo(List<String> items) {
        Map<String, List<String>> deliveryPool = new HashMap<>();
        try {
            for (String item : items) {
                PriorityQueue<Pair<String, Integer>> copy = new PriorityQueue<>(deliveryQueuePriority);
                while (!copy.isEmpty()) {
                    String company = copy.poll().getKey();
                    if (canItemBeDeliveredByCompany(item, company)) {
                        deliveryPool.computeIfAbsent(company, k -> new ArrayList<>()).add(item);
                        break;
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return deliveryPool;
    }
    /**
     * Second phase of the algorithm attempts to rearrange the delivery pool to have smaller groups, reducing the number of companies required to deliver all items from the basket.
     * It starts from the smallest groups and goes to the largest.
     *
     * @param deliveryPool     The original delivery pool mapping company names to the list of items they will deliver.
     * @param minItemsCompany  The company with the smallest group of items in the delivery pool.
     * @param minItems         The number of items for the smallest group.
     * @return A modified delivery pool with potentially smaller groups of items.
     * @throws NullPointerException if no items are found for the specified company in the delivery pool.
     */
    protected Map<String, List<String>> secondPhaseOfAlgo(Map<String, List<String>> deliveryPool, String minItemsCompany, int minItems) {
        try {
            // try to rearrange delivery pool to have smaller groups
            // make copy of deliveryPool
            List<String> itemsList = deliveryPool.get(minItemsCompany);
            Map<String, List<String>> deliveryPoolCopy = copyOfDeliveryPool(deliveryPool);
            int temp = minItems;
            if (itemsList != null) {
                for (String item : itemsList) {
                    temp = addItemToLargestGroups(item, minItemsCompany, deliveryPoolCopy, temp);
                }
                if (temp == 0) {
                    deliveryPoolCopy.remove(minItemsCompany);
                    return deliveryPoolCopy;
                }
            } else {
                throw new NullPointerException("No items found for company: " + minItemsCompany);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return deliveryPool;
    }
    /**
     * Third phase of the algorithm aims to optimize the delivery pool by redistributing products from dominant groups to other groups,
     * maximizing the number of elements in each group by rearranging groups. The tabu list helps avoid repeating actions and considering already checked Companies.
     *
     * @param deliveryPool The delivery pool mapping company names to the list of items they will deliver.
     */
    private void thirdPhaseOfAlgo(Map<String, List<String>> deliveryPool) {
        List<String> tabuList = new ArrayList<>();
        // Iterate until there are no more max groups
        while (true) {
            Pair<String, Integer> maxGroup = countMaxGroup(deliveryPool, tabuList);

            if (maxGroup.getKey() == null) {
                break;
            }
            String dominantCompanyBySize = maxGroup.getKey();
            List<String> dominantProducts = deliveryPool.get(dominantCompanyBySize);
            if (dominantProducts != null) {
                // Remove dominant group's products from all other groups
                for (Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {
                    String company = entry.getKey();
                    List<String> products = entry.getValue();
                    if (!company.equals(dominantCompanyBySize) && !tabuList.contains(company)) {
                        // Find common elements and remove them
                        products.removeAll(dominantProducts);
                    }
                }
                // Add the dominant company to the tabu list
                tabuList.add(dominantCompanyBySize);
            } else {
                throw new NullPointerException("No products found for dominant company: " + dominantCompanyBySize);
            }
        }
    }
    /**
     * Runs the second and third phases of the delivery pool optimization algorithm.
     * In the second phase, the algorithm attempts to decrease the number of companies required to deliver all items.
     * In the third phase, the algorithm maximizes the number of elements in each group by rearranging groups.
     *
     * @param deliveryPool The initial delivery pool to optimize.
     * @return The optimized delivery pool after running the second and third phases.
     */
    private Map<String, List<String>> runSecondAndThirdPhase(Map<String, List<String>> deliveryPool){
        int numberOfImprovements = 0;

        while (numberOfImprovements <= 10) {
            int sizeOfDeliveryPool = deliveryPool.size();
            List<String> tabuList = new ArrayList<>();
            while (sizeOfDeliveryPool != 0) {
                Pair<String, Integer> minDeliveryGroup = countMinGroup(deliveryPool, tabuList);
                String minItemsCompany = minDeliveryGroup.getKey();
                int minItems = minDeliveryGroup.getValue();
                tabuList.add(minItemsCompany);
                Map<String, List<String>> newPool = secondPhaseOfAlgo(deliveryPool, minItemsCompany, minItems);

                sizeOfDeliveryPool--;
                if (newPool.size() != deliveryPool.size()) {
                    numberOfImprovements = 0;
                    deliveryPool = newPool;
                    break;
                } else {
                    numberOfImprovements++;
                }
            }
            //PHASE THREE
            thirdPhaseOfAlgo(deliveryPool);
        }

        return deliveryPool;
    }
    /**
     * Counts the company with the maximum number of products in the delivery pool.
     *
     * @param deliveryPool        The delivery pool mapping company names to the list of items they will deliver.
     * @param tabuListOfCompanies The list of companies that should be excluded from consideration.
     * @return                    A pair containing the company name with the maximum number of products and the count of products.
     */
    private Pair<String, Integer> countMaxGroup(Map<String, List<String>> deliveryPool, List<String> tabuListOfCompanies){
        int maxItems = Integer.MIN_VALUE;
        String company;
        String maxItemsCompany = null;
        for(Map.Entry<String, List<String>> entry : deliveryPool.entrySet()){
            company = entry.getKey();
            if(!tabuListOfCompanies.contains(company)){
                List<String> products = entry.getValue();
                int tempCounter = products.size();
                if(tempCounter> maxItems){
                    maxItems = tempCounter;
                    maxItemsCompany = company;
                }
            }
        }
        return new Pair<>(maxItemsCompany, maxItems);
    }
    /**
     * Counts the company with the minimum number of products in the delivery pool.
     *
     * @param deliveryPool        The delivery pool mapping company names to the list of items they will deliver.
     * @param tabuListOfCompanies The list of companies that should be excluded from consideration.
     * @return                    A pair containing the company name with the minimum number of products and the count of products.
     */
    private Pair<String, Integer> countMinGroup(Map<String, List<String>> deliveryPool, List<String> tabuListOfCompanies) {
        int minItems = Integer.MAX_VALUE;
        String company;
        String minItemsCompany = null;
        for (Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {
            company = entry.getKey();
            if (!tabuListOfCompanies.contains(company)) {
                List<String> products = entry.getValue();
                int tempCounter = products.size();
                if (tempCounter < minItems) {
                    minItems = tempCounter;
                    minItemsCompany = company;
                }
            }
        }
        return new Pair<>(minItemsCompany, minItems);
    }
    /**
     * Creates a copy of the delivery pool.
     *
     * @param deliveryPool The original delivery pool mapping company names to the list of items they will deliver.
     * @return             A copy of the delivery pool.
     */
    private Map<String, List<String>> copyOfDeliveryPool(Map<String, List<String>> deliveryPool){
        Map<String, List<String>> deliveryPoolCopy = new HashMap<>();


        for (Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {

            List<String> valuesCopy = new ArrayList<>(entry.getValue());

            deliveryPoolCopy.put(entry.getKey(), valuesCopy);
        }
        return deliveryPoolCopy;
    }
    /**
     * Adds an item to the largest groups.
     *
     * @param item           The item to add to the groups.
     * @param largestGroup   The name of the largest group in the delivery pool.
     * @param deliveryPool   The delivery pool mapping company names to the list of items they will deliver.
     * @param maxCompanySize The size of the largest group in the delivery pool before adding the item.
     * @return               The updated size of the largest group after adding the item.
     */
    private int addItemToLargestGroups(String item, String largestGroup, Map<String, List<String>> deliveryPool, int maxCompanySize){
        boolean canRearrange = false;
        for(Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {
            String company = entry.getKey();
            if (!company.equals(largestGroup)){

                if(canItemBeDeliveredByCompany(item, company)){
                    deliveryPool.get(company).add(item);
                    canRearrange = true;
                }
            }
        }
        if (canRearrange) maxCompanySize--;
        return maxCompanySize;
    }
    /**
     * Checks if a company can deliver a specific item.
     *
     * @param item    The item to be delivered.
     * @param company The name of the company to check.
     * @return        True if the company can deliver the item, false otherwise.
     */
    protected boolean canItemBeDeliveredByCompany(String item, String company) {
        try {
            if (deliveryConfigMap == null) {
                throw new IllegalStateException("Delivery config is null, populate your config file.");
            }
            List<String> deliveryCompanies = deliveryConfigMap.get(item);
            if (deliveryCompanies != null) {
                for (String value : deliveryCompanies) {
                    if (company.equals(value)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Fills the delivery queue priority with companies and the number of items they can deliver.
     *
     * @param items The list of items to be delivered.
     * @throws NullPointerException If the delivery configuration map is null.
     */
    private void fillDeliveryQueuePriority(List<String> items) {
        HashMap<String, Integer> deliveryOccurrenceCount = countNumberOfItemsForDelivery(items);
        if (deliveryOccurrenceCount != null) {
            PriorityQueue<Pair<String, Integer>> maxHeap = new PriorityQueue<>(Comparator.comparingInt((Pair<String, Integer> pair) -> pair.getValue()).reversed());

            for (Map.Entry<String, Integer> entry : deliveryOccurrenceCount.entrySet()) {
                maxHeap.offer(new Pair<>(entry.getKey(), entry.getValue()));
            }

            deliveryQueuePriority = maxHeap;
        } else {
            throw new NullPointerException("Delivery config is null");
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
    protected Map<String, List<String>> createDeliveryMapFromJSON(String jsonFilePath) throws Exception {
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
    /**
     * Tries to find repeated elements in a map of lists.
     *
     * @param map The map containing lists of elements.
     * @return True if there are repeated elements, false otherwise.
     */
    protected boolean hasRepeatedElements(Map<String, List<String>> map) {
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
     * @param resultMap The result map containing companies and their associated items.
     * @return True if all items are assigned to legal companies, false otherwise.
     */
    protected boolean validateResultCompanies(Map<String, List<String>> resultMap) {
        for (Map.Entry<String, List<String>> entry : resultMap.entrySet()) {
            String company = entry.getKey();
            List<String> items = entry.getValue();
            for (String item : items) {
                if (!isItemAssignedToCompany(item, company)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether an item is assigned to a specific company based on the configuration file.
     *
     * @param item    The item to check.
     * @param company The company to check.
     * @return True if the item is assigned to the company, false otherwise.
     */
    private boolean isItemAssignedToCompany(String item, String company) {
        List<String> allowedCompanies = deliveryConfigMap.get(item);
        return allowedCompanies != null && allowedCompanies.contains(company);
    }
    public void setDeliveryConfigMap(Map<String, List<String>> deliveryConfigMap){
        this.deliveryConfigMap = deliveryConfigMap;
    }
}
