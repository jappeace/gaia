package org.salve.drools.model.values;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;

@Immutable
public class PerlocutionaryValue {
	public final String name;
	private final int hash_value;
	private PerlocutionaryValue(String name) {
		this.name = name;
		hash_value = name.hashCode() * 857;
	}

	public static PerlocutionaryValue create(String from){
		return new PerlocutionaryValue(from.toLowerCase());
	}

	@Override
	public int hashCode(){
		return hash_value;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other ->
				this.name.equals(other.name)
		);
	}
	@Override
	public String toString(){
		return name;
	}
}
