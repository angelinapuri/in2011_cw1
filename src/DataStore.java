import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();
    private final Map<String, String> keyValueMap = new ConcurrentHashMap<>(); //Using concurrent Hash Map to store data in a thread-safe manner

    private DataStore() {
    }

    public static DataStore getInstance() {
        return DataStore.INSTANCE;
    }

    //Store key-value pair
    public void store(String key, String value) {

        this.keyValueMap.put(key, value);
    }

    //Retrieve key-value pair
    public String get(String key) {
        return
                this.keyValueMap.get(key);
    }

    //Print contents of the dataStore
    public void printContents() {
        System.out.println("DataStore Contents:");
        for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
