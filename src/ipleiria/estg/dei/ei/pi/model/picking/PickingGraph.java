package ipleiria.estg.dei.ei.pi.model.picking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ipleiria.estg.dei.ei.pi.utils.EdgeDirection;
import ipleiria.estg.dei.ei.pi.utils.PickLocation;
import ipleiria.estg.dei.ei.pi.utils.exceptions.InvalidNodeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PickingGraph extends Graph<Node> {

    private List<Node> decisionNodes;
    private ArrayList<Node> agents;
    private Node offloadArea;
    private ArrayList<PickNode> picks;
    private HashMap<String, EdgeDirection> subEdges;

    public PickingGraph() {
    }

    public ArrayList<PickNode> getPicks() {
        return picks;
    }

    public int getNumberOfPicks() {
        return this.picks.size();
    }

    public ArrayList<Node> getAgents() {
        return agents;
    }

    public int getNumberOfAgents() {
        return this.agents.size();
    }

    public Node getOffloadArea() {
        return offloadArea;
    }

    public void createGraphFromFile(JsonObject jsonLayout) throws InvalidNodeException {
        createGeneralGraph(jsonLayout);
        importAgents(jsonLayout);
    }

    public void createGraphRandomPicksAndAgents(JsonObject jsonLayout) {
        createGeneralGraph(jsonLayout);
        generateRandomPicksAndAgents();
    }

    private void createGeneralGraph(JsonObject jsonObject) {
        this.successors = new HashMap<>();
        this.nodes = new HashMap<>();
        this.graphSize = 0;
        this.edges = new HashMap<>();
        this.decisionNodes = new ArrayList<>();
        this.agents = new ArrayList<>();
        this.picks = new ArrayList<>();
        this.subEdges = new HashMap<>();
//        this.subEdgesSize = 0;

        importDecisionNodes(jsonObject);
        importSuccessors(jsonObject);
        importEdges(jsonObject);
        importOffload(jsonObject);
    }

    private void importDecisionNodes(JsonObject jsonObject) {
        JsonArray jsonNodes = jsonObject.getAsJsonArray("nodes");

        JsonObject jsonNode;
        Node decisionNode;
        for (JsonElement elementNode : jsonNodes) {
            jsonNode = elementNode.getAsJsonObject();
            decisionNode = new Node(jsonNode.get("nodeNumber").getAsInt(), 0, jsonNode.get("line").getAsInt(), jsonNode.get("column").getAsInt());

            this.nodes.put(jsonNode.get("nodeNumber").getAsInt(), decisionNode);
            this.decisionNodes.add(decisionNode);
            this.graphSize++;
        }
    }

    private void importSuccessors(JsonObject jsonObject) {
        JsonArray jsonNodes = jsonObject.getAsJsonArray("nodes");

        JsonObject jsonNode;
        JsonArray jsonSuccessors;
        JsonObject jsonSuccessor;
        Node node;
        for (JsonElement elementNode : jsonNodes) {
            jsonNode = elementNode.getAsJsonObject();

            List<Node> successors = new ArrayList<>();
            this.successors.put(jsonNode.get("nodeNumber").getAsInt(), successors);

            jsonSuccessors = jsonNode.getAsJsonArray("successors");
            for (JsonElement elementSuccessor : jsonSuccessors) {
                jsonSuccessor = elementSuccessor.getAsJsonObject();

                node = this.nodes.get(jsonSuccessor.get("nodeNumber").getAsInt());
                successors.add(new Node(node.getIdentifier(), jsonSuccessor.get("distance").getAsDouble(), node.getLine(), node.getColumn()));
            }
        }
    }

    private void importEdges(JsonObject jsonObject) {
        JsonArray jsonEdges = jsonObject.getAsJsonArray("edges");

        JsonObject jsonEdge;
        Edge<Node> edge;
        Node node1;
        Node node2;
        for (JsonElement elementEdge : jsonEdges) {
            jsonEdge = elementEdge.getAsJsonObject();

            node1 = this.nodes.get(jsonEdge.get("node1Number").getAsInt());
            node2 = this.nodes.get(jsonEdge.get("node2Number").getAsInt());

//            node1.addEdge(jsonEdge.get("edgeNumber").getAsInt());
//            node2.addEdge(jsonEdge.get("edgeNumber").getAsInt());

            edge = new Edge<>(jsonEdge.get("distance").getAsDouble(), jsonEdge.get("direction").getAsInt() == 1 ? EdgeDirection.ONEWAY : EdgeDirection.TWOWAY);
            edge.addNode(node1);
            edge.addNode(node2);

//            this.edges.add(edge);
            this.edges.put(jsonEdge.get("edgeNumber").getAsInt(), edge);
            this.subEdges.put(jsonEdge.get("node1Number") +  "-" + jsonEdge.get("node2Number"), jsonEdge.get("direction").getAsInt() == 1 ? EdgeDirection.ONEWAY : EdgeDirection.TWOWAY);
//            this.subEdgesSize++;
        }

        // Collections.sort(this.edges); // TODO ?
    }

    private void importAgents(JsonObject jsonObject) throws InvalidNodeException {
        JsonArray jsonAgents = jsonObject.getAsJsonArray("agents");

        JsonObject jsonAgent;
        for (JsonElement elementAgent : jsonAgents) {
            jsonAgent = elementAgent.getAsJsonObject();

            addAgent(jsonAgent.get("edgeNumber").getAsInt(), jsonAgent.get("line").getAsInt(), jsonAgent.get("column").getAsInt()); // TODO ?
        }
    }

    private void importOffload(JsonObject jsonObject) {
        Node node = this.nodes.get(jsonObject.get("offloadArea").getAsInt());
        this.offloadArea = new Node(node.getIdentifier(), 0, node.getLine(), node.getColumn());
    }

    public void importPicks(JsonObject jsonObject) throws InvalidNodeException {
        this.picks = new ArrayList<>();

        JsonArray jsonPicks = jsonObject.getAsJsonArray("picks");

        JsonObject jsonPick;
        for (JsonElement elementNode : jsonPicks) {
            jsonPick = elementNode.getAsJsonObject();

            addPick(jsonPick.get("edgeNumber").getAsInt(), jsonPick.get("line").getAsInt(), jsonPick.get("column").getAsInt(), jsonPick.get("location").getAsInt(), jsonPick.get("weight").getAsDouble(), jsonPick.get("capacity").getAsDouble());
        }
        System.out.println(111);
    }

    private void generateRandomPicksAndAgents() {
        // TODO ?
    }

    private void addAgent(int edgeNumber, int line, int column) throws InvalidNodeException {
        this.agents.add(addNode(edgeNumber, line, column));
    }

    private void addPick(int edgeNumber, int line, int column, int location, double weight, double capacity) throws InvalidNodeException {
        Node node = addNode(edgeNumber, line, column);

        this.picks.add(new PickNode(node.getIdentifier(), 0, node.getLine(), node.getColumn(), location == -1 ? PickLocation.LEFT: PickLocation.RIGHT, weight, capacity));

    }

    private Node addNode(int edgeNumber, int line, int column) throws InvalidNodeException {
        Node node1 = null; // top or left node
        int node1DistanceToNewNode = Integer.MIN_VALUE;
        Node node2 = null; // bottom or right
        int node2DistanceToNewNode = Integer.MAX_VALUE;

        int distance;
        for (Node node : this.edges.get(edgeNumber).getNodes()) {
            distance = (node.getLine() - line) + (node.getColumn() - column);

            if (distance == 0) {
                return node;
            } else if ((distance < 0) && (distance > node1DistanceToNewNode)) {
                node1 = node;
            } else if ((distance > 0) && (distance < node2DistanceToNewNode)) {
                node2 = node;
            }
        }

        if (node1 == null || node2 == null) {
            throw new InvalidNodeException("Could not add node in edge: " + edgeNumber + " line: " + line + " column: " + column);
        }

        // CREATE NEW NODE
        this.graphSize++;
        Node newNode = new Node(this.graphSize, 0, line, column);
        this.nodes.put(this.graphSize, newNode);
        this.edges.get(edgeNumber).addNode(newNode);

        // ADD NEW NODE TO SUCCESSORS
        List<Node> successors = new ArrayList<>();
        this.successors.put(this.graphSize, successors);
        successors.add(new Node(node1.getIdentifier(), Math.abs(node1.getLine() - line) + Math.abs(node1.getColumn() - column), node1.getLine(), node1.getColumn()));
        successors.add(new Node(node2.getIdentifier(), Math.abs(node2.getLine() - line) + Math.abs(node2.getColumn() - column), node2.getLine(), node2.getColumn()));

        // ALTER NODE1 AND NODE2 SUCCESSORS
        removeNodeFromEachOtherSuccessors(node1, node2);
        this.successors.get(node1.getIdentifier()).add(new Node(this.graphSize, Math.abs(node1.getLine() - line) + Math.abs(node1.getColumn() - column), line, column));
        this.successors.get(node2.getIdentifier()).add(new Node(this.graphSize, Math.abs(node2.getLine() - line) + Math.abs(node2.getColumn() - column), line, column));

        // CREATE NEW SUB EDGES
        this.subEdges.put(node1.getIdentifier() + "-" + this.graphSize, this.edges.get(edgeNumber).getEdgeDirection());
        this.subEdges.put(node2.getIdentifier() + "-" + this.graphSize, this.edges.get(edgeNumber).getEdgeDirection());

        return newNode;
    }

    private void removeNodeFromEachOtherSuccessors(Node n1, Node n2) {
        this.successors.get(n1.getIdentifier()).removeIf(node -> node.getIdentifier() == n2.getIdentifier());
        this.successors.get(n2.getIdentifier()).removeIf(node -> node.getIdentifier() == n1.getIdentifier());
    }
}
