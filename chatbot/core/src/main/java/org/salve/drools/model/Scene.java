package org.salve.drools.model;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.function.Function;

/**
 * This is the folder name basically, wrapped in a type for calrity
 */
@Immutable
public class Scene {
	public final String name;

	private final int hash_value;
	public Scene(String name) {
		this.name = name;
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
		return "Scene("+name+")";
	}
}
