import java.util.HashMap;
import java.util.Map;

public class DataStore {
    private Map<String, String> data;

    public DataStore() {

        this.data = new HashMap<>();
    }

    public void store(String key, String value) {

        data.put(key, value);
    }

    public String get(String key) {
        if (data.containsKey(key) & data!=null) {
            return data.get(key);
        } else {
            System.out.println("Not Found");
            System.out.println(data);
            return null;
        }
    }
}
