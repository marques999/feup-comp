package comp.pdg;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class Node {
    private final String nodeType;
    private final String nodeCode;
    private final String nodeContent;
    private final String nodeId;
    private final Node nodeParent;
    private final Node[] nodeChildren;

    private int nodeIndex;

    public Node(final JsonObject jsonNode, final Node parentNode, int paramIndex) {
        nodeType = jsonNode.getString("name", "Unknown");
        nodeCode = jsonNode.getString("code", null);
        nodeContent = jsonNode.getString("content", null);
        nodeParent = parentNode;
        nodeIndex = paramIndex;
        nodeId = Integer.toString(((getParent() == null ? "" : getParent().getId() + "_") + nodeType + (nodeContent == null ? "" : "(" + nodeContent + ")") + "_" + nodeIndex).hashCode());

        if (jsonNode.get("children").isArray()) {
            final JsonArray nodeArray = jsonNode.get("children").asArray();

            nodeChildren = new Node[nodeArray.size()];

            for (int i = 0; i < nodeChildren.length; i++) {
                nodeChildren[i] = new Node(nodeArray.get(i).asObject(), this, i);
            }
        } else {
            nodeChildren = new Node[0];
        }

        if (nodeParent != null) {
            JavaPDG.getGraph().connectDirected("\"" + nodeParent.getId() + "\"", "\"" + getId() + "\"");
        }

        JavaPDG.getGraph().setVertexLabel("\"" + getId() + "\"", nodeType + "(" + nodeContent + ")");
    }

    protected final Node getParent() {
        return nodeParent;
    }

    public final String getCode() {
        return nodeCode;
    }

    public final String getNodeType() {
        return nodeType;
    }

    public final Node[] getChildren() {
        return nodeChildren;
    }

    public final String getId() {
        return nodeId;
    }
}