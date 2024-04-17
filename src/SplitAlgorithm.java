import javafx.util.Pair;
import java.util.*;

public class SplitAlgorithm {
    private enum CountType{
        MAX, MIN
    }
    /**
     * Runs all 3 phases of the algorithm to optimize the delivery pool.
     *
     * @param items                 The list of items to be delivered.
     * @param deliveryConfigMap     The structure which stores parsed JSON dictionary representing delivery configurations.
     * @param deliveryQueuePriority The priority queue representing the maximum number of items each company can deliver.
     * @return The optimized delivery pool after running the second and third phases of the algorithm.
     */
    protected Map<String, List<String>> runAlgorithm(List<String> items, Map<String, List<String>> deliveryConfigMap,  PriorityQueue<Pair<String, Integer>> deliveryQueuePriority){
        int iterationsWithoutImprovements = 0;
        List<String> tabuList = new ArrayList<>();
        // Runs first phase which populates primary delivery pool based on priority queue for given client's basket
        Map<String, List<String>> deliveryPool = firstPhaseOfAlgo(items, deliveryConfigMap, deliveryQueuePriority);

        int MAX_ITERATIONS_WITHOUT_IMPROVEMENTS = 100;
        while (iterationsWithoutImprovements <= MAX_ITERATIONS_WITHOUT_IMPROVEMENTS) {
            int sizeOfDeliveryPool = deliveryPool.size();
            // finds minimum group
            Pair<String, Integer> minDeliveryGroup = countMinGroup(deliveryPool, tabuList);
            String minItemsCompany = minDeliveryGroup.getKey();
            int minItems = minDeliveryGroup.getValue();
            // adds min group to tabu to avoid repeatable calls
            tabuList.add(minItemsCompany);
            // Runs second part of algo which aims to minimize number of groups by regrouping set of companies
            Map<String, List<String>> newPool = secondPhaseOfAlgo(deliveryConfigMap, deliveryPool, minItemsCompany, minItems);

            if (newPool.size() != sizeOfDeliveryPool) {
                deliveryPool = newPool;
                iterationsWithoutImprovements = 0;
            } else {
                iterationsWithoutImprovements++;
            }

            if (tabuList.size() == sizeOfDeliveryPool) {
                tabuList.clear();
            }
            // Run third part of algorithm which tries to rearrange
            // set of groups to have the largest group of max size
            if(iterationsWithoutImprovements == 0){
                thirdPhaseOfAlgo(deliveryPool);
                tabuList.clear();
            }
        }
        return deliveryPool;
    }
    /**
     * First phase of the algorithm populates a dictionary based on the max heap for the given basket.
     * It iterates over each item in the basket and assigns it to the company that can deliver the maximum number of items.
     * The deliveryPool dictionary contains company names as keys and lists of items as values, representing which items each company will deliver.
     *
     * @param items                     The list of items to be delivered.
     * @param deliveryConfigMap         The structure which stores parsed Json dictionary
     * @param deliveryQueuePriority     The priority queue representing the maximum number of items each company can deliver.
     * @return A dictionary mapping company names to the list of items they will deliver.
     */
    private Map<String, List<String>> firstPhaseOfAlgo(List<String> items, Map<String, List<String>> deliveryConfigMap, PriorityQueue<Pair<String, Integer>> deliveryQueuePriority) {
        Map<String, List<String>> deliveryPool = new HashMap<>();
        try {
            for (String item : items) {
                PriorityQueue<Pair<String, Integer>> copy = new PriorityQueue<>(deliveryQueuePriority);
                while (!copy.isEmpty()) {
                    String company = copy.poll().getKey();
                    if (canItemBeDeliveredByCompany(deliveryConfigMap, item, company)) {
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
     * @param deliveryConfigMap The structure which stores parsed Json dictionary
     * @param deliveryPool      The original delivery pool mapping company names to the list of items they will deliver.
     * @param minItemsCompany   The company with the smallest group of items in the delivery pool.
     * @param minItems          The number of items for the smallest group.
     * @return A modified delivery pool with potentially smaller groups of items.
     * @throws NullPointerException if no items are found for the specified company in the delivery pool.
     */
    private Map<String, List<String>> secondPhaseOfAlgo(Map<String, List<String>> deliveryConfigMap, Map<String, List<String>> deliveryPool, String minItemsCompany, int minItems) {
        try {
            // try to rearrange delivery pool to have smaller groups
            // make copy of deliveryPool
            List<String> itemsList = deliveryPool.get(minItemsCompany);
            Map<String, List<String>> deliveryPoolCopy = copyOfDeliveryPool(deliveryPool);
            int temp = minItems;
            if (itemsList != null) {
                for (String item : itemsList) {
                    temp = addItemToLargestGroups(deliveryConfigMap, item, minItemsCompany, deliveryPoolCopy, temp);
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
     * Counts the company with the maximum or minimum number of products in the delivery pool.
     *
     * @param deliveryPool        The delivery pool mapping company names to the list of items they will deliver.
     * @param tabuListOfCompanies The list of companies that should be excluded from consideration.
     * @param countType           The type of count to perform (MAX or MIN).
     * @return                    A pair containing the company name with the maximum or minimum number of products and the count of products.
     */
    private Pair<String, Integer> countGroup(Map<String, List<String>> deliveryPool, List<String> tabuListOfCompanies, CountType countType) {
        int countValue = (countType == CountType.MAX) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        String companyWithCount = null;

        for (Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {
            String company = entry.getKey();
            if (!tabuListOfCompanies.contains(company)) {
                List<String> products = entry.getValue();
                int productCount = products.size();

                if ((countType == CountType.MAX && productCount > countValue) ||
                        (countType == CountType.MIN && productCount < countValue)) {
                    countValue = productCount;
                    companyWithCount = company;
                }
            }
        }
        return new Pair<>(companyWithCount, countValue);
    }

    /**
     * Counts the company with the maximum number of products in the delivery pool.
     *
     * @param deliveryPool        The delivery pool mapping company names to the list of items they will deliver.
     * @param tabuListOfCompanies The list of companies that should be excluded from consideration.
     * @return                    A pair containing the company name with the maximum number of products and the count of products.
     */
    private Pair<String, Integer> countMaxGroup(Map<String, List<String>> deliveryPool, List<String> tabuListOfCompanies) {
        return countGroup(deliveryPool, tabuListOfCompanies, CountType.MAX);
    }

    /**
     * Counts the company with the minimum number of products in the delivery pool.
     *
     * @param deliveryPool        The delivery pool mapping company names to the list of items they will deliver.
     * @param tabuListOfCompanies The list of companies that should be excluded from consideration.
     * @return                    A pair containing the company name with the minimum number of products and the count of products.
     */
    private Pair<String, Integer> countMinGroup(Map<String, List<String>> deliveryPool, List<String> tabuListOfCompanies) {
        return countGroup(deliveryPool, tabuListOfCompanies, CountType.MIN);
    }
    /**
     * Checks if a company can deliver a specific item.
     *
     * @param deliveryConfigMap     The structure which stores parsed Json dictionary
     * @param item                  The item to be delivered.
     * @param company               The name of the company to check.
     * @return                      True if the company can deliver the item, false otherwise.
     */
    private boolean canItemBeDeliveredByCompany(Map<String, List<String>> deliveryConfigMap, String item, String company) {
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
     * Adds an item to the largest groups.
     *
     * @param deliveryConfigMap     The structure which stores parsed Json dictionary
     * @param item                  The item to add to the groups.
     * @param largestGroup          The name of the largest group in the delivery pool.
     * @param deliveryPool          The delivery pool mapping company names to the list of items they will deliver.
     * @param maxCompanySize        The size of the largest group in the delivery pool before adding the item.
     * @return                      The updated size of the largest group after adding the item.
     */
    private int addItemToLargestGroups(Map<String, List<String>> deliveryConfigMap, String item, String largestGroup, Map<String, List<String>> deliveryPool, int maxCompanySize){
        boolean canRearrange = false;
        for(Map.Entry<String, List<String>> entry : deliveryPool.entrySet()) {
            String company = entry.getKey();
            if (!company.equals(largestGroup)){

                if(canItemBeDeliveredByCompany(deliveryConfigMap, item, company)){
                    deliveryPool.get(company).add(item);
                    canRearrange = true;
                }
            }
        }
        if (canRearrange) maxCompanySize--;
        return maxCompanySize;
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
}
