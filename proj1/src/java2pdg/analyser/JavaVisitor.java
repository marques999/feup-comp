package java2pdg.analyser;

import java.util.HashMap;
import java.util.HashSet;

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
		nodeCounter = 1;
		edgeCounter = 1;
	}

	final DotGraph graph = Java2Pdg.getGraph();
	final HashMap<String, HashSet<String>> use = new HashMap<>();
	final HashMap<String, HashSet<String>> def = new HashMap<>();
	final HashSet<String> useTemp = new HashSet<>();
	final HashSet<String> defTemp = new HashSet<>();

	final void saveDataFlow(final String nodeId)
	{
		use.put(nodeId, new HashSet<>(useTemp));
		def.put(nodeId, new HashSet<>(defTemp));
		useTemp.clear();
		defTemp.clear();
	}

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

	final String generateNodeIdentifier()
	{
		return Integer.toString(nodeCounter++);
	}

	final void connectControlEdge(final String targetId, final String sourceId)
	{
		graph.connectDirected(targetId, sourceId, generateEdgeIdentifier());
	}

	final String generateEdgeIdentifier()
	{
		return Integer.toString(edgeCounter++);
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
}