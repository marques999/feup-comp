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
	public static void main(String args[])
	{
		graph = new DotGraph("ABCD");
		graph.setEdgeColor("blue");
		graph.setEdgeStyle("dotted");
		graph.setDpi(15);

		try
		{
			new RootVisitor(parseFile("test/FirstTest.json"));
		}
		catch (final IOException | JsonValueException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}

		graph.generateDotFile("myGraph.dot");
		graph.outputGraph("png", "neato");
	}
	
	private static DotGraph graph;

	public static DotGraph getGraph()
	{
		return graph;
	}

	private static JsonObject parseFile(String astFile) throws IOException
	{
		try (final BufferedReader in = Files.newBufferedReader(FileSystems.getDefault().getPath(astFile), StandardCharsets.UTF_8))
		{
			return Json.parse(in).asObject();
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
}