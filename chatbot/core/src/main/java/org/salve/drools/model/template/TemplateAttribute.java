package org.salve.drools.model.template;

import org.salve.drools.Functions;

import java.util.function.Function;

/**
 * Strings that are intended to be template names.
 * Such as identifiers in the regex, or identifiers in a literal.
 */
public class TemplateAttribute extends ATemplate{
	public TemplateAttribute(String name) {
		super(name);
	}
}
