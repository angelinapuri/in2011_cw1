public class Node {
    private String name;
    private String address;

    public Node(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return name + ":" + address;
    }
}
