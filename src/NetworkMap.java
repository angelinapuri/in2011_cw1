import java.util.*;

public class NetworkMap {
    private static Map<String, String> map;

    public NetworkMap() {
        map = new HashMap<>();
    }

    public static void addNode(String nodeName, String address) {
        map.put(nodeName, address);
    }

    public static List<Node> findClosestNodes(String hashID) throws Exception {
        Map<Integer, List<Node>> distances = new TreeMap<>();

        // Compute distances to all nodes in the map
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String nodeName = entry.getKey();
            String nodeAddress = entry.getValue();
            int distance = HashID.computeDistance(hashID, nodeAddress);

            distances.putIfAbsent(distance, new ArrayList<>());
            distances.get(distance).add(new Node(nodeName, nodeAddress));
        }

        // Create a list to store closest nodes
        List<Node> closestNodes = new ArrayList<>();
        int count = 0;

        // Iterate through distances and add closest nodes to the list
        for (Map.Entry<Integer, List<Node>> entry : distances.entrySet()) {
            List<Node> closestNodesAtDistance = entry.getValue();
            Collections.shuffle(closestNodesAtDistance); // Shuffle to randomize selection

            for (Node closestNode : closestNodesAtDistance) {
                closestNodes.add(closestNode);
                count++;

                if (count >= 3) {
                    return closestNodes; // Return when 3 closest nodes are found
                }
            }
        }

        return closestNodes;
    }
}