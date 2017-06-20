package org.salve.drools.model.db;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Immutable encapsulation over a hashmap
 *
 * @param <Key>
 * @param <Value>
 */
@Immutable
public class Database<Key,Value> {
	private final HashMap<Key, Value> data;
	/**
	 * Constructing the string maybe expensive,
	 * however since we are immutable we can just do it once and remember it
	 */
	private Supplier<String> lazyStringRepresentation;
	private Supplier<Integer> lazyHashValue;

	public final boolean isEmpty;

	public Database(Map<Key, Value> data) {
		this.data = new HashMap<>(data);
		isEmpty = data.isEmpty();

		lazyHashValue = () -> {
			// requires iteration over the map, therefore lazy
			final int hash_value = this.data.hashCode() * 811 - 997;
			this.lazyHashValue = () -> hash_value;
			return hash_value;
		};

		// lazy string representation
		this.lazyStringRepresentation = () -> {
			final StringBuilder bob = new StringBuilder();
			bob.append("Database(");
			final char equals = ':';
			final String newline = String.format("%n");
			bob.append(newline);
			for (Map.Entry<Key, Value> keyvalue : data.entrySet()){
				bob.append(keyvalue.getKey());
				bob.append(equals);
				bob.append(keyvalue.getValue());
				bob.append(newline);
			}
			bob.append(")");
			final String result = bob.toString();
			lazyStringRepresentation = () -> result; // replace this lambda with the result
			return result;
		};
	}
	public Optional<Value> get(Key key){
		if(data.containsKey(key)){
			return Optional.of(data.get(key));
		}
		return Optional.empty();
	}

	/**
	 * Sometimes you jsut want the data, this calls options or else throws a
	 * (nicely formated) indexoutofbounds exception
	 * @param key
	 * @return
	 */
	public Value getOrThrow(Key key){
		return get(key).orElseThrow(() -> new IndexOutOfBoundsException("Could not find " + key + " in " + toString()));
	}

	public Stream<Key> keys(){
		return data.keySet().stream();
	}
	public Stream<Value> values(){
		return data.values().stream();
	}
	public Stream<Map.Entry<Key,Value>> entries(){
		return data.entrySet().stream();
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other -> this.data.equals(other.data)
		);
	}

	@Override
	public int hashCode(){
		return lazyHashValue.get();
	}

	@Override
	public String toString(){
		return lazyStringRepresentation.get();
	}

}
