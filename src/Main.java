import java.io.File;
import java.util.List;
import java.util.Map;


public class Main {
    private static final String absolutePathToConfig = new File("resources/config.json").getAbsolutePath();

    public static void main(String[] args) {
        BasketSplitter basketSplitter = new BasketSplitter(absolutePathToConfig);
        List<String> items = Utils.readItemsFromJsonFile(new File("resources/basket-2.json").getAbsolutePath());
        Map<String, List<String>> map = basketSplitter.split(items);
        Utils.printMap(map);
    }
}