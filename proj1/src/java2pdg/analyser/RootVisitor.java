package java2pdg.analyser;

import eclipsesource.json.JsonArray;
import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class RootVisitor extends JavaVisitor
{
	public RootVisitor(final JsonObject currentAst) throws JsonValueException
	{
		final JsonObject astRoot = parseJsonObject(parseJsonArray(currentAst.get("children")).get(0));
		final JsonArray compilationUnit = parseJsonArray(parseJsonObject(astRoot).get("children"));

		for (final JsonValue jsonValue : compilationUnit)
		{
			new PackageVisitor(parseJsonObject(jsonValue), null);
		}
	}
}