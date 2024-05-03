import java.util.*;

public class NetworkMap {
    private static Map<String, NodeNameAndAddress> map;

    static {
        map = new HashMap<>();
        String[] nodeNamesAndAddresses = {
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001 10.0.0.164:20001",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20002 10.0.0.164:20002",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20003 10.0.0.164:20003",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20004 10.0.0.164:20004",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20005 10.0.0.164:20005",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20006 10.0.0.164:20006",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20007 10.0.0.164:20007",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20008 10.0.0.164:20008",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20009 10.0.0.164:20009",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20010 10.0.0.164:20010"
        };

        for (String nodeNameAndAddress : nodeNamesAndAddresses) {
            String[] parts = nodeNameAndAddress.split(" ");
            String nodeName = parts[0];
            String nodeAddress = parts[1];
            addNode(nodeName, nodeAddress);
        }
    }

    public static void addNode(String nodeName, String nodeAddress) {
        map.put(nodeName, new NodeNameAndAddress(nodeName, nodeAddress));
    }

    public static void removeNode(String address) {
        NodeNameAndAddress nodeToRemove = null;
        for (NodeNameAndAddress node : map.values()) {
                if (node.getNodeAddress().equals(address))
                    nodeToRemove = node;
                break;
            }
        if (nodeToRemove!=null) {
            map.remove(nodeToRemove.getNodeName());
            System.out.println(nodeToRemove.getNodeName() + nodeToRemove.getNodeAddress() + "node removed from network map");
        }
    }

    public static Map<String, NodeNameAndAddress> getMap() {
        return map;
    }

    public static String getNearestNodes(String hashID) {
        try {
            Map<Integer, List<NodeNameAndAddress>> distances = new TreeMap<>();

            for (NodeNameAndAddress node : map.values()) {
                String nodeName = node.getNodeName();
                String nodeAddress = node.getNodeAddress();
                String nodeHashID = HashID.computeHashID(nodeName + "\n");
                int distance = HashID.computeDistance(hashID, nodeHashID);

                distances.putIfAbsent(distance, new ArrayList<>());
                distances.get(distance).add(new NodeNameAndAddress(nodeName, nodeAddress));
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
            return null;
        }
    }

}
