import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class NetworkMap {
    private Map<String, String> map;

    public NetworkMap() {
        this.map = new HashMap<>();
    }

    public void add(String node, String address) {
        if (this.map.size() < 3) {
            this.map.put(node, address);
        }
    }

    public void remove(String node, String address) {

        map.remove(node, address);
    }

    public void closestNodes(String node, String address) throws Exception {
        TreeMap<Integer, String> distances = new TreeMap<>();
        String hashID = HashID.computeHashID(node + "\n");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String newNodeName = entry.getKey();
            String newNodeHashID = HashID.computeHashID(newNodeName + "\n");
            int distance = HashID.computeDistance(hashID, newNodeHashID);
            distances.put(distance, newNodeName); // Store distance along with the node name
        }

        // Print the three closest nodes
        int count = 0;
        for (Map.Entry<Integer, String> entry : distances.entrySet()) {
            if (count >= 3) break; // Exit loop after finding the three closest nodes
            int distance = entry.getKey();
            String closestNode = entry.getValue();
            System.out.println("Closest node: " + closestNode + ", Distance: " + distance);
            count++;
        }
    }


    public String getAddress(String node) {
        return map.get(node);
    }

    public Map<String, String> getMap() {
        return map;
    }
}
