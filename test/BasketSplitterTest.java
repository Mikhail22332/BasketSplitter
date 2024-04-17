import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasketSplitterTest extends TestCase {
    @Nested
    class Basket1 {
        private final String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        private Map<String, List<String>> result;
        private Map<String, List<String>> correctSplit = new HashMap<>();

        @BeforeEach
        public void init() {
            List<String> items = Utils.readItemsFromJsonFile(new File("resources/basket-1.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            result = basketSplitter.split(items);
        }

        /*
         * Check if there are no repeated elements in the result
         */
        @Test
        public void testSplitOnUniqueElements() {
            assertFalse(Utils.hasRepeatedElements(result));
        }

        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> deliveryConfigMap = null;
            try{
                Field field = BasketSplitter.class.getDeclaredField("deliveryConfigMap");
                field.setAccessible(true);
                deliveryConfigMap = (Map<String, List<String>>) field.get(basketSplitter);
            } catch (Throwable e){
                System.out.println(e);
            }
            assertTrue(Utils.validateResultCompanies(deliveryConfigMap, result));
        }

        @Test
        public void testIfSplitISCorrect(){
            String path = new File("resources/answers/result-1.json").getAbsolutePath();
            correctSplit = Utils.readItemsMapFromJsonFile(path);
            assertTrue(correctSplit.equals(result));
        }
    }

    @Nested
    class Basket2 {
        private final String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        private Map<String, List<String>> result;
        private Map<String, List<String>> correctSplit = new HashMap<>();

        @BeforeEach
        public void init() {
            List<String> items = Utils.readItemsFromJsonFile(new File("resources/basket-2.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            result = basketSplitter.split(items);
        }

        /*
         * Check if there are no repeated elements in the result
         */
        @Test
        public void testSplitOnUniqueElements() {
            assertFalse(Utils.hasRepeatedElements(result));
        }

        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> deliveryConfigMap = null;
            try{
                Field field = BasketSplitter.class.getDeclaredField("deliveryConfigMap");
                field.setAccessible(true);
                deliveryConfigMap = (Map<String, List<String>>) field.get(basketSplitter);
            } catch (Throwable e){
                System.out.println(e);
            }
            assertTrue(Utils.validateResultCompanies(deliveryConfigMap, result));
        }

        @Test
        public void testIfSplitISCorrect(){
            String path = new File("resources/answers/result-2.json").getAbsolutePath();
            correctSplit = Utils.readItemsMapFromJsonFile(path);
            assertTrue(Utils.compareTwoMaps(correctSplit, result));
        }

    }
    @Nested
    class Basket3 {
        private final String absolutePathToConfig = new File("resources/config1.json").getAbsolutePath();
        private Map<String, List<String>> result;
        private Map<String, List<String>> correctSplit = new HashMap<>();

        @BeforeEach
        public void init() {
            List<String> items = Utils.readItemsFromJsonFile(new File("resources/basket-3.json").getAbsolutePath());
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            result = basketSplitter.split(items);
        }

        /*
         * Check if there are no repeated elements in the result
         */
        @Test
        public void testSplitOnUniqueElements() {
            assertFalse(Utils.hasRepeatedElements(result));
        }

        @Test
        public void testIfAllItemsHaveAllowableCompanies(){
            BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
            Map<String, List<String>> deliveryConfigMap = null;
            try{
                Field field = BasketSplitter.class.getDeclaredField("deliveryConfigMap");
                field.setAccessible(true);
                deliveryConfigMap = (Map<String, List<String>>) field.get(basketSplitter);
            } catch (Throwable e){
                System.out.println(e);
            }
            assertTrue(Utils.validateResultCompanies(deliveryConfigMap, result));
        }

        @Test
        public void testIfSplitISCorrect(){
            String path = new File("resources/answers/result-3.json").getAbsolutePath();
            correctSplit = Utils.readItemsMapFromJsonFile(path);
            System.out.println(correctSplit);
            System.out.println(result);
            assertTrue(Utils.compareTwoMaps(correctSplit, result));
        }
    }
    @Test
    public void testCanItemBeDeliveredByCompany() {
        List<String> items = Utils.readItemsFromJsonFile(new File("resources/basket-1.json").getAbsolutePath());
        String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();
        BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
        Map<String, List<String>> deliveryConfigMap = new HashMap<>();
        deliveryConfigMap.put("Item1", Arrays.asList("Company1", "Company2"));
        deliveryConfigMap.put("Item2", Arrays.asList("Company3"));

        try {
            Class cls = SplitAlgorithm.class;
            Method method = cls.getDeclaredMethod("canItemBeDeliveredByCompany", Map.class, String.class, String.class);
            method.setAccessible(true);

            SplitAlgorithm splitAlgorithmInstance = new SplitAlgorithm();
            assertTrue((boolean) method.invoke(splitAlgorithmInstance, deliveryConfigMap, "Item1", "Company1"));
            assertTrue((boolean) method.invoke(splitAlgorithmInstance, deliveryConfigMap, "Item2", "Company3"));
            assertFalse((boolean) method.invoke(splitAlgorithmInstance, deliveryConfigMap, "Item1", "Company3"));
            assertFalse((boolean) method.invoke(splitAlgorithmInstance, deliveryConfigMap, "Item3", "Company1"));
        }catch (Throwable e){
            System.out.println(e);
        }


    }
    @Test
    public void testCreateDeliveryMapFromJSON_NonExistingFile() {
        String jsonFilePath = "resources/non_existing_file.json";
        assertThrows(RuntimeException.class, () -> new BasketSplitter(jsonFilePath));
    }
    @Test
    public void testCreateDeliveryMapFromJSON_ExistingFile() {
        String jsonFilePath = "resources/config.json";
        try {
            BasketSplitter basketSplitter = new BasketSplitter(jsonFilePath);
            Field field = BasketSplitter.class.getDeclaredField("deliveryConfigMap");
            field.setAccessible(true);
            Map<String, List<String>> deliveryConfigMap = (Map<String, List<String>>) field.get(basketSplitter);
            Map<String, List<String>> correctSplit = Utils.readItemsMapFromJsonFile(jsonFilePath);

            assertTrue(deliveryConfigMap.equals(correctSplit));

        } catch (Exception e) {
            fail("An exception occurred while reading the existing file: " + e.getMessage());
        }
    }

}