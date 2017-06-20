package org.salve.drools.model.template;

import org.salve.drools.Functions;

import java.util.function.Function;

/**
 * This class is just a wrapper around string to provide typesafety checks.
 */
public abstract class ATemplate {
	public final String name;

	private final int hash_value;
	private final String to_string_value;
	public ATemplate(String name) {
		this.name = name;
		to_string_value = this.getClass().getSimpleName() + "("+name+")";
		hash_value = this.name.hashCode();
	}

	@Override
	public int hashCode(){
		return hash_value;
	}
	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other -> this.name.equals(other.name)
		);
	}
	public String toString(){
		return to_string_value;
	}
}
