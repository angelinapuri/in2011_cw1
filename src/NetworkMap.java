import java.util.*;

public class NetworkMap {
    private static Map<String, String> map;

    public NetworkMap() {
        map = new HashMap<>();
    }

    public static void addNode(String nodeName, String address) {
        map.put(nodeName, address);
    }

    public static Map<String, String> getMap() {
     return map;
 }

    public String computeNearestNodes(String hashID) {
        try {
            Map<Integer, List<Node>> distances = new TreeMap<>();

            // Compute distances to all nodes in the map
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String nodeName = entry.getKey();
                String nodeAddress = entry.getValue();
                String nodeHashID = HashID.computeHashID(nodeName + "\n");
                int distance = HashID.computeDistance(hashID, nodeHashID);

                distances.putIfAbsent(distance, new ArrayList<>());
                distances.get(distance).add(new Node(nodeName, nodeAddress));
            }

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
                        break; // Exit the loop if maximum count reached
                    }
                }

                if (count >= 1) {
                    break; // Exit the loop if at least one node added
                }
            }
            StringBuilder nodeList = new StringBuilder();
            for (Node node : closestNodes) {
                nodeList.append(node.getName()).append("\n").append(node.getAddress()).append("\n");
            }

            return "NODES " + count + "\n" + nodeList.toString();
        } catch (Exception e) {
            System.err.println("Error computing nearest nodes: " + e.getMessage());
            return "ERROR";
        }
    }
}