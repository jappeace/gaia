package org.salve.personality;

import com.google.common.collect.Lists;
import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.javatuples.Pair;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Short hand for typing the argumetns out each times, it also makes java
 * realize you can compose them (no auto expension pairs).
 *
 * Using this instead of Pair<Believes, DialogueTree> makes the typesingature
 * shorter and probably more readable.
 * It also opens up the posibility for easier changes.
 */
@Immutable
public class JungFuncArgs {
	public final Believes believes;
	public final DialogueTree tree;

	public final NextFunction next; // we assume this is immutable
	public interface NextFunction extends Supplier<Pair<JungFuncAccessor, NextFunction>> {}
	public static class UnitNextFunction implements NextFunction{
		private final Pair<JungFuncAccessor, NextFunction> result =
			new Pair<>(JungianFunction.Unit, this);
		public Pair<JungFuncAccessor, NextFunction> get(){
			return result;
		}
	}

	public JungFuncArgs(Believes believes, DialogueTree tree, NextFunction next) {
		this.believes = believes;
		this.tree = tree;
		this.next = next;
	}
	public static JungFuncArgs create(Believes one, DialogueTree two){
		return new JungFuncArgs(one, two, new UnitNextFunction());
	}
	public JungFuncArgs setBelieves(Believes to){
		return new JungFuncArgs(to, tree, next);
	}
	public JungFuncArgs setTree(DialogueTree to){
		return new JungFuncArgs(believes, to, next);
	}
	public JungFuncArgs set(Believes believes, DialogueTree tree){
		return new JungFuncArgs(believes, tree, next);
	}
	public JungFuncArgs insertNextFunc(JungFuncAccessor funcs){
		return insertNextFuncs(Arrays.asList(funcs));
	}
	public JungFuncArgs insertNextFuncs(List<JungFuncAccessor> funcs){
		// this should be a reduce (fold), but I don't get the combinator part of the
		// java8 api, its probably beause they put parralization into the same
		// api, but a fold isn't a parralel opperation
		JungFuncArgs result = this;

		for(JungFuncAccessor func : Lists.reverse(funcs)){ // is this the right order??
			final JungFuncArgs currentResult = result; // circumvnet we need to be final error
			result = new JungFuncArgs(believes, tree, ()-> new Pair<>(func, currentResult.next));
		}
		return result;
	}
	public JungFuncArgs applyNext(){
		Pair<JungFuncAccessor, NextFunction> nextstep = next.get();
		JungFuncArgs input = new JungFuncArgs(believes, tree, nextstep.getValue1());
		return nextstep.getValue0().getFunction().apply(input);
	}
}
