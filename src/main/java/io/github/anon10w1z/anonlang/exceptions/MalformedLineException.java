package io.github.anon10w1z.anonlang.exceptions;

/**
 * An exception thrown due to a malformed line that couldn't be processed
 */
public class MalformedLineException extends AnonLangException {
	public MalformedLineException(String message) {
		super(message);
	}
}
