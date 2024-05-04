import java.util.*;

public class NetworkMap {
    private static final int MAX_NODES_PER_DISTANCE = 3;
    private static Map<String, NodeNameAndAddress> map = new LinkedHashMap<>();

    public static void addNode(String nodeName, String nodeAddress) {
        //Null names are excluded as they are temporary nodes
        if(!map.containsKey(nodeName) || !map.containsValue(new NodeNameAndAddress(nodeName, nodeAddress)) || nodeName == null) {
            map.put(nodeName, new NodeNameAndAddress(nodeName, nodeAddress));
            System.out.println("Node added to map " + nodeName +  " at " + nodeAddress );
        }
    }

    public static void removeNode(String nodeName, String nodeAddress) {
        //Null names are excluded as they are temporary nodes
        if(!map.containsKey(nodeName) || !map.containsValue(new NodeNameAndAddress(nodeName, nodeAddress)) || nodeName == null) {
            map.remove(nodeName, new NodeNameAndAddress(nodeName, nodeAddress));
            System.out.println("Node removed from map: " + nodeName + " at " + nodeAddress);
        }
    }

    public static Map<String, NodeNameAndAddress> getMap() {
        return map;
    }

    public static String getNearestNodes(String hashID) {
        try {
            Map<Integer, List<NodeNameAndAddress>> distances = new TreeMap<>();

            //Compute distance between the hashIDs of all the nodes in map and the hashID
            for (NodeNameAndAddress node : map.values()) {
                String nodeName = node.getNodeName();
                String nodeAddress = node.getNodeAddress();
                String nodeHashID = HashID.computeHashID(nodeName + "\n");
                int distance = HashID.computeDistance(hashID, nodeHashID);

                distances.putIfAbsent(distance, new ArrayList<>());
                distances.get(distance).add(new NodeNameAndAddress(nodeName, nodeAddress));
            }

            //Build a list of the nodes closest to the given hashID(maximum 3 nodes)
            StringBuilder responseBuilder = new StringBuilder();
            int count = 0;
            for (Map.Entry<Integer, List<NodeNameAndAddress>> entry : distances.entrySet()) {
                List<NodeNameAndAddress> nearestNodes = entry.getValue();
                for (NodeNameAndAddress node : nearestNodes) {
                    responseBuilder.append(node.getNodeName()).append("\n").append(node.getNodeAddress()).append("\n");
                    count++;
                    if (count >= MAX_NODES_PER_DISTANCE) break;
                }
                if (count >= MAX_NODES_PER_DISTANCE) break;
            }

            return "NODES " + count + "\n" + responseBuilder;

        } catch (Exception e) {
            System.err.println("Error handling NEAREST request: " + e.getMessage());
            return null;
        }
    }

}
