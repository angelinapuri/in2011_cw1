import java.util.*;

public class NetworkMap {
    private static Map<String, String> map;

    public NetworkMap() {
        map = new HashMap<>();
    }

    public static void addNode(String nodeName, String hashID) {
        map.put(nodeName, hashID);
    }

    public List<String> getClosestNodes(String node) throws Exception {
        Map<Integer, List<String>> distances = new TreeMap<>();
        String hashID = HashID.computeHashID(node + "\n");


        // Compute distances to all nodes in the map
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String otherNodeName = entry.getKey();
            String otherNodeHashID = entry.getValue();
            int distance = HashID.computeDistance(hashID, otherNodeHashID);

            distances.putIfAbsent(distance, new ArrayList<>());
            distances.get(distance).add(otherNodeName);
        }

        // Create a list to store closest nodes
        List<String> closestNodes = new ArrayList<>();
        int count = 0;

        // Iterate through distances and add closest nodes to the list
        for (Map.Entry<Integer, List<String>> entry : distances.entrySet()) {
            List<String> closestNodesAtDistance = entry.getValue();
            Collections.shuffle(closestNodesAtDistance); // Shuffle to randomize selection

            for (String closestNode : closestNodesAtDistance) {
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