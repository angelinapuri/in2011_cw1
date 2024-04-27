import java.util.ArrayList;
import java.util.List;

public class Node {
    private static List<NodeNameAndAddress> nodes;

    static {
        nodes = new ArrayList<>();
        String[] nodeNamesAndAddresses = {
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000:10.0.0.164:20000",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20001",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20002:10.0.0.164:20002",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20003",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20004",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20005",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20006",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20007",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20008",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20009",
                "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20001:10.0.0.164:20010"
        };

        for (String nodeNameAndAddress : nodeNamesAndAddresses) {
            String[] parts = nodeNameAndAddress.split(":");
            String nodeName = parts[0];
            String address = parts[1];
            addNode(nodeName, address);
        }
    }

    private static void addNode(String nodeName, String address) {
        NodeName name = new NodeName(nodeName);
        NodeNameAndAddress nodeNameAndAddress = new NodeNameAndAddress(name, address);
        nodes.add(nodeNameAndAddress);
    }

    public static List<NodeNameAndAddress> getNodes() {
        return nodes;
    }
}