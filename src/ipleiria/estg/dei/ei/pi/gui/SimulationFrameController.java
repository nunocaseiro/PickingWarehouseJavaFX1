package ipleiria.estg.dei.ei.pi.gui;

import ipleiria.estg.dei.ei.pi.model.picking.*;
import ipleiria.estg.dei.ei.pi.utils.PickLocation;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

public class SimulationFrameController implements Initializable, EnvironmentListener {

    public AnchorPane simulationPane;
    public Group group;


    private List<Node> graphDecisionNodes;
    private List<PickingPick> graphPicks;
    private List<PickingAgent> graphAgents;
    private HashMap<Integer,Edge<Node>> graphEdges;


    private static final int NODE_SIZE = 10;
    private static final int PADDING = 25;
    private static final int PADDINGS_BOXES = 35;

    private HashMap<String, Rectangle> picks = new HashMap<>();
    private HashMap<Integer, StackPane> nodes = new HashMap<>();
    private HashMap<Integer, StackPane> agents = new HashMap<>();
    private StackPane offLoad;

    private Timeline timeline;
    public boolean stFirst = false;

    private MainFrameController main;

    public void init(MainFrameController mainFrameController){
        main= mainFrameController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        group = new Group();
    }

    public void createEdge(List<Node> nodes){
        Line l = new Line(this.nodes.get(nodes.get(0).getIdentifier()).getLayoutX()+10,this.nodes.get(nodes.get(0).getIdentifier()).getLayoutY()+10
                ,this.nodes.get(nodes.get(1).getIdentifier()).getLayoutX()+10,this.nodes.get(nodes.get(1).getIdentifier()).getLayoutY()+10);
        l.setViewOrder(1.0);
        simulationPane.getChildren().add(l);
    }

    public void createShelf(Edge e){
        Node node1 = (Node) e.getNodes().get(0);
        List<Rectangle> nodeList = new ArrayList<>();

        for (int i = 1; i < (int) e.getLength(); i++) {
            nodeList.clear();
            Rectangle rL = new Rectangle((node1.getColumn() *PADDING)-25,((node1.getLine()*PADDING)+(i*PADDING)),20,20);
            rL.setStroke(Color.BLACK);
            rL.setStrokeType(StrokeType.INSIDE);
            rL.setFill(Color.WHITE);

            Rectangle rR = new Rectangle((node1.getColumn() *PADDING)+25,(node1.getLine()*PADDING)+(i*PADDING),20,20);

            rR.setStroke(Color.BLACK);
            rR.setStrokeType(StrokeType.INSIDE);
            rR.setFill(Color.WHITE);
            rR.setId("1-1-R");

            simulationPane.getChildren().add(rL);
            simulationPane.getChildren().add(rR);
            picks.put((node1.getLine()+(i)+"-"+node1.getColumn()+"L"),rL);
            picks.put((node1.getLine()+(i)+"-"+node1.getColumn()+"R"),rR);
        }
    }

    public void createLayout(){
        for (int i = 0; i < graphDecisionNodes.size(); i++) {
            createNode(graphDecisionNodes.get(i));
        }
        for (Integer integer : graphEdges.keySet()) {

            createEdge(graphEdges.get(integer).getNodes());

            if(graphEdges.get(integer).getNodes().get(0).getColumn()==graphEdges.get(integer).getNodes().get(1).getColumn()){
                createShelf(graphEdges.get(integer));
            }
        }
        for (Node graphAgent : graphAgents) {
            createNode(graphAgent);
        }
    }

    private void createPicks() {

        for (PickingPick graphPick : graphPicks) {

            String strBuilder = graphPick.getLine()+"-"+graphPick.getColumn();
            if(graphPick.getPickLocation()== PickLocation.LEFT){
                strBuilder=strBuilder+"L";
                picks.get(strBuilder).setFill(Color.GREEN);

            }
            if(graphPick.getPickLocation()== PickLocation.RIGHT){
                strBuilder=strBuilder+"R";
                picks.get(strBuilder).setFill(Color.GREEN);
            }
        }
    }

    public void createNode(Node node){
        Text text = new Text(String.valueOf(node.getIdentifier()));
        Circle circle = new Circle();
        StackPane stackPane = new StackPane();

        if(graphDecisionNodes.contains(node)){
            circle = new Circle(NODE_SIZE, Color.WHITE);
            stackPane.setViewOrder(-1.0);
            nodes.put(node.getIdentifier(),stackPane);
        }

        if(graphAgents.contains(node)){
            circle = new Circle(NODE_SIZE, Color.RED);
            stackPane.setViewOrder(-2.0);
            agents.put(agents.size()+1,stackPane);
        }
        circle.setStroke(Color.BLACK);
        stackPane.getChildren().addAll(circle,text);
        stackPane.setLayoutY(node.getLine()*PADDING);
        stackPane.setLayoutX(node.getColumn()*PADDING);

        this.simulationPane.getChildren().add(stackPane);
    }

