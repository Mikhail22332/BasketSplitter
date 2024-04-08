import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasketSplitterTest extends TestCase {
    private BasketSplitter basketSplitter = new BasketSplitter();
    @Nested
    class Basket1 {
        private final String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        @Test
        public void testSplitOnUniqueElements() {
            List<String> items = readItemsFromJsonFile(new File("resources/basket-1.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            // Check if there are no repeated elements in the result
            assertFalse(basketSplitter.hasRepeatedElements(result));
        }

        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            List<String> items = readItemsFromJsonFile(new File("resources/basket-1.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            assertTrue(basketSplitter.validateResultCompanies(result));
        }


    }
    @Nested
    class Basket2 {
        private final String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        @Test
        public void testSplitOnUniqueElements() {
            List<String> items = readItemsFromJsonFile(new File("resources/basket-2.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            // Check if there are no repeated elements in the result
            assertFalse(basketSplitter.hasRepeatedElements(result));
        }
        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            List<String> items = readItemsFromJsonFile(new File("resources/basket-2.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            assertTrue(basketSplitter.validateResultCompanies(result));
        }

    }
    @Nested
    class Basket3 {
        private final String absolutePathToConfig = new File("resources/config1.json").getAbsolutePath();
        @Test
        public void testSplitOnUniqueElements() {
            List<String> items = readItemsFromJsonFile(new File("resources/basket-3.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            // Check if there are no repeated elements in the result
            assertFalse(basketSplitter.hasRepeatedElements(result));
        }
        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            List<String> items = readItemsFromJsonFile(new File("resources/basket-3.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> result = basketSplitter.split(items);
            assertTrue(basketSplitter.validateResultCompanies(result));
        }
    }
    @Test
    public void testCanItemBeDeliveredByCompany() {
        List<String> items = readItemsFromJsonFile(new File("resources/basket-1.json").getAbsolutePath());
        String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
        Map<String, List<String>> deliveryConfigMap = new HashMap<>();
        deliveryConfigMap.put("Item1", Arrays.asList("Company1", "Company2"));
        deliveryConfigMap.put("Item2", Arrays.asList("Company3"));

        basketSplitter.setDeliveryConfigMap(deliveryConfigMap);

        assertTrue(basketSplitter.canItemBeDeliveredByCompany("Item1", "Company1"));
        assertTrue(basketSplitter.canItemBeDeliveredByCompany("Item2", "Company3"));
        assertFalse(basketSplitter.canItemBeDeliveredByCompany("Item1", "Company3"));
        assertFalse(basketSplitter.canItemBeDeliveredByCompany("Item3", "Company1"));
    }
    @Test
    public void testFirstPhaseOfAlgoWithNullItems() {
        BasketSplitter basketSplitter = new BasketSplitter("resources/config.json");

        Map<String, List<String>> deliveryPool = basketSplitter.firstPhaseOfAlgo(null);

        assertNotNull(deliveryPool);
        assertTrue(deliveryPool.isEmpty());
    }
    @Test
    void testSecondPhaseOfAlgoHandlesNullPointerException() {
        // Create an empty deliveryPool
        Map<String, List<String>> deliveryPool = new HashMap<>();
        String minItemsCompany = "TestCompany";
        int minItems = 5;

        BasketSplitter basketSplitter = new BasketSplitter("resources/config.json");
        Map<String, List<String>> result = basketSplitter.secondPhaseOfAlgo(deliveryPool, minItemsCompany, minItems);

        assertEquals(0, result.size());
    }
    @Test
    public void testCreateDeliveryMapFromJSON_ExistingFile() {
        String jsonFilePath = "resources/config.json";
        try {
            Map<String, List<String>> deliveryMap = basketSplitter.createDeliveryMapFromJSON(jsonFilePath);
            assertNotNull(deliveryMap);
            assertTrue(deliveryMap.size() > 0);
        } catch (Exception e) {
            fail("An exception occurred while reading the existing file: " + e.getMessage());
        }
    }

    @Test
    public void testCreateDeliveryMapFromJSON_NonExistingFile() {
        String jsonFilePath = "resources/non_existing_file.json";
        assertThrows(IllegalArgumentException.class, () -> {
            basketSplitter.createDeliveryMapFromJSON(jsonFilePath);
        });
    }
    private List<String> readItemsFromJsonFile(String filePath) {
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
}