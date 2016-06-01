package java2pdg.analyser;

import java.util.ArrayList;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class PackageVisitor extends JavaVisitor
{
	public PackageVisitor(final JsonValue jsonPackage, ArrayList<String> exitNodes) throws JsonValueException
	{
		final JsonObject currentPackage = parseJsonObject(jsonPackage);
		final String packageLabel = parseJsonString(currentPackage.get("content"));
		final String packageIdentifier = "\"package_" + packageLabel + "\"";
		final JsonArray packageClasses = parseJsonArray(currentPackage.get("children"));

		pushVertex(packageIdentifier, "Package: " + packageLabel);

		if (exitNodes == null)
		{
			exitNodes = new ArrayList<>();
		}
		else
		{
			for (final String previousNodeId : exitNodes)
			{
				connectControlEdge(previousNodeId, packageIdentifier);
			}
		}

		System.out.println("  generating dependency graph for package " + packageLabel);

		for (final JsonValue jsonObject : packageClasses)
		{
			final JsonObject currentObject = parseJsonObject(jsonObject);

			exitNodes.clear();
			exitNodes.add(packageIdentifier);

			switch (parseJsonString(currentObject.get("name")))
			{
			case "Class":
				new ClassVisitor(currentObject, exitNodes);
				break;
			case "Package":
				new PackageVisitor(currentObject, exitNodes);
				break;
			}
		}
	}

	@Override
	void pushVertex(final String nodeId, final String nodeLabel)
	{
		graph.pushVertex(nodeId);
		graph.setVertexLabel(nodeId, nodeLabel);
		graph.setVertexShape(nodeId, "folder");
	}
}