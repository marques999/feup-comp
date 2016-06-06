package java2pdg.analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

import java2pdg.SimplePair;

@SuppressWarnings("Duplicates")
public class MethodVisitor extends JavaVisitor
{
	private final ArrayList<String> argumentList;
	private final ArrayList<String> parameterNodes = new ArrayList<String>();
	private final ArrayList<String> breakNodes = new ArrayList<String>();
	private final ArrayList<String> continueNodes = new ArrayList<String>();
	private final ArrayList<String> throwNodes = new ArrayList<String>();
	private final ArrayList<String> returnNodes = new ArrayList<String>();
	private final ArrayList<String> fNodes = new ArrayList<>();
	private final String objectName;

	private boolean returnMissing;

	public MethodVisitor(final JsonObject currentObject, ArrayList<String> exitNodes) throws JsonValueException
	{
		int blockPosition = 1;

		final JsonArray objectContent = parseJsonArray(currentObject.get("children"));

		for (int i = blockPosition; i < objectContent.size(); i++)
		{
			final JsonObject currentParameter = parseJsonObject(objectContent.get(i));

			switch (parseJsonString(currentParameter.get("name")))
			{
				case "Parameter":
					parameterNodes.add(parseJsonString(currentParameter.get("content")));
					defTemp.addAll(parameterNodes);
					saveDataFlow(0);
					break;
				case "Block":
					blockPosition = i;
					break;
			}
		}

		String methodName = parseJsonString(currentObject.get("content"));

		objectName = methodName;

		final String nodeIdentifier = generateVertexName(0);

		pushMethod(nodeIdentifier, "function: " + methodName);
		returnMissing = false;

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, nodeIdentifier);
		}

		System.out.println("      generating dependency graph for method " + parseJsonString(currentObject.get("content")) + "()");
		exitNodes.clear();
		exitNodes.add("0");

		System.out.println("MethodVisitor:" + exitNodes);
		argumentList = new ArrayList<String>();

		methodName += "(";
		visitParameters(nodeIdentifier);
		methodName += ")";
		exploreNode(objectContent.get(blockPosition), exitNodes);

		System.out.println("USE: " + use);
		System.out.println("DEF: " + def);
		generateDataFlow();
		System.out.println("SUCCESSORS: " + mSuccessors);
	}

	void connectDataEdge(int targetId, int sourceId)
	{
		if (targetId == sourceId)
		{
			graph.setEdgeColor("green");
			graph.connectDirected(generateVertexName(targetId), generateVertexName(sourceId), "<def>");
		}
		else
		{
			graph.setEdgeColor("red");
			graph.connectDirected(generateVertexName(targetId), generateVertexName(sourceId), "<use>");
		}

		graph.setEdgeColor("blue");
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

	final String generateVertexName(int nodeId)
	{
		return "\"" + objectName + "_" + nodeId + "\"";
	}

	final String generateVertexName(final String nodeId)
	{
		return "\"" + objectName + "_" + nodeId + "\"";
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

			int childStartingId = generateNodeIdentifier();
			String childStartingNode; // = childStartingId + ": Case " + processGeneric((JsonObject) nodeChildren.get(0)); REDUNDANT

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
				connectControlEdge(childStartingId, previousNode);
				connectSuccessors(childStartingId, previousNode);
			}

			i = 1;
			exitNodes.add(Integer.toString(childStartingId));
			saveDataFlow(childStartingId);
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
				case "Throw":
					return visitThrow(exitNodes, newNode);
				case "Continue":
					return visitContinue(exitNodes, newNode);
				case "Break":
					return visitBreak(exitNodes, newNode);
				case "Return":
					returnMissing = false;
					return visitReturn(exitNodes, newNode);
				case "Try":
					visitTry(exitNodes, newNode);
					break;
				case "Invocation":
					visitInvocation(exitNodes, newNode);
					break;
				default:
					visitDefault(exitNodes, newNode);
					break;
			}
		}

		return exitNodes;
	}

	private void visitInvocation(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		int defaultNodeId = generateNodeIdentifier();
		final String defaultLabel = defaultNodeId + ": " + visitInvocationNode(children);

		pushVertex(defaultNodeId, defaultLabel);
		saveDataFlow(defaultNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, defaultNodeId);
			connectSuccessors(previousNodeId, defaultNodeId);
		}

		exitNodes.clear();
		exitNodes.add(Integer.toString(defaultNodeId));

		String functionNode = children.get(1).asObject().getString("content", null);
		if (functionNode != null)
		{
			graph.connectDirected(generateVertexName(defaultNodeId), functionNode + "_0");
		}
	}

	private void visitBlock(ArrayList<String> exitNodes, JsonObject currentNode) throws JsonValueException
	{
	}

	private void visitCatch(ArrayList<String> exitNodes, JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//

		int conditionNodeId = generateNodeIdentifier();
		final String conditionLabel = conditionNodeId + ": Catch ()";

		pushVertex(conditionNodeId, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		exitNodes.clear();

		//+--------------------------+//
		//|   PROCESS Catch BLOCK    |//
		//+--------------------------+//

		for (JsonValue child : children)
		{
			argumentList.clear();
			argumentList.add(Integer.toString(conditionNodeId));
			exitNodes.addAll(exploreNode(child, argumentList));
		}
	}

	private void visitTry(ArrayList<String> exitNodes, JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//

		int conditionNodeId = generateNodeIdentifier();
		final String conditionLabel = conditionNodeId + ": Try";

		pushVertex(conditionNodeId, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		exitNodes.clear();

		//+--------------------------+//
		//| PROCESS FIRST (TRY) BLOCK |//
		//+--------------------------+//

		for (JsonValue child : children)
		{
			argumentList.clear();
			argumentList.add(Integer.toString(conditionNodeId));
			exitNodes.addAll(exploreNode(child, argumentList));
			//System.out.println(parseJsonObject(child).get("name"));
		}
	}

	private final HashMap<Integer, HashSet<Integer>> mSuccessors = new HashMap<>();

	void connectSuccessors(final Integer targetVertex, final Integer sourceVertex)
	{
		if (mSuccessors.containsKey(targetVertex))
		{
			mSuccessors.get(targetVertex).add(sourceVertex);
		}
		else
		{
			mSuccessors.put(targetVertex, new HashSet<>());
			mSuccessors.get(targetVertex).add(sourceVertex);
		}
	}

	void connectSuccessors(int targetVertex, final String sourceVertex)
	{
		try
		{
			connectSuccessors(targetVertex, Integer.parseInt(sourceVertex));
		} catch (NumberFormatException ex)
		{
		}
	}

	void connectSuccessors(final String targetVertex, int sourceVertex)
	{
		try
		{
			connectSuccessors(Integer.parseInt(targetVertex), sourceVertex);
		} catch (NumberFormatException ex)
		{
		}
	}

	private ArrayList<String> visitBreak(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		int breakNodeId = generateNodeIdentifier();
		final String breakString = breakNodeId + ": " + parseJsonString(currentNode.get("name")).toLowerCase();

		pushVertex(breakNodeId, breakString);
		breakNodes.add(Integer.toString(breakNodeId));

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, breakNodeId);
			connectSuccessors(previousNodeId, breakNodeId);
		}

		exitNodes.clear();

		return exitNodes;
	}

	void connectControlEdge(int targetId, final String sourceId)
	{
		connectControlEdge(generateVertexName(targetId), generateVertexName(sourceId));
	}

	void connectControlEdge(final String targetId, int sourceId)
	{
		connectControlEdge(generateVertexName(targetId), generateVertexName(sourceId));
	}

	void connectControlEdge(int targetId, int sourceId)
	{
		connectControlEdge(generateVertexName(targetId), generateVertexName(sourceId));
	}

	private ArrayList<String> visitContinue(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		int continueNodeId = generateNodeIdentifier();
		final String continueString = continueNodeId + ": " + parseJsonString(currentNode.get("name")).toLowerCase();

		pushVertex(continueNodeId, continueString);
		continueNodes.add(Integer.toString(continueNodeId));

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, continueNodeId);
			connectSuccessors(previousNodeId, continueNodeId);
		}

		exitNodes.clear();

		return exitNodes;
	}

	private ArrayList<String> visitThrow(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray nodeChildren = parseJsonArray(currentNode.get("children"));

		int throwNodeId = generateNodeIdentifier();
		final String throwNode = processGeneric(nodeChildren.get(0));
		final String throwString = throwNodeId + ": " + parseJsonString(currentNode.get("name")).toLowerCase();

		pushVertex(throwNodeId, throwString);
		throwNodes.add(Integer.toString(throwNodeId));

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, throwNodeId);
			connectSuccessors(previousNodeId, throwNodeId);
		}

		exitNodes.addAll(throwNodes);
		saveDataFlow(throwNodeId);
		exitNodes.clear();

		return exitNodes;
	}

	private void defaultReturn(final ArrayList<String> exitNodes)
	{
		int returnNodeId = generateNodeIdentifier();
		final String returnLabel = returnNodeId + ": return";

		pushVertex(returnNodeId, returnLabel);
		returnNodes.add(Integer.toString(returnNodeId));

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, returnNodeId);
			connectSuccessors(previousNode, returnNodeId);
		}

		exitNodes.clear();
	}

	private ArrayList<String> visitReturn(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray nodeChildren = parseJsonArray(currentNode.get("children"));
		final String returnNode = processGeneric(nodeChildren.get(0));
		int returnNodeId = generateNodeIdentifier();
		final String returnLabel = returnNodeId + ": return " + returnNode;

		pushVertex(returnNodeId, returnLabel);
		returnNodes.add(Integer.toString(returnNodeId));

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, returnNodeId);
			connectSuccessors(previousNode, returnNodeId);
		}

		exitNodes.addAll(returnNodes);
		saveDataFlow(returnNodeId);
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
		int conditionNodeId = generateNodeIdentifier();
		final String conditionLabel = conditionNodeId + ": " + conditionNode;

		pushVertex(conditionNodeId, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
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

		int conditionNodeId = generateNodeIdentifier();
		final String conditionNode = processGeneric(parseJsonObject(children.get(0)));
		final String conditionLabel = conditionNodeId + ": " + conditionNode;

		pushVertex(conditionNodeId, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		exitNodes.clear();

		//+--------------------------+//
		//| PROCESS FIRST (IF) BLOCK |//
		//+--------------------------+//

		if (children.size() > 1)
		{
			argumentList.clear();
			argumentList.add(Integer.toString(conditionNodeId));
			exitNodes.addAll(exploreNode(children.get(1), argumentList));
		}

		//+-----------------------------+//
		//| PROCESS SECOND (ELSE) BLOCK |//
		//+-----------------------------+//

		if (children.size() > 2)
		{
			argumentList.clear();
			argumentList.add(Integer.toString(conditionNodeId));
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
		int conditionNodeId = generateNodeIdentifier();
		final String conditionLabel = conditionNodeId + ": " + conditionNode;

		//+-------------------------+//
		//| PROCESS "DO" START NODE |//
		//+-------------------------+//

		int startNodeId = generateNodeIdentifier();
		final String startNodeLabel = startNodeId + ": do";

		pushVertex(conditionNodeId, conditionLabel);
		pushVertex(startNodeId, startNodeLabel);
		saveDataFlow(startNodeId);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, startNodeId);
			connectSuccessors(previousNode, startNodeId);
		}

		argumentList.clear();
		argumentList.add(Integer.toString(startNodeId));

		//+--------------------+//
		//| PROCESS CODE BLOCK |//
		//+--------------------+//

		exitNodes = exploreNode(children.get(1), argumentList);

		for (final String previousNode : exitNodes)
		{
			connectControlEdge(previousNode, conditionNodeId);
			connectSuccessors(previousNode, conditionNodeId);
		}

		exitNodes.clear();
		connectControlEdge(startNodeId, conditionNodeId);
		connectSuccessors(startNodeId, conditionNodeId);

		// connect breaks and continues
		System.out.println(breakNodes);
		exitNodes.addAll(breakNodes);
		breakNodes.clear();

		for (final String previousNode : continueNodes)
		{
			connectControlEdge(startNodeId, previousNode);
			connectSuccessors(startNodeId, previousNode);
		}

		continueNodes.clear();
		exitNodes.add(Integer.toString(conditionNodeId));
	}

	private void visitWhile(ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//

		final String conditionNode = processGeneric(children.get(0));
		int conditionNodeId = generateNodeIdentifier();
		final String conditionLabel = conditionNodeId + ": if (" + conditionNode + ")";

		pushDiamond(conditionNodeId, conditionLabel);
		saveDataFlow(conditionNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		argumentList.clear();
		argumentList.add(Integer.toString(conditionNodeId));

		//+----------------------------+//
		//| PROCESS INSTRUCTIONS BLOCK |//
		//+----------------------------+//

		exitNodes = exploreNode(children.get(1), argumentList);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		exitNodes.clear();
		System.out.println(breakNodes);
		exitNodes.addAll(breakNodes);
		breakNodes.clear();

		for (String previousNodeId : continueNodes)
		{
			connectControlEdge(conditionNodeId, previousNodeId);
			connectSuccessors(conditionNodeId, previousNodeId);
		}

		continueNodes.clear();
		exitNodes.add(Integer.toString(conditionNodeId));
	}

	void pushDiamond(int nodeId, final String nodeLabel)
	{
		String vertexName = generateVertexName(nodeId);
		graph.pushVertex(vertexName);
		graph.setVertexLabel(vertexName, nodeLabel);
		graph.setVertexShape(vertexName, "diamond");
	}

	final HashMap<Integer, HashSet<String>> use = new HashMap<>();
	final HashMap<Integer, HashSet<String>> def = new HashMap<>();
	final HashSet<String> useTemp = new HashSet<>();
	final HashSet<String> defTemp = new HashSet<>();

	final void saveDataFlow(final Integer nodeId)
	{
		use.put(nodeId, new HashSet<>(useTemp));
		def.put(nodeId, new HashSet<>(defTemp));
		useTemp.clear();
		defTemp.clear();
	}

	private void visitFor(ArrayList<String> exitNodes, final ArrayList<String> argumentList, final JsonObject currentNode) throws JsonValueException
	{
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		//+-----------------------+//
		//|PROCESS ASSIGNMENT NODE|//
		//+-----------------------+//

		int assignmentNodeId = generateNodeIdentifier();
		final String assignmentNode = processGeneric(children.get(0));
		final String assignmentLabel = assignmentNodeId + ": " + assignmentNode;

		pushVertex(assignmentNodeId, assignmentLabel);
		saveDataFlow(assignmentNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, assignmentNodeId);
			connectSuccessors(previousNodeId, assignmentNodeId);
		}

		exitNodes.clear();

		//+----------------------+//
		//|PROCESS CONDITION NODE|//
		//+----------------------+//

		int conditionNodeId = generateNodeIdentifier();
		final String conditionNode = processGeneric(children.get(1));
		final String conditionLabel = conditionNodeId + ": if (" + conditionNode + ")";

		pushDiamond(conditionNodeId, conditionLabel);
		connectControlEdge(assignmentNodeId, conditionNodeId);
		connectSuccessors(assignmentNodeId, conditionNodeId);

		//+----------------------------+//
		//| PROCESS INSTRUCTIONS BLOCK |//
		//+----------------------------+//

		argumentList.clear();
		argumentList.add(Integer.toString(conditionNodeId));
		saveDataFlow(conditionNodeId);
		exitNodes.addAll(exploreNode(children.get(3), argumentList));

		//+----------------------+//
		//|PROCESS STATEMENT NODE|//
		//+----------------------+//

		final String statementNode = processGeneric(children.get(2));
		int statementNodeId = generateNodeIdentifier();
		final String statementLabel = statementNodeId + ": " + statementNode;

		pushVertex(statementNodeId, statementLabel);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, statementNodeId);
			connectSuccessors(previousNodeId, statementNodeId);
		}

		connectControlEdge(statementNodeId, conditionNodeId);
		connectSuccessors(statementNodeId, conditionNodeId);
		exitNodes.clear();
		exitNodes.addAll(breakNodes);
		breakNodes.clear();

		for (final String previousNodeId : continueNodes)
		{
			connectControlEdge(previousNodeId, conditionNodeId);
			connectSuccessors(previousNodeId, conditionNodeId);
		}

		continueNodes.clear();
		exitNodes.add(Integer.toString(conditionNodeId));
		saveDataFlow(statementNodeId);
	}

	private void pushVertex(int nodeId, final String nodeLabel)
	{
		String vertexName = generateVertexName(nodeId);
		graph.pushVertex(vertexName);
		graph.setVertexLabel(vertexName, nodeLabel);
		graph.setVertexShape(vertexName, "box");
	}

	private void visitDefault(final ArrayList<String> exitNodes, final JsonObject currentNode) throws JsonValueException
	{
		int defaultNodeId = generateNodeIdentifier();
		final String defaultLabel = defaultNodeId + ": " + processGeneric(currentNode);

		pushVertex(defaultNodeId, defaultLabel);
		saveDataFlow(defaultNodeId);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, defaultNodeId);
			connectSuccessors(previousNodeId, defaultNodeId);
		}

		exitNodes.clear();
		exitNodes.add(Integer.toString(defaultNodeId));
	}

	private void generateDataFlow()
	{
		final LinkedList<Integer> queue = new LinkedList<>();
		final HashSet<SimplePair> edgeConnections = new HashSet<>();
		final HashMap<String, Integer> currentLastDef = new HashMap<>();
		final HashMap<Integer, Statement> statementDef = new HashMap<>();

		queue.addAll(mSuccessors.keySet());

		while (!queue.isEmpty())
		{
			final Integer currentNodeId = queue.poll();
			final HashSet<String> defSet = def.get(currentNodeId);
			final HashSet<String> useSet = use.get(currentNodeId);

			Statement previousDefs = statementDef.get(currentNodeId);

			if (previousDefs == null)
			{
				statementDef.put(currentNodeId, new Statement());
				previousDefs = statementDef.get(currentNodeId);
			}

			boolean statementChanged = false;

			if (useSet == null)
			{
				continue;
			}

			for (final String currentVariable : useSet)
			{
				final Integer lastDefStatement = currentLastDef.get(currentVariable);

				if (lastDefStatement != null)
				{
					previousDefs.updateVariable(currentVariable, lastDefStatement);

					if (previousDefs.hasChanged())
					{
						statementChanged = true;
					}

					edgeConnections.add(new SimplePair(lastDefStatement, currentNodeId));
				}
			}

			for (final String currentVariable : defSet)
			{
				currentLastDef.put(currentVariable, currentNodeId);
			}

			if (statementChanged)
			{
				final HashSet<Integer> mySucessors = mSuccessors.get(currentNodeId);

				if (mySucessors != null)
				{
					queue.addAll(mySucessors);
				}
			}
		}

		for (final SimplePair connection : edgeConnections)
		{
			connectDataEdge(connection.getFirst(), connection.getSecond());
		}
	}

	private String processGeneric(final JsonValue jsonValue) throws JsonValueException
	{
		final JsonObject currentNode = parseJsonObject(jsonValue);
		final String type = currentNode.getString("name", null);
		final String content = currentNode.getString("content", null);
		final JsonArray children = parseJsonArray(currentNode.get("children"));

		switch (type)
		{
			case "ParameterReference":
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
			case "ArrayTypeReference":
				return visitArrayTypeReference(children);
			case "NewArray":
				return visitNewArray(content, children);
			case "ArrayWrite":
				return visitArrayWrite(children);
			case "ArrayRead":
				return visitArrayRead(children);
			case "CatchVariable":
			case "TypeAccess":
				return processGeneric(children.get(0));
			case "ConstructorCall":
				return visitConstructor(children);
			case "ExecutableReference":
				return visitExecutableReference(content);
			//case "Invocation":
			//	return visitInvocation(children);
			case "FieldRead":
				return visitFieldRead(children);
			case "FieldReference":
				return visitFieldReference(content, children);
			case "Invocation":
				return visitInvocationNode(children);
			case "Break":
				return type;
			case "Continue":
				return type;
			case "Throw":
				return type;
			case "Return":
				return type;
		}

		return type;
	}

	private String visitInvocationNode(JsonArray children) throws JsonValueException
	{
		String res = "";
		int i = 1;
		for (; i < children.size() - 1; i++)
		{
			if (children.get(i).asObject().getString("name", null).equals("ExecutableReference") && i + 1 != children.size())
			{
				res += processGeneric(children.get(i)) + "(" + processGeneric(children.get(i + 1)) + ")" + (i < children.size() - 2 ? "." : "");
				i++;
			}
			else
				res += processGeneric(children.get(i)) + ".";
		}
		return res;
	}

	private String visitFieldReference(String content, JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(0)) + "." + content;
	}

	private String visitFieldRead(JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(1));
	}


	private String visitExecutableReference(String content) throws JsonValueException
	{
		switch (content)
		{
			case "<init>":
				return "new";
			default:
				return content;
		}
	}

	private String visitConstructor(JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(1)) + " " + processGeneric(children.get(0)) + "()";
	}

	private String visitArrayRead(JsonArray children) throws JsonValueException
	{
		String variable = processGeneric(children.get(1));
		useTemp.add(variable);
		return variable + "[" + processGeneric(children.get(2)) + "]";
	}

	private String visitArrayWrite(JsonArray children) throws JsonValueException
	{
		String variable = processGeneric(children.get(1));
		defTemp.add(variable);
		return variable + "[" + processGeneric(children.get(2)) + "]";
	}

	private String visitArrayTypeReference(JsonArray children) throws JsonValueException
	{
		return processGeneric(children.get(0)) + "[]";
	}

	private String visitNewArray(final String nodeContent, final JsonArray nodeChildren) throws JsonValueException
	{
		String content;
		if (nodeContent.equals("type:dimension"))
		{
			content = "new " + processGeneric(nodeChildren.get(0));
		}
		else
		{
			content = "{";

			for (JsonValue children : nodeChildren)
			{
				if (children.equals(nodeChildren.get(0)))
					continue;

				content += processGeneric(children);

				if (!children.equals(nodeChildren.get(nodeChildren.size() - 1)))
					content += ",";
			}
			content += "}";
		}

		return content;
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