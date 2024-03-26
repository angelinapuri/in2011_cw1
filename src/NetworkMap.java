import java.util.HashMap;
import java.util.Map;

public class NetworkMap {
    private Map<String, String> map;

    public NetworkMap() {
        this.map = new HashMap<>();
    }

    public void add(String node, String address) {
        map.put(node, address);
    }

    public void remove(String node, String address) {
        map.remove(node, address);
    }

    public String getAddress(String node) {
        return map.get(node);
    }

    public Map<String, String> getMap() {
        return map;
    }
}
