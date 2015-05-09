package io.github.anon10w1z.anonlang.exceptions;

import io.github.anon10w1z.anonlang.AnonLang;

/**
 * An exception thrown by the interpreter
 */
public class AnonLangException extends RuntimeException {
	public AnonLangException(String message) {
		super(message);
	}

	@Override
	public void printStackTrace() {
		System.err.println(this.toString().replaceFirst("io.github.anon10w1z.anonlang.exceptions.", "").replaceFirst("Exception:", "Exception at line #" + (AnonLang.currentIndex + 1) + ":"));
	}
}
