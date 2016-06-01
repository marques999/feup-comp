package java2pdg;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import eclipsesource.json.Json;
import eclipsesource.json.JsonObject;

import java2pdg.analyser.JsonValueException;
import java2pdg.analyser.RootVisitor;

import java2pdg.dot.DotGraph;

public class Java2Pdg
{
	private static DotGraph graph;

	public static void main(String args[])
	{
		graph = new DotGraph("ABCD");
		graph.setEdgeColor("blue");
		graph.setEdgeStyle("dotted");
		graph.setDpi(15);

		try
		{
			parseFile("/home/pedro/IdeaProjects/feup-comp/proj1/test/Teste.json");
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}

		try
		{
			new RootVisitor(ast);
		}
		catch (JsonValueException ex)
		{
			ex.printStackTrace();
		}

		graph.generateDotFile("myGraph.dot");
		graph.outputGraph("png", "dot");

	}

	public static DotGraph getGraph()
	{
		return graph;
	}

	private static JsonObject ast;

	private static void parseFile(String astFile) throws IOException
	{
		try (final BufferedReader in = Files.newBufferedReader(FileSystems.getDefault().getPath(astFile), StandardCharsets.UTF_8))
		{
			ast = Json.parse(in).asObject();
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public JsonObject getTree()
	{
		return ast;
	}
}