package org.salve.drools.model;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.time.Instant;

/**
 * Something said by somebody, not sure when, or how the reactions where.
 */
@Immutable
public class Informative {
	public final Actor who;
	public final Symbol what; // immutable
	/**
	 * This is a utility when, recording when the utterance was created and
	 * thus the agent became 'aware' of it.
	 */
	private final int hash_code;
	private final String to_string;

	public Informative(Actor who, Symbol what) {
		this.who = who;
		this.what = what;

		// since the class is immutable and we don't deal with collections,
		// we can calulate this now
		hash_code =
			what.hashCode() * 7 +
			who.hashCode() * 11 - 3;
		to_string = who+ ": " + what.name;
	}

	@Override
	public int hashCode(){
		return hash_code;
	}

	/**
	 * Please note that we don't consider when as equality of an utterance.
	 * The reason for this is that identity should be used for that since
	 * its  basically assigned on consturction and immutable.
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other ->
				this.what.equals(other.what) &&
				this.who.equals(other.who)
		);
	}
	@Override
	public String toString(){
		return to_string;
	}
	public Informative setActor(Actor actor){
		return new Informative(actor, what);
	}
}
