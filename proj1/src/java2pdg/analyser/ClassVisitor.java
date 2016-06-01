package java2pdg.analyser;

import java.util.ArrayList;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class ClassVisitor extends JavaVisitor
{
	public ClassVisitor(final JsonObject currentClass, final ArrayList<String> exitNodes) throws JsonValueException
	{
		final JsonArray classProperties = parseJsonArray(currentClass.get("children"));
		final String className = parseJsonString(currentClass.get("content")) + "()";
		final String classIdentifier = "\"class_" + className + "\"";

		pushVertex(classIdentifier, "Class: " + className);

		for (final String previousNodeId : exitNodes)
		{
			connectControlEdge(previousNodeId, classIdentifier);
		}

		System.out.println("    generating dependency graph for class " + parseJsonString(currentClass.get("content")));

		for (final JsonValue currentProperty : classProperties)
		{
			exitNodes.clear();
			exitNodes.add(classIdentifier);
			new PropertyVisitor(currentProperty, exitNodes);
		}
	}

	@Override
	void pushVertex(final String nodeId, final String nodeLabel)
	{
		graph.pushVertex(nodeId);
		graph.setVertexLabel(nodeId, nodeLabel);
		graph.setVertexShape(nodeId, "component");
	}
}