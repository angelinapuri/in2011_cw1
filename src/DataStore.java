import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private static Map<String, String> keyValueMap = new ConcurrentHashMap<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        return DataStore.INSTANCE;
    }

    @SuppressWarnings("Not Used")
    private DataStore readResolve() {
        return DataStore.INSTANCE;
    }

    public void store(String key, String value) {
        keyValueMap.put(key, value);
    }

    public String get(String key) {
        return
                keyValueMap.get(key);
    }
    public void printContents() {
        System.out.println("DataStore Contents:");
        for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
