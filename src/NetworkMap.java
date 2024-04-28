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

    public String nearestResponse(String hashID) {
        try {
            List<NodeNameAndAddress> nodes = Node.getNodes();
            System.out.println(nodes);

            Map<Integer, List<NodeNameAndAddress>> distances = new TreeMap<>();

            for (NodeNameAndAddress node : nodes) {
                String nodeName = node.getNodeName();
                String nodeAddress = node.getNodeAddress();
                String nodeHashID = HashID.computeHashID(nodeName + "\n");
                int distance = HashID.computeDistance(hashID, nodeHashID);
                System.out.println(distance);

                distances.putIfAbsent(distance, new ArrayList<>());
                distances.get(distance).add(new NodeNameAndAddress(nodeName, nodeAddress));
                System.out.println(distances);
            }

            StringBuilder responseBuilder = new StringBuilder();
            int count = 0;
            for (Map.Entry<Integer, List<NodeNameAndAddress>> entry : distances.entrySet()) {
                List<NodeNameAndAddress> nearestNodes = entry.getValue();
                for (NodeNameAndAddress node : nearestNodes) {
                    responseBuilder.append(node.getNodeName()).append("\n").append(node.getNodeAddress()).append("\n");
                    count++;
                    if (count >= 3) break;
                }
                if (count >= 3) break;
            }

            return "NODES " + count + "\n" + responseBuilder.toString();

        } catch (Exception e) {
            System.err.println("Error handling NEAREST request: " + e.getMessage());
        }
        return hashID;
    }
}
