package java2pdg.analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java2pdg.Java2Pdg;
import java2pdg.dot.DotGraph;

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
		JsonObject rootPackage = (JsonObject) ((JsonArray) Java2Pdg.getAST().getTree().get("children")).get(0);
		JsonObject packageMain = (JsonObject) ((JsonArray) rootPackage.get("children")).get(0);
		JsonObject mainClass = (JsonObject) ((JsonArray) packageMain.get("children")).get(0);
		JsonObject mainFunc = (JsonObject) ((JsonArray) mainClass.get("children")).get(1);
		JsonValue mainFuncCode = ((JsonArray) mainFunc.get("children")).get(1);
	
		graph = new DotGraph("ABCD");
		graph.setEdgeColor("blue");
		graph.setEdgeStyle("dotted");
		graph.setDpi(15);

		use = new HashMap<>();
		def = new HashMap<>();
		useTemp = new HashSet<>();
		defTemp = new HashSet<>();
		
		try
		{
			exploreNode(mainFuncCode, null);
		}
		catch (final JsonValueException ex)
		{
			ex.printStackTrace();
		}
		
		System.out.println("DEF " + def);
		System.out.println("USE " + use);
		
		graph.generateDotFile("myGraph.dot");
		graph.outputGraph("pdf", "dot");
	}
	
	public ArrayList<String> exploreNode(JsonValue jsonValue, String prevStartNode) throws JsonValueException
	{
		final String startNode = prevStartNode;
		final ArrayList<String> exitNodes = new ArrayList<String>();
		final JsonObject currentNode = parseJsonObject(jsonValue);
		final JsonArray nodeChildren = parseJsonArray(currentNode.get("children"));
		int i = 0;
		
		if (currentNode.get("name").equals("Case"))
		{
			final JsonObject caseNode = nodeChildren.get(0).asObject();
			final String childStartingNode = newNodeName() + ": Case " + processGeneric((JsonObject) nodeChildren.get(0));
			
			// falta analisar case
			graph.pushVertex(childStartingNode);
			graph.connectDirected(startNode, childStartingNode, newEdgeName());
			
			exitNodes.add(childStartingNode);
			saveDataFlow(childStartingNode);
			i = 1;
		}
		else if (startNode != null)
		{
			exitNodes.add(startNode);
		}

		for (; i < nodeChildren.size(); i++)
		{
			final JsonObject newNode = (JsonObject) nodeChildren.get(i);
			final String nodeName = newNode.getString("name", null);
			
			System.out.println(nodeName);
			
			switch(nodeName)
			{
			case "If":
				visitIf(exitNodes, newNode);
				break;
			case "While":
				visitWhile(exitNodes, newNode);
				break;
			case "For":
				visitFor(exitNodes, newNode);
				break;
			case "Switch":
				visitSwitch(exitNodes, newNode);
				break;
			case "Return":
				break;
			default:
				visitDefault(exitNodes, newNode);
				break;
			}
		}
		
		return exitNodes;
	}

	private void visitSwitch(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		final JsonObject conditionNode = parseJsonObject(children.get(0));
		final String conditionString = processGeneric(conditionNode);
		final String childStartingNode = newNodeName() + ": " + conditionString;

		graph.pushVertex(childStartingNode);
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for (final String previousNode : exitNodes)
		{
			graph.connectDirected(previousNode, childStartingNode, newEdgeName());
		}

		exitNodes.clear();

		for (int j = 1; j < children.size(); j++)
		{
			exitNodes.addAll(exploreNode(children.get(j), childStartingNode));
		}
	}

	private JsonArray parseJsonArray(final JsonValue jsonValue) throws JsonValueException
	{
		if (jsonValue.isArray())
		{
			return jsonValue.asArray();
		}

		throw new JsonValueException(jsonValue, "JsonArray");
	}
	
	private JsonObject parseJsonObject(final JsonValue jsonValue) throws JsonValueException
	{
		if (jsonValue.isObject())
		{
			return jsonValue.asObject();
		}
		
		throw new JsonValueException(jsonValue, "JsonObject");
	}
	
	private void visitIf(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		final JsonObject conditionNode = parseJsonObject(children.get(0));
		final String conditionString = processGeneric(conditionNode);
		final String childStartingNode = newNodeName() + ": " + conditionString;
		
		System.out.println(childStartingNode);
		graph.pushVertex(childStartingNode);	
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for (final String previousNode : exitNodes)
		{
			graph.connectDirected(previousNode, childStartingNode, newEdgeName());
		}

		exitNodes.clear();

		// process first block (IF)
		if (children.size() > 1)
		{
			exitNodes.addAll(exploreNode(children.get(1), childStartingNode));
		}

		// process second block (ELSE), if exists
		if (children.size() > 2)
		{
			exitNodes.addAll(exploreNode(children.get(2), childStartingNode));
		}
	}

	private void visitWhile(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		// process condition and create condition node
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		final JsonObject conditionNode = parseJsonObject(children.get(0));
		final String conditionString = processGeneric(conditionNode);
		final String childStartingNode = newNodeName() + ": " + conditionString;
		
		System.out.println(childStartingNode);
		graph.pushVertex(childStartingNode);	
		saveDataFlow(childStartingNode);
		
		// connect condition node to previous child end nodes
		for (final String nodeIn : exitNodes)
		{
			graph.connectDirected(nodeIn, childStartingNode, newEdgeName());
		}
		
		exitNodes.clear();
		
		// process code block
		exitNodes.addAll(exploreNode(children.get(1), childStartingNode));

		// connect condition node to previous child end nodes
		for (final String nodeIn : exitNodes)
		{
			graph.connectDirected(nodeIn, childStartingNode, newEdgeName());
		}
		
		// the conditional node is were the loop will end and connect to the rest of the code
		exitNodes.add(childStartingNode);
	}

	private void visitFor(ArrayList<String> exitNodes, JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		final JsonObject assignmentNode = parseJsonObject(children.get(0));
		final String assignment = processGeneric(assignmentNode);
		final String assignmentString = newNodeName() + ": " + assignment;
		
		graph.pushVertex(assignmentString);
		saveDataFlow(assignmentString);
		
		// connect assignment node to previous child end nodes
		for (final String nodeIn : exitNodes)
		{
			graph.connectDirected(nodeIn, assignmentString, newEdgeName());
		}

		exitNodes.clear();
		
		// process condition node
		final String conditionNode = processGeneric(children.get(1));
		final String conditionString = newNodeName() + ": " + conditionNode;
		
		// connect condition node to previous child end nodes
		graph.pushVertex(conditionString);
		graph.connectDirected(assignmentString, conditionString, newEdgeName());
		saveDataFlow(conditionString);
		
		// process statement node
		final String statementNode = processGeneric(children.get(2));
		final String statementString = newNodeName() + ": " + statementNode;
		
		// connect statement node to previous child end nodes
		graph.pushVertex(statementString);
		graph.connectDirected(conditionString, statementString, newEdgeName());	
		saveDataFlow(statementString);
		
		// Do something for for node
		exitNodes.addAll(exploreNode(children.get(3), statementString));
	}

	private void visitDefault(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final String currentName = newNodeName();
		final String childStartingNode =  currentName + ": "+ processGeneric(currentNode);

		System.out.println(childStartingNode);
		
		graph.pushVertex(currentName);
		graph.setVertexLabel(currentName, childStartingNode);
		saveDataFlow(childStartingNode);

		for (final String nodeIn : exitNodes)
		{
			graph.connectDirected(nodeIn, currentName, newEdgeName());
		}

		exitNodes.clear();
		exitNodes.add(currentName);
	}

	private void saveDataFlow(final String nodeId)
	{
		use.put(nodeId, new HashSet<>(useTemp));
		def.put(nodeId, new HashSet<>(defTemp));
		useTemp.clear();
		defTemp.clear();
	}
	
	private String processGeneric(final JsonValue jsonValue) throws JsonValueException
	{
		final JsonObject currentNode = parseJsonObject(jsonValue);
		final String type = currentNode.getString("name", null);
		final String content = currentNode.getString("content", null);
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		
		switch (type) 
		{
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

	private String visitUnaryOperator(final String nodeContent, final JsonArray nodeChildren) throws JsonValueException
	{
		final String contentEdited = nodeContent.replace("_", "");
		final String variable = processGeneric(nodeChildren.get(1));
		
		defTemp.add(variable);
		
		if (nodeContent.charAt(0) == '_')
		{
			return variable + contentEdited;
		}
		else
		{
			return contentEdited + variable;
		}
	}

	private String visitOperatorAssignment(final String nodeContent, final JsonArray nodeChildren) throws JsonValueException
	{
		return processGeneric(nodeChildren.get(2)) + " " + nodeContent + " " + processGeneric(nodeChildren.get(1));
	}

	private String visitVariableWrite(final JsonArray children) throws JsonValueException
	{
		final String output = processGeneric(children.get(1));
		defTemp.add(output);		
		return output;
	}

	private String visitAssignment(final JsonArray children) throws JsonValueException
	{
		final String leftSide = processGeneric(children.get(1));			
		final String rightSide = processGeneric(children.get(2));
		return leftSide + " = " + rightSide;
	}

	private String visitBinaryOperator(final String content, final JsonArray children) throws JsonValueException
	{
		final String rightSide = processGeneric(children.get(2));
		final String leftSide = processGeneric(children.get(1));
		return leftSide + content + rightSide;
	}

	private String visitLocalVariable(final String variableName, final JsonArray nodeChildren) throws JsonValueException
	{
		final String leftSide = processGeneric(nodeChildren.get(0));
		
		defTemp.add(variableName);
		
		if (nodeChildren.size() < 2)
		{
			return leftSide + " " + variableName;
		}

		return leftSide + " " + variableName + " = " + processGeneric(nodeChildren.get(1));
	}

	private String visitVariableRead(final JsonArray children) throws JsonValueException
	{
		final String output = processGeneric(children.get(1));
		useTemp.add(output);		
		return output;
	}
	
	private String newNodeName()
	{
		return Integer.toString(nodeCounter++);
	}
	
	private String newEdgeName()
	{
		return Integer.toString(edgeCounter++);
	}
	
	public DotGraph getGraph()
	{
		return graph;
	}
}