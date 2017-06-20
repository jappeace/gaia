package org.salve.drools.model;

import org.salve.drools.Functions;
import org.salve.drools.model.template.db.QueryDatabase;
import org.salve.drools.model.values.PerlocutionaryValueSet;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A pointer to another symbol with aditional information
 */
@Immutable
public class Connection {
	public final Optional<Before> before;
	/**
	 * Name of where the connection goes to
	 */
	public final Symbol to;
	/**
	 * Sometimes certain connections don't make sense,
	 * For example a patient asking a doctor what the
	 * problem is isn't desirable.
	 */
	public final Actor restricted_to;

	/**
	 * What expected emotions or mental effects does this connection have on
	 * the other party.
	 */
	public final PerlocutionaryValueSet values;
	public final QueryDatabase queries;

	public Connection(Optional<Before> before, Symbol to, Actor restricted_to, PerlocutionaryValueSet values, QueryDatabase queries) {
		this.before = before;
		this.to = to;
		this.restricted_to = restricted_to;
		this.values = values;
		this.queries = queries;
		this.hash_code = () -> {
			final int result =
				to.hashCode() * 3 +
				restricted_to.hashCode() * 151 +
				values.hashCode() * 199 -
				queries.hashCode() * 599;

			this.hash_code = () -> result;
			return result;
		};
	}
	public static Connection create(Symbol to, Actor restricted_to, PerlocutionaryValueSet values){
		return new Connection(Optional.empty(), to, restricted_to, values, QueryDatabase.empty);
	}

	private Supplier<Integer> hash_code;
	@Override
	public int hashCode(){
		return hash_code.get();
	}

	@Override
	public String toString(){
		return restricted_to + "->" + to;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other -> this.to.equals(other.to) &&
				this.restricted_to.equals(other.restricted_to) &&
				this.values.equals(other.values) &&
				this.queries.equals(other.queries)
		);
	}

	public Connection setRestrictedTo(Actor toActor){
		return new Connection(before, to, toActor, values, queries);
	}
	public Connection setTo(Symbol to){
		return new Connection(before, to, restricted_to, values, queries);
	}
}
