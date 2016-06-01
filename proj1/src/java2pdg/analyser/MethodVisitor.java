package java2pdg.analyser;

import java.lang.reflect.Array;
import java.util.*;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class MethodVisitor extends JavaVisitor
{
	private final ArrayList<String> argumentList;
	private final ArrayList<String> parameterNodes = new ArrayList<String>();
	private final ArrayList<String> breakNodes = new ArrayList<String>();
	private final ArrayList<String> continueNodes = new ArrayList<String>();
	private final ArrayList<String> returnNodes = new ArrayList<String>();
	private final String objectName;
	
	private boolean returnMissing;
	
	public MethodVisitor(final JsonObject currentObject, final JsonArray objectContent, ArrayList<String> exitNodes) throws JsonValueException
	{
		int blockPosition = 1;
		
		for (int i = blockPosition; i < objectContent.size(); i++)
		{
			final JsonObject currentParameter = parseJsonObject(objectContent.get(i));
			
			switch (parseJsonString(currentParameter.get("name")))
			{
			case "Parameter":
				parameterNodes.add(parseJsonString(currentParameter.get("content")));
				break;
			case "Block":
				blockPosition = i;
				break;
			}
		}
		
		String methodName = parseJsonString(currentObject.get("content"));
		
		final String nodeIdentifier = "\"method_" + methodName + "\"";
		
		pushMethod(nodeIdentifier, "function: " + methodName);
		returnMissing = false;

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, nodeIdentifier);
		}

		System.out.println("      generating dependency graph for method " + parseJsonString(currentObject.get("content")) + "()");
		exitNodes.clear();
		exitNodes.add(nodeIdentifier);
		objectName = methodName;
		System.out.println("MethodVisitor:" + exitNodes);
		argumentList = new ArrayList<String>();
		
		methodName += "(";
		visitParameters(nodeIdentifier);
		methodName += ")";
		exploreNode(objectContent.get(blockPosition), exitNodes);
		
		System.out.println("USE: " + use);
		System.out.println("DEF: " + def);
	}

	private void visitParameters(final String nodeIdentifier)
	{
		for (int i = 0; i < parameterNodes.size(); i++)
		{
			final String parameterNode = "\"" + objectName + "_param_" + parameterNodes.get(i) + "\"";
			final String parameterLabel = "Parameter: " + parameterNodes.get(i);
			pushVertex(parameterNode, parameterLabel);
			connectControlEdge(parameterNode, nodeIdentifier);
		}
	}
	
	final String generateVertexName(final String nodeId)
	{
		return "\"" + objectName + "_" + nodeId  + "\"";
	}
	
	private ArrayList<String> exploreNode(JsonValue jsonValue, ArrayList<String> previousStartNodes) throws JsonValueException
	{
		final ArrayList<String> startNodes = previousStartNodes;
		final ArrayList<String> exitNodes = new ArrayList<String>();
		final JsonObject currentNode = parseJsonObject(jsonValue);
		final JsonArray nodeChildren = parseJsonArray(currentNode.get("children"));
		int i = 0;

		if (currentNode.get("name").equals("Case"))
		{			
			final JsonObject caseNode = parseJsonObject(nodeChildren.get(0));
			final String caseNodeType = parseJsonString(caseNode.get("name"));
			
			String childStartingNode = generateNodeIdentifier() + ": Case " + processGeneric((JsonObject) nodeChildren.get(0));
			
			if (caseNodeType.equals("Literal"))
			{
				childStartingNode = generateNodeIdentifier() + ": Case " + processGeneric(caseNode);
			}
			else
			{
				childStartingNode = generateNodeIdentifier() + ": Default";
			}
			
			graph.pushVertex(childStartingNode);
			graph.setVertexShape(childStartingNode, "box");
			
			for (final String previousNode : startNodes)
			{
				connectControlEdge(childStartingNode, previousNode);
			}
			
			i = 1;	
			exitNodes.add(childStartingNode);
			saveDataFlow(childStartingNode);
		}
		else if (startNodes != null)
		{
			exitNodes.addAll(startNodes);
		}

		for (; i < nodeChildren.size(); i++)
		{
			final JsonObject newNode = (JsonObject) nodeChildren.get(i);
			final String nodeName = parseJsonString(newNode.get("name"));

			System.out.println(nodeName);
			returnMissing = true;

			switch (nodeName)
			{
			case "If":
				visitIf(exitNodes, argumentList, newNode);
				break;
			case "While":
				visitWhile(exitNodes, argumentList, newNode);
				break;
			case "For":
				visitFor(exitNodes, argumentList, newNode);
				break;
			case "Do":
				visitDoWhile(exitNodes, argumentList, newNode);
				break;
			case "Switch":
				visitSwitch(exitNodes, argumentList, newNode);
				break;
			case "Continue":
				return visitContinue(exitNodes, newNode);
			case "Break":
				return visitBreak(exitNodes, newNode);
			case "Return":
				returnMissing = false;
				return visitReturn(exitNodes, newNode);
			default:
				visitDefault(exitNodes, newNode);
				break;
			}
		}
		
		return exitNodes;
	}
	
	private ArrayList<String> visitBreak(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final String breakNodeId = generateNodeIdentifier();
		final String breakString = breakNodeId + ": " + parseJsonString(currentNode.get("name")).toLowerCase();

		pushVertex(breakNodeId, breakString);
		breakNodes.add(breakString);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, breakNodeId);
		}

		exitNodes.clear();
		
		return exitNodes;
	}

	private ArrayList<String> visitContinue(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final String continueNodeId = generateNodeIdentifier();
		final String continueNode = generateVertexName(continueNodeId);
		final String continueString = continueNodeId + ": " + parseJsonString(currentNode.get("name")).toLowerCase();

		pushVertex(continueNodeId, continueString);
		continueNodes.add(continueNode);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, continueNode);
		}

		exitNodes.clear();
		
		return exitNodes;
	}
	
	private void defaultReturn(final ArrayList<String> exitNodes)
	{
		final String returnNodeId = generateNodeIdentifier();
		final String returnVertex = generateVertexName(returnNodeId);
		final String returnLabel = returnNodeId + ": return";

		pushVertex(returnVertex, returnLabel);
		returnNodes.add(returnVertex);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, returnVertex);
		}

		exitNodes.clear();
	}

	private ArrayList<String> visitReturn(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray nodeChildren = parseJsonArray(currentNode.get("children"));
		final String returnNode = processGeneric(nodeChildren.get(0));
		final String returnNodeId = generateNodeIdentifier();
		final String returnVertex = generateVertexName(returnNodeId);
		final String returnLabel = returnNodeId + ": return " + returnNode;

		pushVertex(returnVertex, returnLabel);
		returnNodes.add(returnVertex);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, returnVertex);
		}

		exitNodes.clear();
		
		return exitNodes;
	}

	private void visitSwitch(final ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
	
		//+------------------------+//
		//| PROCESS CONDITION NODE |//
		//+------------------------+//
		
		final String conditionNode = processGeneric(children.get(0));
		final String conditionNodeId = generateNodeIdentifier();
		final String conditionVertex = generateVertexName(conditionNodeId);
		final String conditionLabel = conditionNodeId + ": " + conditionNode;

		pushVertex(conditionVertex, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionVertex);
		}

		exitNodes.clear();
		
		//+----------------------------+//
		//| PROCESS FALL-THROUGH NODES |//
		//+----------------------------+//
		
		int numberCases = children.size();
		final ArrayList<String> lastCaseElement = new ArrayList<String>();

		for (int j = 1; j < numberCases; j++)
		{
			argumentList.clear();
			lastCaseElement.clear();
			argumentList.add(conditionLabel);
			argumentList.addAll(lastCaseElement);
			lastCaseElement.addAll(exploreNode(children.get(j), argumentList));
		}

		exitNodes.addAll(lastCaseElement);
		exitNodes.addAll(breakNodes);
		breakNodes.clear();
	}

	private void visitIf(final ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		
		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//
		
		final String conditionNode = processGeneric( parseJsonObject(children.get(0)));
		final String conditionNodeId = generateNodeIdentifier();
		final String conditionVertex = generateVertexName(conditionNodeId);
		final String conditionLabel = conditionNodeId + ": " + conditionNode;

		pushVertex(conditionVertex, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionVertex);
		}

		exitNodes.clear();

		//+--------------------------+//
		//| PROCESS FIRST (IF) BLOCK |//
		//+--------------------------+//
		
		if (children.size() > 1)
		{
			argumentList.clear();
			argumentList.add(conditionVertex);
			exitNodes.addAll(exploreNode(children.get(1), argumentList));
		}

		//+-----------------------------+//
		//| PROCESS SECOND (ELSE) BLOCK |//
		//+-----------------------------+//
		
		if (children.size() > 2)
		{
			argumentList.clear();
			argumentList.add(conditionVertex);
			exitNodes.addAll(exploreNode(children.get(2), argumentList));
		}
	}
	
	private void visitDoWhile(ArrayList<String> exitNodes, final ArrayList<String> argumentList, JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		
		//+------------------------+//
		//| PROCESS CONDITION NODE |//
		//+------------------------+//
		
		final String conditionNode = processGeneric(children.get(0));
		final String conditionNodeId = generateNodeIdentifier();
		final String conditionVertex = generateVertexName(conditionNodeId);
		final String conditionLabel = conditionNodeId + ": " + conditionNode;
		
		//+-------------------------+//
		//| PROCESS "DO" START NODE |//
		//+-------------------------+//
		
		final String startNodeId = generateNodeIdentifier();
		final String startNodeVertex = generateVertexName(startNodeId);
		final String startNodeLabel = startNodeId + ": do";
		
		pushVertex(conditionVertex, conditionLabel);
		pushVertex(startNodeVertex, startNodeLabel);
		saveDataFlow(startNodeId);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, startNodeVertex);
		}

		argumentList.clear();
		argumentList.add(startNodeVertex);

		//+--------------------+//
		//| PROCESS CODE BLOCK |//
		//+--------------------+//

		exitNodes = exploreNode(children.get(1), argumentList);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, conditionVertex);
		}

		exitNodes.clear();
		connectControlEdge(startNodeVertex, conditionVertex);

		// connect breaks and continues
		System.out.println(breakNodes);
		exitNodes.addAll(breakNodes);
		breakNodes.clear();
		
		for (final String previousNode : continueNodes)
		{
			connectControlEdge(startNodeVertex, previousNode);
		}
		
		continueNodes.clear();
		exitNodes.add(conditionVertex);
	}

	private void visitWhile(ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		
		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//
		
		final String conditionNode = processGeneric(children.get(0));
		final String conditionNodeId = generateNodeIdentifier();
		final String conditionVertex = generateVertexName(conditionNodeId);
		final String conditionLabel = conditionNodeId + ": if (" + conditionNode + ")";

		pushDiamond(conditionVertex, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionVertex);
		}

		argumentList.clear();
		argumentList.add(conditionVertex);
		
		//+----------------------------+//
		//| PROCESS INSTRUCTIONS BLOCK |//
		//+----------------------------+//
		
		exitNodes = exploreNode(children.get(1), argumentList);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionVertex);
		}

		exitNodes.clear();
		System.out.println(breakNodes);
		exitNodes.addAll(breakNodes);
		breakNodes.clear();
		
		for (String previousNodeId : continueNodes)
		{
			connectControlEdge(conditionVertex, previousNodeId);
		}
		
		continueNodes.clear();
		exitNodes.add(conditionVertex);
	}
	
	void pushDiamond(final String nodeId, final String nodeLabel)
	{
		graph.pushVertex(nodeId);
		graph.setVertexLabel(nodeId, nodeLabel);
		graph.setVertexShape(nodeId, "diamond");
	}

	private void visitFor(ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{	
		final JsonArray children = parseJsonArray(currentNode.get("children"));
		
		//+-----------------------+//
		//|PROCESS ASSIGNMENT NODE|//
		//+-----------------------+//
		
		final String assignmentNode = processGeneric(children.get(0));
		final String assignmentNodeId = generateNodeIdentifier();
		final String assignmentNodeVertex = generateVertexName(assignmentNodeId);
		final String assignmentLabel = assignmentNodeId + ": " + assignmentNode;

		pushVertex(assignmentNodeVertex, assignmentLabel);
		saveDataFlow(assignmentNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, assignmentNodeVertex);
		}

		exitNodes.clear();

		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//
		
		final String conditionNode = processGeneric(children.get(1));
		final String conditionNodeId = generateNodeIdentifier();
		final String conditionVertex = generateVertexName(conditionNodeId);
		final String conditionLabel = conditionNodeId + ": if (" + conditionNode + ")";

		pushDiamond(conditionVertex, conditionLabel);
		connectControlEdge(assignmentNodeVertex, conditionVertex);
		
		//+----------------------------+//
		//| PROCESS INSTRUCTIONS BLOCK |//
		//+----------------------------+//
		
		argumentList.clear();
		argumentList.add(conditionVertex);
		exitNodes.addAll(exploreNode(children.get(3), argumentList));
		saveDataFlow(conditionNodeId);

		//+----------------------+//
		//|PROCESS STATEMENT NODE|//
		//+----------------------+//
		
		final String statementNode = processGeneric(children.get(2));
		final String statementNodeId = generateNodeIdentifier();
		final String statementVertex = generateVertexName(statementNodeId);
		final String statementLabel = statementNodeId + ": " + statementNode;

		pushVertex(statementVertex, statementLabel);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, statementVertex);
		}
		
		connectControlEdge(statementVertex, conditionVertex);
		exitNodes.clear();	
		exitNodes.addAll(breakNodes);
		breakNodes.clear();
		
		for (final String previousNodeId : continueNodes)
		{
			connectControlEdge(previousNodeId, conditionVertex);
		}
		
		continueNodes.clear();
		exitNodes.add(conditionVertex);
		saveDataFlow(statementNodeId);
	}

	private void visitDefault(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final String defaultNodeId = generateNodeIdentifier();
		final String defaultVertex = generateVertexName(defaultNodeId);
		final String defaultLabel = defaultNodeId + ": " + processGeneric(currentNode);

		System.out.println(defaultLabel);
		pushVertex(defaultVertex, defaultLabel);
		saveDataFlow(defaultNodeId);


		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, defaultVertex);
		}

		exitNodes.clear();
		exitNodes.add(defaultVertex);
	}

	private String processGeneric(final JsonValue jsonValue) throws JsonValueException
	{
		final JsonObject currentNode = parseJsonObject(jsonValue);
		final String type = currentNode.getString("name", null);
		final String content = currentNode.getString("content", null);
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		switch (type)
		{
		case "ParameterReference": case "TypeReference":
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
			return visitAssignment(content, children);
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

		return type;
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

	private String visitAssignment(final String content, final JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(1)) + " = " + processGeneric(children.get(2));
	}

	private String visitBinaryOperator(final String content, final JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(1)) + content + processGeneric(children.get(2));
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
}