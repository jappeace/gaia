package org.salve.drools.model;

import org.salve.drools.Functions;
import org.salve.drools.model.template.db.CapturedMatchDB;
import org.salve.drools.model.values.PerlocutionaryValueSet;

import javax.annotation.concurrent.Immutable;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Immutable utterance.
 *
 * An utterance is said by someone, with a meaning (symbol) and at a time
 */
@Immutable
public class Utterance {
	public final Informative informative;
	/**
	 * This is a utility when, recording when the utterance was created and
	 * thus the agent became 'aware' of it.
	 */
	public final Instant when; // immutable
	public final CapturedMatchDB capturedDB;

	/**
	 * The perlocutionary values this utterence caused
	 * (or we think are caused in another actor)
	 * currently this doesn't work for multiple agents
	 */
	public final PerlocutionaryValueSet perlocutionaryValues;

	private Supplier<Integer> lazyHashValue;

	public Utterance(Informative informative, PerlocutionaryValueSet perlocutionaryValues, CapturedMatchDB capturedDB) {
		this.informative = informative;
		this.capturedDB = capturedDB;
		this.perlocutionaryValues = perlocutionaryValues;
		this.when = Instant.now();

		lazyHashValue = () -> {
			// since the class is immutable and we don't deal with collections,
			// we can calulate this now, if it every is required...
			final int hash_code =
				311 * informative.hashCode() -
				193 * this.perlocutionaryValues.hashCode() +
				701 * capturedDB.hashCode();
			lazyHashValue = () -> hash_code;
			return hash_code;
		};
	}
	public static Utterance create(Actor byWhom, Symbol what, PerlocutionaryValueSet perlocutionaryValues, CapturedMatchDB capturedDB){
		return new Utterance(new Informative(byWhom, what), perlocutionaryValues, capturedDB);
	}
	public static Utterance create(Actor byWhom, Symbol what, PerlocutionaryValueSet perlocutionaryValues){
		return create(byWhom, what, perlocutionaryValues, CapturedMatchDB.empty);
	}
	public static Utterance createFromConnection(Connection connection){
		return createFromConnection(connection, CapturedMatchDB.empty);
	}
	public static Utterance createFromConnection(Connection connection, CapturedMatchDB db){
		return create(connection.restricted_to, connection.to, connection.values, db);
	}

	@Override
	public int hashCode(){
		return lazyHashValue.get();
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
		return Functions.equalsAs(this, obj).fold(Function.identity(),
		other ->
			this.informative.equals(other.informative) &&
			this.perlocutionaryValues.equals(other.perlocutionaryValues) &&
			this.capturedDB.equals(other.capturedDB)
		);
	}
	@Override
	public String toString(){
		return "utt("+ getByWhom() + ": " + getWhat().name + ", " + perlocutionaryValues + ", " + this.capturedDB + ")";
	}
	public Symbol getWhat(){
		return informative.what;
	}

	public Actor getByWhom() {
		return informative.who;
	}

	public Utterance setByWhom(Actor to){
		return new Utterance(informative.setActor(to), perlocutionaryValues, capturedDB);
	}
	public Utterance setPerlocutionaryValues(PerlocutionaryValueSet to){
		return new Utterance(informative, to, capturedDB);
	}
}
