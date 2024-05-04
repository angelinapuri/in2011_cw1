public class NodeNameAndAddress {
    private String nodeName;
    private String nodeAddress;

    public NodeNameAndAddress(String nodeName, String nodeAddress) {
        this.nodeName = nodeName;
        this.nodeAddress = nodeAddress;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    //Converts nodeName and nodeAddress to string
    @Override
    public String toString() {
        return nodeName + "\n" + nodeAddress;

    }
}
