package java2pdg;

import java2pdg.analyser.AST;
import java2pdg.analyser.Visitor;

public class Java2Pdg
{
	private static AST ast;
	private static Visitor visitor;

	public static void main(String args[])
	{
		ast = new AST("/home/pedro/IdeaProjects/feup-comp/proj1_new/ast.json");
		visitor = new Visitor(ast);
	}

	public static AST getAST()
	{
		return ast;
	}

	public static Visitor getVisitor()
	{
		return visitor;
	}

	public static void setVisitor(final Visitor visitor)
	{
		Java2Pdg.visitor = visitor;
	}
}