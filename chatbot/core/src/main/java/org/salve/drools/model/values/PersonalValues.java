package org.salve.drools.model.values;

import com.google.common.collect.Maps;
import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.*;

// perhaps this would benifit from becoming a database?
@Immutable
public class PersonalValues implements Comparator<PerlocutionaryValueSet>{

	private final Map<PerlocutionaryValue, Integer> values;
	private final int default_value;
	public static final PersonalValues empty = create(Maps.newHashMap());

	public static PersonalValues create(Map<PerlocutionaryValue, Integer> uservals){
		return create(uservals, 0);
	}
	public static PersonalValues create(Map<PerlocutionaryValue, Integer> uservals, int default_value){
		return new PersonalValues(new HashMap<>(uservals), default_value);
	}
	private PersonalValues(Map<PerlocutionaryValue, Integer> to, int default_value){
		this.default_value = default_value;
		values = to;
	}

	@Override
	public int compare(PerlocutionaryValueSet left, PerlocutionaryValueSet right) {
		int leftcount = valuate(left);
		int rightcount = valuate(right);
		return rightcount - leftcount;
	}
	public boolean contains(PerlocutionaryValue toCheck){
		return values.containsKey(toCheck);
	}
	private static int add(int one , int two){
		return one + two;
	}
	public int valuate(PerlocutionaryValueSet values){
		return values
			.getValues()
			.map(this::get)
			.reduce(default_value, PersonalValues::add);
	}
	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other -> this.values.equals(other.values)
		);
	}

	public PersonalValues set(PerlocutionaryValue which, Integer to){
		Map<PerlocutionaryValue, Integer> copy = new HashMap<>(values);
		copy.put(which, to);
		return new PersonalValues(copy, default_value);
	}
	public PersonalValues increase(PerlocutionaryValue which){
		return set(which, get(which) + 1);
	}

	public Integer get(PerlocutionaryValue which){
		if(!values.containsKey(which)){
			return default_value;
		}
		return values.get(which);
	}
	@Override
	public String toString(){
		return "PersonalValues(" + values +")";
	}
}
