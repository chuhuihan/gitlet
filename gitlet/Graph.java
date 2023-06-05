package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Graph {

    /** The second parent, only happens if merged. */
    private Map<Node, ArrayList<Node>> adjacentNodes = new HashMap<>();
    /** The second parent, only happens if merged. */
    private Map<String, Node> nodeNames = new HashMap<>();
    /** The second parent, only happens if merged. */
    private ArrayList<Node> allNodes = new ArrayList<>();
    /** The second parent, only happens if merged. */
    private File allCommitDirectory = new File(".gitlet/allCommits/");

    Graph(LinkedList<String> allSha1) {
        for (String sha1Code : allSha1) {
            addNode(sha1Code);
        }

        for (Node node : allNodes) {
            File thisCommitFile = new File(allCommitDirectory
                    + "/" + node.getLabel() + ".txt");
            Commit thisCommit = Utils.readObject(thisCommitFile, Commit.class);
            if (thisCommit.getParent() != null
                    && thisCommit.getSecondParent() == null) {
                addConnection(thisCommit.getSha1(),
                        thisCommit.getParent().getSha1());
            } else if (thisCommit.getParent() != null
                    && thisCommit.getSecondParent() != null) {
                addConnection(thisCommit.getSha1(),
                        thisCommit.getParent().getSha1());
                addConnection(thisCommit.getSha1(),
                        thisCommit.getParent().getSha1());
            }
        }
    }

    void addNode(String label) {
        Node newNode = new Node(label);
        nodeNames.putIfAbsent(label, newNode);
        adjacentNodes.putIfAbsent(newNode, new ArrayList<>());
        allNodes.add(newNode);
    }

    void addConnection(String label1, String label2) {
        adjacentNodes.get(nodeNames.get(label1)).add(nodeNames.get(label2));
        adjacentNodes.get(nodeNames.get(label2)).add(nodeNames.get(label1));
        nodeNames.get(label1).addParent(nodeNames.get(label2));
    }

    public ArrayList<Node> getAdjNodes(String label) {
        return adjacentNodes.get(nodeNames.get(label));
    }

    public void breathFirstSearch(String label, String color) {
        LinkedList<Node> nodeLinkedList = new LinkedList<>();
        nodeLinkedList.add(nodeNames.get(label));
        HashMap<Node, Boolean> visited = new HashMap<>();
        for (int i = 0; i < allNodes.size(); i++) {
            visited.put(allNodes.get(i), false);
        }
        visited.replace(nodeNames.get(label), true);

        while (!nodeLinkedList.isEmpty()) {
            Node newNode = nodeLinkedList.pop();
            ArrayList<Node> neighbors = getAdjNodes(newNode.getLabel());

            for (Node node: neighbors) {
                if (!visited.get(node)) {
                    nodeLinkedList.add(node);
                    visited.replace(node, true);
                    if (!node.getParents().contains(newNode)) {
                        if (color.equals("red")
                                && node.getColor().equals("blue")) {
                            node.setColor(color);
                        }
                    }
                    if (color.equals("blue")) {
                        node.setColor(color);
                    }
                }
            }
        }
    }

    public ArrayList<Node> getAllNodes() {
        return allNodes;
    }

}
