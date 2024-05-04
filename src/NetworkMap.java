import java.util.*;

public class NetworkMap {
    private static final int MAX_NODES_PER_DISTANCE = 3;
    private static Map<String, NodeNameAndAddress> map = new LinkedHashMap<>();

//    static {
//        map = new HashMap<>();
//        String[] nodeNamesAndAddresses = {
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000 10.0.0.164:20000",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001 10.0.0.164:20001",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20002 10.0.0.164:20002",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20003 10.0.0.164:20003",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20004 10.0.0.164:20004",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20005 10.0.0.164:20005",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20006 10.0.0.164:20006",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20007 10.0.0.164:20007",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20008 10.0.0.164:20008",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20009 10.0.0.164:20009",
//                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20010 10.0.0.164:20010"
//        };
//
//        for (String nodeNameAndAddress : nodeNamesAndAddresses) {
//            String[] parts = nodeNameAndAddress.split(" ");
//            String nodeName = parts[0];
//            String nodeAddress = parts[1];
//            addNode(nodeName, nodeAddress);
//        }
//    }

    public static void addNode(String nodeName, String nodeAddress) {
        if(!map.containsKey(nodeName)) {
            if (nodeName == null) {
                nodeName = "Temporary Node";
            }
            map.put(nodeName, new NodeNameAndAddress(nodeName, nodeAddress));
            System.out.println("Node added to map " + nodeName +  " at " + nodeAddress );
            List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
            for (NodeNameAndAddress nodeNameAndAddress : nodes) {
                System.out.println(nodeNameAndAddress);
            }
        }
    }

    public static void removeNode(String nodeName, String nodeAddress) {
        if (!map.containsKey(nodeName)) {
            if (nodeName == null) {
                nodeName = "Temporary Node";
            }
            map.remove(nodeName, new NodeNameAndAddress(nodeName, nodeAddress));
            System.out.println("Node removed from map: " + nodeName + " at " + nodeAddress);
            List<NodeNameAndAddress> nodes = new ArrayList<>(NetworkMap.getMap().values());
            for (NodeNameAndAddress nodeNameAndAddress : nodes) {
                System.out.println(nodeNameAndAddress);
            }
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
