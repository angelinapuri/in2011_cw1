import java.util.*;

public class NetworkMap {
    private static Map<String, String> map;

    public NetworkMap() {
        map = new HashMap<>();
    }

    public static void addNode(String nodeName, String address) {
        map.put(nodeName, address);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String computeNearestNodes(String hashID) {
        try {
            Map<Integer, List<Node>> distances = new TreeMap<>();

            // Compute distances to all nodes in the map
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String nodeName = entry.getKey();
                String nodeAddress = entry.getValue();
                NodeNameAndAddress nodeNameAndAddress = new NodeNameAndAddress(new NodeName(nodeName), nodeAddress);
                String nodeHashID = HashID.computeHashID(nodeNameAndAddress.toString());
                int distance = HashID.computeDistance(hashID, nodeHashID);

                distances.putIfAbsent(distance, new ArrayList<>());
                distances.get(distance).add(new Node());
            }

            List<Node> closestNodes = new ArrayList<>();
            int count = 0;

            // Iterate through distances and add closest nodes to the list
            for (Map.Entry<Integer, List<Node>> entry : distances.entrySet()) {
                List<Node> closestNodesAtDistance = entry.getValue();

                closestNodes.addAll(closestNodesAtDistance);
                count += closestNodesAtDistance.size();

                if (count >= 3) {
                    break; // Exit the loop if at least three nodes added
                }
            }
            String nearestResponse = "NODES " + count + "\n" + closestNodes;
            return nearestResponse;
        } catch (Exception e) {
            System.err.println("Error computing nearest nodes: " + e.getMessage());
            return null;
        }
    }
}