package java2pdg.analyser;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

import java2pdg.Java2Pdg;
import java2pdg.dot.DotGraph;

public class JavaVisitor
{
	private int nodeCounter;
	private int edgeCounter;

	public JavaVisitor()
	{
		nodeCounter = 0;
		edgeCounter = 0;
	}

	final DotGraph graph = Java2Pdg.getGraph();

	final String parseJsonString(final JsonValue jsonValue) throws JsonValueException
	{
		if (jsonValue.isString())
		{
			return jsonValue.asString();
		}

		throw new JsonValueException(jsonValue, "String");
	}

	final JsonArray parseJsonArray(final JsonValue jsonValue) throws JsonValueException
	{
		if (jsonValue.isArray())
		{
			return jsonValue.asArray();
		}

		throw new JsonValueException(jsonValue, "JsonArray");
	}

	final JsonObject parseJsonObject(final JsonValue jsonValue) throws JsonValueException
	{
		if (jsonValue.isObject())
		{
			return jsonValue.asObject();
		}

		throw new JsonValueException(jsonValue, "JsonObject");
	}

	int generateNodeIdentifier()
	{
		return ++nodeCounter;
	}

	void connectControlEdge(final String targetId, final String sourceId)
	{
		graph.connectDirected(targetId, sourceId);
	}

	final String generateEdgeIdentifier()
	{
		return Integer.toString(++edgeCounter);
	}

	void pushVertex(final String nodeId, final String nodeLabel)
	{
		graph.pushVertex(nodeId);
		graph.setVertexLabel(nodeId, nodeLabel);
		graph.setVertexShape(nodeId, "box");
	}

	final void pushMethod(final String nodeId, final String nodeLabel)
	{
		graph.pushVertex(nodeId);
		graph.setVertexLabel(nodeId, nodeLabel);
	}

	int getLastNode()
	{
		return nodeCounter;
	}
}