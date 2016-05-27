package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import data.AST;
import dot.DotGraph;

public class Visitor {

	private DotGraph graph;
	
	private HashMap<String,HashSet<String>> use;
	private HashMap<String,HashSet<String>> def;

	private HashSet<String> useTemp;
	private HashSet<String> defTemp;

	private int nodeCounter = 0;
	private int edgeCounter = 0;
	
	public Visitor(AST ast) 
	{
		JsonObject rootPackage = (JsonObject) ((JsonArray) AnalyseAst.getAST().getTree().get("children")).get(0);
		JsonObject packageMain = (JsonObject) ((JsonArray) rootPackage.get("children")).get(0);
		JsonObject mainClass = (JsonObject) ((JsonArray) packageMain.get("children")).get(0);
		JsonObject mainFunc = (JsonObject) ((JsonArray) mainClass.get("children")).get(1);
		JsonObject mainFuncCode = (JsonObject) ((JsonArray) mainFunc.get("children")).get(1);
	
		graph = new DotGraph("ABCD");
		graph.setEdgeColor("blue");
		graph.setEdgeStyle("dotted");
		graph.setDpi(15);
		//graph = new DirectedPseudograph<String,String>(String.class);
		//dataGraph = new DirectedPseudograph<String,String>(String.class);
		
		use = new HashMap<>();
		def = new HashMap<>();

		useTemp = new HashSet<>();
		defTemp = new HashSet<>();
		
		exploreNode(mainFuncCode, null);
		
		System.out.println("DEF " + def);
		System.out.println("USE " + use);
		
		graph.generateDotFile("myGraph.dot");
		graph.outputGraph("pdf", "dot");
	}
	
	public ArrayList<String> exploreNode(JsonObject currentNode, String prevStartNode)
	{
		String startNode = prevStartNode;
		ArrayList<String> exitNodesList = new ArrayList<String>();
		
		JsonArray currentNodeContent = currentNode.get("children").asArray();
		String childStartingNode;
		int i = 0;
		
		if (currentNode.get("name").equals("Case"))
		{
			i = 1;
			JsonObject caseNode = currentNodeContent.get(0).asObject();
			childStartingNode = newNodeName() + ": Case "
					+ processGeneric((JsonObject) currentNodeContent.get(0));
			graph.pushVertex(childStartingNode);
			graph.connectDirected(startNode, childStartingNode, newEdgeName());
			exitNodesList.add(childStartingNode);

			saveDataFlow(childStartingNode);
		} else
		{
			if (startNode != null)
				exitNodesList.add(startNode);
		}

		for(; i < currentNodeContent.size(); i++){
			JsonObject newNode = (JsonObject) currentNodeContent.get(i);
			System.out.println(newNode.get("name"));
			
			final String nodeName = newNode.getString("name", null);
			
			switch(nodeName)
			{
			case "If":
				visitIf(exitNodesList, newNode);
				break;
			case "While":
				visitWhile(exitNodesList, newNode);
				break;
			case "For":
				visitFor(exitNodesList, newNode);
				break;
			case "Switch":
				visitSwitch(exitNodesList, newNode);
				break;
			case "Return":
				break;
			default:
				visitDefault(exitNodesList, newNode);
				break;
			}
		}
		
		return exitNodesList;
	}

	private void visitSwitch(final ArrayList<String> exitNodesList, final JsonObject newNode)
	{
		// process condition and create condition node
		final String condition = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(0));
		final String childStartingNode = newNodeName() + ": " + condition;

		graph.pushVertex(childStartingNode);
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for (final String node : exitNodesList)
		{
			graph.connectDirected(node, childStartingNode, newEdgeName());
		}
		
		// reset exitNodesList array
		exitNodesList.clear();
		
