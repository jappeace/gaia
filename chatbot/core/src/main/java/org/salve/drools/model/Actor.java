package org.salve.drools.model;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.function.Function;

/**
 * Provides actor semantics, where the any wildcard can be used for any actor
 * and always equal the other actor.
 * for example:
 *
 * any = patient
 * patient = any
 * patient = patient
 *
 * This is a very crude implementation and probably should be replaced by more
 * fine grained group controls.
 * However having all relevant places be marked by this class should make
 * refactoring a bit more easy (rather than the orignal which was string)
 */
@Immutable
public class Actor {
	public final String name;
	public static final String anyName = "any";
	public static final Actor any = new Actor(anyName);
	private final int hash_value;
	/**
	 * provides another shortcutting layer which is impossible to bypass
	 * ALso allows detection of the wildcard, which can be usefull
	 * (for example to expend any connections into their respective possible actors)
	 */
	public final boolean isAny;
	private Actor(String name) {
		this.name = name;
		hash_value = name.hashCode() * 349;
		isAny = anyName.equals(name);
	}

	@Override
	public int hashCode(){
		return hash_value;
	}

	public static final Actor create(String name){
		if(name.equals(anyName)){
			// this allows equal to shortcut on identity check
			// however this is bypassable with reflection
			return any;
		}
		return new Actor(name);
	}

	@Override
	public boolean equals(Object obj){
		if(isAny){
			return true;
		}
		if(obj == any){
			return true;
		}
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other -> {
			if(other.isAny){ // in case of externally defined anyname (the static isn't used)
				return true;
			}
			return this.name.equals(other.name);
			}
		);
	}
	@Override
	public String toString(){
		return isAny ? "AnyActor" : "Actor(" + name + ")";
	}
}
