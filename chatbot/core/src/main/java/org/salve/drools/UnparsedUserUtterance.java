package org.salve.drools;

import javax.annotation.concurrent.Immutable;

/**
 * Wrap type to identify the userutterance in drool rules
 */
@Immutable
public class UnparsedUserUtterance {
	public final String value;

	public UnparsedUserUtterance(String request) {
		super();
		this.value = request;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString(){
		return "u>" + value + "<";
	}
}
