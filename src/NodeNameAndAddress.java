public class NodeNameAndAddress {
    private NodeName nodeName;
    private String address;

    public NodeNameAndAddress(NodeName nodeName, String address) {
        this.nodeName = nodeName;
        this.address = address;
    }

    public NodeName getNodeName() {
        return nodeName;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return nodeName.toString() + " " + address;
    }
}
