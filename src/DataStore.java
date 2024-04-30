import java.util.HashMap;
import java.util.Map;

public class DataStore {
    private Map<String, String> data;

    public DataStore() {
        this.data = new HashMap<>();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public void store(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }
    public void printContents() {
        System.out.println("DataStore Contents:");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}
