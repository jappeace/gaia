package org.salve.personality.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.salve.drools.Functions;
import org.salve.drools.model.Symbol;
import org.salve.personality.JungianFunction;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Personality{
	private final List<JungianFunction> functionOrder;
	public static final Personality empty = new Personality(Collections.emptyList());

	private final int hash_value;
	public Personality(List<JungianFunction> functionOrder) {
		this.functionOrder = Lists.newArrayList(functionOrder);
		hash_value = functionOrder.hashCode();
	}

	public static Personality create(JungianFunction... from){
		return new Personality(Arrays.asList(from));
	}
	public OperationHeightCalculator curryCalculateOperationHeight(JungianFunction function){
		return (tree) -> calculateOperationHeight(function, tree);
	}

	public int calculateOperationHeight(JungianFunction function, DialogueTree tree){
		if(functionOrder.isEmpty()){
			return function.isRational ? 1 : 0;
		}

		// Ti, Se, Ne, Fe
		List<JungianFunction> other = Lists.newArrayList(functionOrder);
		// Ti, Se, Ne, Fe, Ti
		other.add(other.get(0));
		// Se, Ne, Fe, Ti
		other.remove(0);

		// Se, Ne, Fe, Ti
		// Ti, Se, Ne, Fe
		Stream<Pair<JungianFunction, JungianFunction>> funcs = Streams.zip(functionOrder.stream(), other.stream(), Pair::new);
		// [[Ti, Se], [Se, Ne], [Ne, Fe], [Fe, Ti]]

		Stream<Triplet<Long, JungianFunction, JungianFunction>> pairedfuncs =
		Streams.mapWithIndex(funcs, (pair, index) -> new Triplet<>(index, pair.getValue0(), pair.getValue1()))
		// [[Ti, Se], +[Se, Ne]+, [Ne, Fe], +[Fe, Ti]+]
			.filter(x -> Functions.isEven(x.getValue0()));

		Optional<Pair<Integer, Boolean>> pairDepth = Streams.mapWithIndex(pairedfuncs, Triplet::setAt0)
		// select the triplet that contains the function
		.filter(triplet -> (triplet.getValue1() == function) || (triplet.getValue2() == function))
		.findAny() // if any
		// get the depth of the pair and figure out of function is first
		.map(triplet -> new Pair<>(triplet.getValue0().intValue(), triplet.getValue1().isRational)) ;

		Optional<Integer> result = pairDepth.map(pair ->{
			int modifier = 0;
			if(pair.getValue1()){
				 modifier = function.isRational ? 0 : 1;
			}
			return pair.getValue0() + modifier;
			}
		);

		return Math.max(0, tree.height - result.orElse(0));
	}

	@Override
	public int hashCode(){
		return hash_value;
	}
	@Override
	public String toString(){
		return functionOrder.toString();
	}
	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,  other ->
			this.functionOrder.equals(other.functionOrder)
		);
	}

	public Stream<JungianFunction> stream(){
		return functionOrder.stream();
	}
	public List<JungianFunction> copyToList(){
		return Lists.newArrayList(functionOrder);
	}
	public List<JungianFunction> dropUntill(JungianFunction func){
		Iterator<JungianFunction> it = functionOrder.iterator();
		while(it.hasNext() && !func.equals(it.next()));
		return Lists.newArrayList(it);
	}
}

