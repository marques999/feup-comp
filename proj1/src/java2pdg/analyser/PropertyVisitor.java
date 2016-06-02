package java2pdg.analyser;

import java.util.ArrayList;

import eclipsesource.json.JsonObject;
import eclipsesource.json.JsonValue;

public class PropertyVisitor extends JavaVisitor
{
	public PropertyVisitor(final JsonValue jsonMethod, final ArrayList<String> exitNodes) throws JsonValueException
	{
		final JsonObject currentObject = parseJsonObject(jsonMethod);

		switch (parseJsonString(currentObject.get("name")))
		{
		case "Constructor":
			System.out.println("      generating dependency graph for constructor " + parseJsonString(currentObject.get("content")));
			break;
		case "Method":
			new MethodVisitor(currentObject, exitNodes);
			break;
		}
	}
}