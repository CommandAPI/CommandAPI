package dev.jorel.commandapi;

// This is the first way I thought to highlight where methods are not implemented
//  Though all the warnings may be a little unnecessary ¯\_(ツ)_/¯
@Deprecated(since = "TODO: Implement this method")
public class UnimplementedMethodException extends RuntimeException {
	public UnimplementedMethodException() {
		super("This method has not been implemented - CommandAPI");
	}
}