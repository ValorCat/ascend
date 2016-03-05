package interpreter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import interpreter.Compiler.CommandData;
import util.AscendException;
import util.TokenArray;

public class Ascend {

	public static boolean debugMode = false;
	public static Compiler compiler;
	public static int line = 0;
	
	public static void main(String[] args) {
		run("input.asc", StandardCharsets.UTF_8);
	}
	
	public static void run(String path, Charset encoding) {
		String input;
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(path));
			input = new String(bytes, encoding).replaceAll("\r", "");
		} catch (IOException e) {
			System.out.println("Failed to read the file");
			if (debugMode) {
				e.printStackTrace();
			}
			return;
		}
		execute(input);
	}
	
	public static void execute(String input) {
		try {
			if (input.startsWith("//%debug%\n")) {
				debugMode = true;
			}
			compiler = new Compiler();
			Parser.loadNewParser();
			TokenArray tokens = Tokenizer.tokenize(input);
			if (debugMode) {
				System.out.println("Tokens: " + tokens);
			}
			CommandData commands = compiler.compile(tokens);
			Parser.getParser().parse(commands);
		} catch (AscendException e) {
			if (line == 0) {
				System.out.printf("\n\n%s: %s\n", e.getName(), e.getMessage());
			} else {
				System.out.printf("\n\n%s, line %d: %s\n", e.getName(), line, e.getMessage());
			}
			if (debugMode) {
				StackTraceElement[] traceback = e.getStackTrace();
				for (int i = 1; i <= traceback.length; i++) {
					System.out.printf("    (%d) %s\n", i, traceback[i - 1]);
				}
				System.out.println();
			}
		} finally {
			if (debugMode) {
				System.out.println("\n======================================================================");
				System.out.println("Line: " + line);
				System.out.println("Environment: " + Parser.getParser().getEnv());
			}
		}
	}

}