    public void createOffLoad(Node node){
        Text text = new Text(String.valueOf(node.getIdentifier()));
        Circle circle;
        StackPane stackPane = new StackPane();
        circle = new Circle(NODE_SIZE, Color.BLACK);
        circle.setStroke(Color.BLACK);
        stackPane.getChildren().addAll(circle,text);
        stackPane.setLayoutY(node.getLine()*PADDING);
        stackPane.setLayoutX(node.getColumn()*PADDING);
        stackPane.setViewOrder(-1.0);
        offLoad=stackPane;
        this.simulationPane.getChildren().add(stackPane);
    }

    public void start(PickingIndividual individual) {
        int max=0;
        for (PickingAgentPath path : individual.getPaths()) {
            if(max<path.getPath().size()){
                max=path.getPath().size();
            }
        }

        timeline = new Timeline();
        for (int i = 0; i < max; i++) {
            for (int i1 = 1; i1 <= individual.getPaths().size(); i1++) {
                if(i<individual.getPaths().get(i1-1).getPath().size()){
                    PathNode node = individual.getPaths().get(i1-1).getPath().get(i);
                    KeyFrame k;
                    KeyFrame k2;
                    KeyFrame k3;
                    switch (node.getPickLocation()){
                        case NONE:
                            if(nodes.containsKey(node.getIdentifier())){
                                    k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),nodes.get(node.getIdentifier()).getLayoutX()),new KeyValue(agents.get(i1).layoutYProperty(),nodes.get(node.getIdentifier()).getLayoutY()));
                            }else{
                                if(picks.containsKey(node.getLine()+"-"+node.getColumn()+"L")){
                                    k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"L").getX()+25),new KeyValue(agents.get(i1).layoutYProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"L").getY()));
                                }else{
                                    k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getX()-25),new KeyValue(agents.get(i1).layoutYProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getY()));
                                }
                            }
                            timeline.getKeyFrames().add(k);
                        break;
                        case LEFT:
                            k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"L").getX()+25),new KeyValue(agents.get(i1).layoutYProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"L").getY()));
                            k2 = new KeyFrame(Duration.millis((node.getTime()+1)*250),e->setPickEmpty(picks.get(node.getLine()+"-"+node.getColumn()+"L")));
                            timeline.getKeyFrames().add(k);
                            timeline.getKeyFrames().add(k2);
                            break;
                        case RIGHT:
                            k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getX()-25),new KeyValue(agents.get(i1).layoutYProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getY()));
                            k2 = new KeyFrame(Duration.millis((node.getTime()+1)*250),e->setPickEmpty(picks.get(node.getLine()+"-"+node.getColumn()+"R")));
                            timeline.getKeyFrames().add(k);
                            timeline.getKeyFrames().add(k2);
                            break;
                        case BOTH:
                            k = new KeyFrame(Duration.millis((node.getTime()+1)*250),new KeyValue(agents.get(i1).layoutXProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getX()-25),new KeyValue(agents.get(i1).layoutYProperty(),picks.get(node.getLine()+"-"+node.getColumn()+"R").getY()));
                            k2 = new KeyFrame(Duration.millis((node.getTime()+1)*250),e->setPickEmpty(picks.get(node.getLine()+"-"+node.getColumn()+"L")));
                            k3 = new KeyFrame(Duration.millis((node.getTime()+1)*250),e->setPickEmpty(picks.get(node.getLine()+"-"+node.getColumn()+"R")));
                            timeline.getKeyFrames().add(k);
                            timeline.getKeyFrames().add(k2);
                            timeline.getKeyFrames().add(k3);
                    }
                }
            }
        }

        timeline.play();

    }

    private void setPickEmpty(Rectangle pick){
        pick.setFill(Color.WHITE);
    }


    public void startFromSlider(Double time){
        timeline.jumpTo(Duration.millis(time));
        timeline.play();
    }


    @Override
    public void updateEnvironment() {

    }

    @Override
    public void createEnvironment(List<Node> decisionNodes, HashMap<Integer,Edge<Node>> edges, List<PickingAgent> agents, Node offLoad) {
        simulationPane.getChildren().clear();
        nodes.clear();
        picks.clear();
        this.agents.clear();
        this.offLoad=null;
        this.graphDecisionNodes= decisionNodes;
        this.graphEdges=edges;
        this.graphAgents=agents;
        createLayout();
        createOffLoad(offLoad);
    }


    @Override
    public void createSimulationPicks(List<PickingPick> pickNodes) {
        this.graphPicks= pickNodes;
        createPicks();
    }
}
