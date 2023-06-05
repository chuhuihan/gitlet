package gitlet;

import java.util.ArrayList;

public class Node {

    /** The second parent, only happens if merged. */
    private String _label;
    /** The second parent, only happens if merged. */
    private String _color;
    /** The second parent, only happens if merged. */
    private int _count;
    /** The second parent, only happens if merged. */
    private ArrayList<Node> parents = new ArrayList<>();

    Node(String label) {
        _label = label;
        _color = "white";
        _count = 0;
    }

    public String getLabel() {
        return _label;
    }

    public void setColor(String newColor) {
        _color = newColor;
    }

    public void setCount(int newCount) {
        _count = newCount;
    }

    public void addParent(Node parent) {
        parents.add(parent);
    }

    public ArrayList<Node> getParents() {
        return parents;
    }

    public int getCount() {
        return _count;
    }

    public String getColor() {
        return _color;
    }
}
