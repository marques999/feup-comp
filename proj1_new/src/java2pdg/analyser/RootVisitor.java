package java2pdg.analyser;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class RootVisitor extends JavaVisitor
{
	public RootVisitor(final JsonObject currentAst) throws JsonValueException
	{
		final JsonObject astRoot = parseJsonObject(parseJsonArray(currentAst.get("children")).get(0));
		final JsonObject compilationUnit = parseJsonObject(astRoot);
		final JsonArray compilationUnitPackages = parseJsonArray(compilationUnit.get("children"));

		System.out.println("generating dependency graph for " + compilationUnit.get("name"));

		for (final JsonValue jsonValue : compilationUnitPackages)
		{
			new PackageVisitor(parseJsonObject(jsonValue), null);
		}
	}
}