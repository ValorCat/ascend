package lang;

import cmd.Command;
import util.TokenArray;

public interface Statement {
	
	boolean isValid(TokenArray statement);
	
	Command[] apply(TokenArray statement);
	
}
