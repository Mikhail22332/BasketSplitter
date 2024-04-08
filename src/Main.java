import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Main {
    private static final String absolutePathToConfig = new File("resources/config1.json").getAbsolutePath();
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
    public static void main(String[] args) {
        BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
        List<String> items = readItemsFromJsonFile(new File("resources/basket-3.json").getAbsolutePath());
        Map<String, List<String>> map = basketSplitter.split(items);
        printMap(map);
    }
    private static List<String> readItemsFromJsonFile(String filePath) {
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