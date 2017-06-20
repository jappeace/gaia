package org.salve.drools.model.values;

import com.google.common.collect.Sets;
import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO remove itterable (I think it allows mutation of the set)
@Immutable
public class PerlocutionaryValueSet implements Iterable<PerlocutionaryValue>{
	// because PerlocutionaryValueSet is immutable we can just expose these
	public static final PerlocutionaryValueSet empty = new PerlocutionaryValueSet(Sets.newHashSet());
	public static final PerlocutionaryValueSet confused = create("confused");

	private final Set<PerlocutionaryValue> values;
	private final int hash_value;

	private PerlocutionaryValueSet(Set<PerlocutionaryValue> values) {
		this.values = values;
		hash_value = this.values.hashCode();
	}

	@Override
	public int hashCode(){
		return hash_value;
	}

	public static PerlocutionaryValueSet create(Set<PerlocutionaryValue> from){
		return new PerlocutionaryValueSet(new HashSet<>(from));
	}
	public static PerlocutionaryValueSet create(PerlocutionaryValue... from){
		return create(
			Arrays.stream(from).collect(Collectors.toSet())
		);
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj, other ->
			this.values.equals(other.values)
		);
	}
	public static PerlocutionaryValueSet create(String... from){
		return create(
			Arrays.stream(from)
			.map(PerlocutionaryValue::create)
				.collect(Collectors.toSet())
		);
	}

	public Stream<PerlocutionaryValue> getValues(){
		return values.stream();
	}

	@Override
	public Iterator<PerlocutionaryValue> iterator() {
		return values.iterator();
	}

	@Override
	public String toString(){
		return this.values.toString();
	}
}