		for (int j = 1; j < ((JsonArray) newNode.get("children")).size(); j++)
		{
			exitNodesList.addAll(exploreNode(
					(JsonObject) ((JsonArray) newNode.get("children")).get(1),
					childStartingNode));
		}
	}

	private void visitIf(ArrayList<String> exitNodesList, JsonObject newNode)
	{
		// process condition and create condition node
		final String condition = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(0));
		final String childStartingNode = newNodeName() + ": " + condition;
		
		System.out.println(childStartingNode);
		graph.pushVertex(childStartingNode);	
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for(String node : exitNodesList){
			graph.connectDirected(node, childStartingNode, newEdgeName());
		}
		
		// reset exitNodesList array
		exitNodesList.clear();
		
		// process first block
		exitNodesList.addAll(exploreNode((JsonObject) ((JsonArray) newNode.get("children")).get(1), childStartingNode));
		// if second block exists, process
		if(((JsonArray) newNode.get("children")).size() > 2){
			// Process Else (second block)
			exitNodesList.addAll(exploreNode((JsonObject) ((JsonArray) newNode.get("children")).get(2), childStartingNode));
		}
	}

	private void visitWhile(ArrayList<String> exitNodesList, JsonObject newNode)
	{
		// process condition and create condition node
		final String condition = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(0));
		final String childStartingNode = newNodeName() + ": " + condition;
		
		System.out.println(childStartingNode);
		graph.pushVertex(childStartingNode);	
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for(String node : exitNodesList){
			graph.connectDirected(node, childStartingNode, newEdgeName());
		}
		
		// reset exitNodesList array
		exitNodesList.clear();
		
		// process code block
		exitNodesList.addAll(exploreNode((JsonObject) ((JsonArray) newNode.get("children")).get(1), childStartingNode));

		// connect condition node to previous child end nodes
		for(String node : exitNodesList){
			graph.connectDirected(node, childStartingNode, newEdgeName());
		}
		
		// the conditional node is were the loop will end and connect to the rest of the code
		exitNodesList.add(childStartingNode);
	}

	private void visitFor(ArrayList<String> exitNodesList, JsonObject newNode)
	{
		String condition;
		// process condition and create condition node
		String assignment = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(0));
		String assignmentNode = newNodeName() + ": " + assignment;
		graph.pushVertex(assignmentNode);
		
		saveDataFlow(assignmentNode);
		
		// connect condition node to previous child end nodes
		for(String node : exitNodesList){
			graph.connectDirected(node, assignmentNode, newEdgeName());
		}
		
		// reset exitNodesList array
		exitNodesList.clear();
		
		// process condition and create condition node
		condition = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(1));
		String conditionNode = newNodeName() + ": " + condition;
		graph.pushVertex(conditionNode);
		graph.connectDirected(assignmentNode, conditionNode, newEdgeName());
		
		saveDataFlow(conditionNode);
		
		// process condition and create condition node
		String statement = processGeneric((JsonObject) ((JsonArray) newNode.get("children")).get(2));
		String statementNode = newNodeName() + ": " + statement;
		graph.pushVertex(statementNode);
		graph.connectDirected(conditionNode, statementNode, newEdgeName());
		
		saveDataFlow(statementNode);
		
		// Do something for for node
		exitNodesList.addAll(exploreNode((JsonObject) ((JsonArray) newNode.get("children")).get(1), statementNode));
	}

	private void visitDefault(ArrayList<String> exitNodesList, JsonObject newNode)
	{
		// process statement and create condition node
		final String childStartingNode = newNodeName() + ": "+ processGeneric(newNode);

		System.out.println(childStartingNode);
		graph.pushVertex(childStartingNode);

		saveDataFlow(childStartingNode);

		// connect condition node to previous child end nodes
		for (final String node : exitNodesList)
		{
			graph.connectDirected(node, childStartingNode, newEdgeName());
		}

		exitNodesList.clear();
		exitNodesList.add(childStartingNode);
	}

	private void saveDataFlow(final String nodeId)
	{
		use.put(nodeId, (HashSet<String>) useTemp.clone());
		useTemp.clear();
		def.put(nodeId, (HashSet<String>) defTemp.clone());
		defTemp.clear();
	}
	
	private String processGeneric(final JsonValue node)
	{
		final String type = ((JsonObject)node).getString("name", null);
		final String content = ((JsonObject)node).getString("content", null);
		final JsonArray children = ((JsonObject)node).get("children").asArray();
		
		switch(type) {
		case "TypeReference":
			return content;
		case "VariableRead":
			return visitVariableRead(children);
		case "LocalVariable":
			return visitLocalVariable(content, children);
		case "Literal":
			return content;
		case "LocalVariableReference":
			return content;
		case "BinaryOperator":
			return visitBinaryOperator(content, children);
		case "Assignment":
			return visitAssignment(children);
		case "VariableWrite":
			return visitVariableWrite(children);
		case "OperatorAssignement":
			return visitOperatorAssignment(content, children);
		case "UnaryOperator":
			return visitUnaryOperator(content, children);
		case "Break":
			return type;
		case "Continue":
			return type;
		case "Return":
			return type;
		}
		
		return null;
	}

	private String visitUnaryOperator(final String content, final JsonArray children)
	{
		String contentEdited = content.replace("_", "");
		String variable = processGeneric((JsonObject) children.get(1));
		defTemp.add(variable);
		
		if (content.charAt(0) == '_')
		{
			return variable + contentEdited;
		}
		else
		{
			return contentEdited + variable;
		}
	}

	private String visitOperatorAssignment(final String content, final JsonArray children)
	{
		//return processGeneric(children.get(2) + " " + content + " " + processGeneric((JsonObject)children.get(1)));
		return "";
	}

	private String visitVariableWrite(final JsonArray children)
	{
		String output = processGeneric((JsonObject)children.get(1));
		
		// mark variable definition
		defTemp.add(output);
		
		return output;
	}

	private String visitAssignment(final JsonArray children)
	{
		final String leftSide = processGeneric(children.get(1));			
		final String rightSide = processGeneric(children.get(2));
		return leftSide + " = " + rightSide;
	}

	private String visitBinaryOperator(final String content, final JsonArray children)
	{
		final String rightSide = processGeneric(children.get(2));
		final String leftSide = processGeneric(children.get(1));
		return leftSide + content + rightSide;
	}

	private String visitLocalVariable(final String content, final JsonArray children)
	{
		String output = processGeneric(children.get(0)) + " " + content;
		
		if (children.size() == 2)
		{
			output += " = " + processGeneric(children.get(1));
		}
		
		defTemp.add(content);
		
		return output;
	}

	private String visitVariableRead(final JsonArray children)
	{
		String output;
		output = processGeneric((JsonObject) children.get(1));
		useTemp.add(output);		
		return output;
	}
	
	private String newNodeName()
	{
		String newNode = Integer.toString(nodeCounter);
		nodeCounter++;
		return newNode;
	}
	
	private String newEdgeName()
	{
		String newEdge = Integer.toString(edgeCounter);
		edgeCounter++;
		return newEdge;
	}
	
	public DotGraph getGraph()
	{
		return graph;
	}
}