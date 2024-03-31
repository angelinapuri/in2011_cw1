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
            String nodeHashID = HashID.computeHashID(nodeName + "\n");
            int distance = HashID.computeDistance(hashID, nodeHashID);

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
                    break; // Exit the loop if maximum count reached
                }
            }

            if (count >= 1) {
                break; // Exit the loop if at least one node added
            }
        }

        // Write the closest nodes to the console
        System.out.println("NODES " + count);
        for (Node node : closestNodes) {
            System.out.println(node.getName() + "\n" + node.getAddress());
        }

        return closestNodes;
    }

 /**   public static void main(String[] args) throws Exception {
        NetworkMap networkMap = new NetworkMap();

        // Add nodes to the map
        networkMap.addNode("Node1", "Address1");
        networkMap.addNode("Node2", "Address2");
        networkMap.addNode("Node3", "Address3");
        networkMap.addNode("Node4", "Address4");

        // Find closest nodes to a given hash ID
        String hashID = "0f033be6cea034bd45a0352775a219ef5dc7825ce55d1f7dae9762d80ce64411";
        List<Node> closestNodes = networkMap.findClosestNodes(hashID);

    } */
}