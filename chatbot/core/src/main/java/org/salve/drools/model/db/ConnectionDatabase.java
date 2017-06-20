package org.salve.drools.model.db;

import org.javatuples.Pair;
import org.salve.drools.Functions;
import org.salve.drools.model.Actor;
import org.salve.drools.model.Connection;
import org.salve.drools.model.Symbol;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connections are build around symbol names and are therefore loosly coupled
 */
@Immutable
public class ConnectionDatabase extends Database<Symbol, Set<Connection>>{
	public static final ConnectionDatabase empty = new ConnectionDatabase(Collections.emptyMap());
	public ConnectionDatabase(Map<Symbol, Set<Connection>> connections) {
		super(connections);
	}


	/**
	 * Creates a new database where all connections from and to are flipped.
	 * @return
	 */
	public ConnectionDatabase createDual(){
		// the result of this could be stored lazily, and that result should
		// have the lazy result reference this
		return new ConnectionDatabase(Functions.streamToHashMapSet(
			this.entries().flatMap(entry ->
				entry.getValue()
					.stream()
					.map(connection -> new Pair<>(
							connection.to,
							connection.setTo(entry.getKey())
						)
					)
			),
			Pair::getValue0,
			Pair::getValue1
		));
	}

	/**
	 * Gets the connections, expends *any* connections into the provided set of actors
	 * @param name
	 * @return
	 */
	public Stream<Connection> getConnectionsExpand(Symbol name, Supplier<Stream<Actor>> actorsource){
		return getConnections(name)
			.flatMap(connection ->{
				if(connection.restricted_to.isAny){
					return actorsource.get().map(connection::setRestrictedTo);
				}
				return Stream.of(connection);
			});
	}

	public Stream<Connection> getConnections(Symbol name){
		return get(name).map(Set::stream).orElse(Stream.empty());
	}

	public Optional<Connection> getFromTo(Symbol from, Symbol to){
		return get(from)
			.map(list -> list.stream()
				.filter(x-> x.to.equals(to))
				.findFirst()
			)
			.flatMap(Function.identity());
	}

	public ConnectionDatabase putInCopy(Symbol symbol, Connection... connections){
		final Map<Symbol, Set<Connection>> mutdb = entries().collect(
			Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue)
		);
		final Set<Connection> existing = mutdb.containsKey(symbol) ? mutdb.get(symbol) : new HashSet<>();
		existing.addAll(Arrays.asList(connections));
		mutdb.put(symbol, existing);
		return new ConnectionDatabase(mutdb);
	}
}

