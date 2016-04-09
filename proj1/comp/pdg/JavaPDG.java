package comp.pdg;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import comp.ast.LocalVariable;
import comp.dot.DotGraph;

public class JavaPDG
{
	private static DotGraph g = new DotGraph("Example");

	public static DotGraph getGraph()
	{
		return g;
	}
	public static void main(String[] args)
	{	
		g.setEdgeColor("blue");
		g.setEdgeStyle("dotted");
		g.setDpi(15);
		
		try (final BufferedReader in = Files.newBufferedReader(FileSystems.getDefault().getPath("ast.json"), StandardCharsets.UTF_8))
		{
			new Node(Json.parse(in).asObject(), null, 0);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		g.generateDotFile("myGraph.dot");
		g.outputGraph("pdf", "dot");
	}
	
	public static void printNode(JsonObject jsonNode, JsonValue jsonParent, int currentDepth)
	{
		final String identationFormat = currentDepth <= 0 ? "%s" : ("%" + Integer.toString(4 * currentDepth) + "s");
		final String nodeName = jsonNode.getString("name", "Unknown");
		final String nodeContent = jsonNode.getString("content", null);
		final JsonValue nodeChildren = jsonNode.get("children");
		final String nodeCode = jsonNode.getString("code", null);

		if (nodeName.equals("LocalVariable"))
		{
			if (!localsTable.containsKey(nodeContent))
			{
				localsTable.put(nodeContent, new LocalVariable(jsonNode, null));
			}
		}
		
		if (nodeContent != null)
		{
			if (nodeCode != null)
			{
				System.out.print(String.format(identationFormat + "%s(%s) -> %s\n", "", nodeName,nodeContent, nodeCode));  
			}
			else
			{
				System.out.print(String.format(identationFormat + "%s(%s)\n", "", nodeName,nodeContent));  
			}
		}
		else
		{
			if (nodeCode != null)
			{
				System.out.print(String.format(identationFormat + "%s -> %s\n", "", nodeName, nodeCode));
			}
			else
			{
				System.out.print(String.format(identationFormat + "%s\n", "", nodeName));
			}
		}
		
		if (nodeChildren != null)
		{
			treeTraverse(nodeChildren.asArray(), jsonNode, currentDepth + 1);
		}
	}
	
	private static int currentVertex = 1;
	
	private static HashMap<Integer, Integer> vertexMap = new HashMap<Integer, Integer>();
	private static HashMap<String, LocalVariable> localsTable = new HashMap<String, LocalVariable>();
	
	static {
		vertexMap.put(0, 0);
	}
	
	public static void treeTraverse(final JsonValue jsonObject, final JsonValue jsonParent, int currentDepth)
	{
		if (jsonObject.isArray())
		{
			final Iterator<JsonValue> thisIterator = jsonObject.asArray().iterator();
			
			while (thisIterator.hasNext())
			{
				treeTraverse(thisIterator.next(), jsonObject, currentDepth);
			}
		}
		else if (jsonObject.isObject())
		{
			String parentIndex = "0";
		
			if (jsonParent != null && vertexMap.containsKey(jsonParent.hashCode()))
			{
				parentIndex = Integer.toString(vertexMap.get(jsonParent.hashCode()));
			}

			final String vertexlabel = Integer.toString(currentVertex);

			vertexMap.put(jsonObject.hashCode(), currentVertex++);
			g.connectDirected(parentIndex, vertexlabel);
			g.setVertexLabel(vertexlabel, jsonObject.asObject().getString("name", "Unknown"));
			printNode(jsonObject.asObject(), jsonParent, currentDepth);
		}
	}
}