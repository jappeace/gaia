package org.salve.personality;

import org.salve.drools.model.*;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.values.PersonalValues;
import org.javatuples.Pair;
import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.personality.model.OperationHeightCalculator;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Carl Jung's functions
 */
@Immutable
public enum JungianFunction implements JungFuncAccessor{
	Se(JungianFunction::extrovertedSensing, false),
	Si(JungianFunction::introvertedSensing, false),
	Ne(new Ne(0), false),
	Ni(new Ni(0), false),
	Te(JungianFunction::extrovertedThinking, true),
	Ti(JungianFunction::introvertedThinking, true),
	Fe(JungianFunction::extrovertedFeeling, true),
	Fi(JungianFunction::introvertedFeeling, true),
	Unit(Function.identity(), false);

	private final Function<JungFuncArgs, JungFuncArgs> function;
	public final boolean isRational;

	JungianFunction(Function<JungFuncArgs, JungFuncArgs> function, boolean isRational) {
		this.function = function;
		this.isRational = isRational;
	}

	@Override
	public Function<JungFuncArgs, JungFuncArgs> getFunction(){
		return function;
	}

	/**
	 * Both feeling functions $F$ use the perlocutionary acts to order the children.
	 * $F_i$ uses a predefined value set $h$:
	 * \[ \pi \overset{h}{\to} i \].
	 * This valuation is done by a lookup table on all avalaible perlocutionary speech
	 * acts.
	 * @param args
	 * @return
	 */
	private static JungFuncArgs introvertedFeeling(JungFuncArgs args){
		// Alternativly to this implementation we could use the scene's to which
		// goals are available and try to find those
		final Believes believes = args.believes;
		final PersonalValues values = believes.values;

		final DialogueTree result = sortWithValues(
			believes.personality.curryCalculateOperationHeight(Fi),
			args.tree,
			values
		);

		return args.setTree(result);
	}
	private static DialogueTree sortWithValues(
		OperationHeightCalculator calculator,
		DialogueTree currentTree,
		PersonalValues values
	){
		return currentTree.copyWithStartAtUntilLeaf(
			calculator.apply(currentTree),
			tree -> {
			// sort the options based upon personal value judgments

			final List<DialogueTree> sorted = tree.getOptions().sorted(
				new AlternateOnEqual(tree,
					(a, b) ->
					values.compare(a.connectionUsed.values, b.connectionUsed.values)
				)
			).collect(Collectors.toList());
			return tree.replaceOptions(sorted);
		});
	}

	/**
	 * $F_e$ tries to figure out what the conversation partners values by
	 * picking the perlocutionary act the other chose most.
	 * This is done by simply keeping track on how many of such speech acts the
	 * partner uttered and picking the that has been uttered most, if that one is not
	 * available we move to the next one. This is similar to fictitious play.
	 * @param args
	 * @return
	 */
	private static JungFuncArgs extrovertedFeeling(JungFuncArgs args){

		final Believes believes = args.believes;

		PerlocutionaryValueSet uttered = believes.getUtterancesReversed()
			.findFirst()
			.filter(x->!x.getByWhom().equals(believes.self))
			.map(x->x.perlocutionaryValues)
			.orElse(PerlocutionaryValueSet.empty);

		// this is technically another fold operation, but again,
		// java can't do that well
		PersonalValues learned = args.believes.learnedValues;
		for(PerlocutionaryValue value : uttered){
			learned = learned.increase(value);
		}
		final Believes believesWithLearned = believes.setLearnedValues(learned);

		DialogueTree result = sortWithValues(
			believes.personality.curryCalculateOperationHeight(Fe),
			args.tree,
			believesWithLearned.learnedValues
		);

		return args.set(believesWithLearned, result);
	}

	/**
	 * To model $T_i$ however the most obvious solution would be to implement an
	 * axiomatic logic system.
	 * This is however rather heavy on maintenance.
	 * Every agent would need to have their own axiomatic system to determine what to
	 * do for each node in the symbol graph.
	 * The only real solution would be to create this dynamically somehow,
	 * but this is out of scope of this thesis.
	 * Therefore we looked for an alternative.
*
	 * $T_i$ however uses a different strategy. It wants to help the conversation
	 * partner to analyze the problem according to the partner's own internal
	 * logic framework
	 * and to do this it wants to give as much options as possible to the partner.
	 * Therefore it will choose the speech acts that produce the most symbols for the
	 * partner.
	 * To do this it will sort the child nodes according to as much unique symbols as
	 * possible.
	 * @param args
	 * @return
	 */
	private static JungFuncArgs introvertedThinking(JungFuncArgs args){
		final Believes believes = args.believes;
		final DialogueTree tree = args.tree;

		// sorting may ask the same result several times,
		// (in fact log n extra times).
		// we will store the results of next function in cache rather than
		// recomputing (we assume that calculating next will be expensive)
		final Map<DialogueTree, Integer> nextCache = new HashMap<>();
		DialogueTree result = sortOnGoalsOrWith(
			believes.personality.curryCalculateOperationHeight(Ti),
			believes, tree, (above, pair) -> {
			int one = dealWithCache(nextCache, args, pair.getValue0());
			int two = dealWithCache(nextCache, args, pair.getValue1());
			return one - two;
		});
		return args.setTree(result);
	}
	private static int dealWithCache(Map<DialogueTree, Integer> nextCache,  JungFuncArgs args, DialogueTree tree){
		if(!nextCache.containsKey(tree)) {

			DialogueTree oneOption = tree.replaceOptions(Collections.emptyList());
			JungFuncArgs applied = args.setTree(oneOption).applyNext();
			int result = (int) applied.tree.getOptions().count();
			// put the result in cache
			nextCache.put(tree,
				result
			);
		}
		return nextCache.get(tree);
	}

	/**
	 *
	 * To start we will describe $T_e$. It sees the problem to solve as the
	 * conversation itself.
	 * Therefore it will consistently choose speech acts that could help the partner to
	 * progress the scenario.
	 * So it wants to put the partner in a position where he has
	 * almost no other options except to progress the scenario
	 * If it encounteres a child node  with a goal in it it will give priority to that.
	 * The higher the goal is the more priority.
	 * The information about scenario progression is encoded in the drools itself,
	 * so this could potentially be acquired by using reflection on the drools.
	 *
	 * // instead of reflectino I think we can just use the scene connection
	 * info, then see were they lead and if there are any goals in those scne
	 * (and how they compare)
	 * From that we can preffer options that 'force' the doctor to go into
	 * direction of the goal (at least according to the connections).
	 *
	 * @param args
	 * @return
	 */
	private static JungFuncArgs extrovertedThinking(JungFuncArgs args){
		final Believes believes = args.believes;
		final DialogueTree tree = args.tree;

		DialogueTree result = sortOnGoalsOrWith(
			believes.personality.curryCalculateOperationHeight(Te),
			believes, tree, (target, pair) -> {
			final DialogueTree one = pair.getValue0();
			final DialogueTree two = pair.getValue1();

			//this heuristic relies on the assumption that the dialogue 'flow'
			// behaves more like a tree than a graph.
			// However if this doesn't work we can consider recording goals per
			// scene beforehand and then allow
			// else check if one of them changes scene
			if(one.getScene().equals(two.getScene())){
				// of course not if they're equal
				return Compare.equal;
			}
			Scene targetScene = target.getScene();
			if(!one.getScene().equals(targetScene)){
				if(two.getScene().equals(targetScene)){
					return Compare.onepreffered;
				}
			}else{
				if(!two.getScene().equals(targetScene)){
					return Compare.twopreffered;
				}
			}
			return Compare.equal;
		} );
		return args.setTree(result);
	}

	private static DialogueTree sortOnGoalsOrWith(
		OperationHeightCalculator calculator,
		Believes believes,
		DialogueTree treeTarget,
		BiFunction<DialogueTree, Pair<DialogueTree, DialogueTree>, Integer> with
	){

		return treeTarget.copyWithStartAtUntilLeaf(calculator.apply(treeTarget), target ->
			target.replaceOptions(
				target.getOptions().sorted(
					new AlternateOnEqual(target,
					(one, two) -> {
						Optional<Goal> goalone = believes.goals.stream().filter(goal -> goal.isGoal(one.utterance)).findAny();
						Optional<Goal> goaltwo = believes.goals.stream().filter(goal -> goal.isGoal(two.utterance)).findAny();

						// if they're both goals, use utility
						return -goalone.flatMap(g1 -> goaltwo.map(g1::compareTo)).orElseGet(()->{
							// else if one of them is a goal preffer that
							if(goalone.isPresent()){
								return Compare.onepreffered;
							}
							if(goaltwo.isPresent()){
								return Compare.twopreffered;
							}
							return with.apply(target, new Pair<>(one, two));
						});
					})
				).collect(Collectors.toList())
			)
		);
	}
	private static int scenesDontEqual(DialogueTree one, DialogueTree two){
		if(!one.utterance.getWhat().scene.equals(two.utterance.getWhat().scene)){
			return 1;
		}
		return 0;
	}

	/**
	 * The $S_e$ function just receives all possible connections from the current
	 * meaning for several plies and then applies the /next/ function on it.
	 *
	 * @param args
	 * @return
	 */
	private static JungFuncArgs extrovertedSensing(JungFuncArgs args) {
		final Believes believes = args.believes;
		final DialogueTree tree = args.tree;

		final int height = believes.personality.calculateOperationHeight(Se, tree);
		DialogueTree result = tree.copyWithAboveLeftMostLeaf(height, targetTree -> {
			final Utterance utt = targetTree.utterance;
			// find *new* options to add
			final Stream<DialogueTree> found = believes.findProgrammedOptions(utt);

			// replace the target tree's options (in a copy)
			return targetTree.appendOptions(found);
		});
		return args.setTree(result);
	}

	/**
	 * The $S_i$ however is more conservative and will only pop $x$ random meanings by
	 * default (the first $x$ connections),
	 * however it will construct its own connections of whatever the user said in
	 * response to the bot from previous conversations when at the same meaning (if it
	 * didn't exists already).
	 * Whenever these connections are available they will substitute the random $x$.
	 * So $S_i$ starts of kind off similar to $S_e$ but builds up over time.
	 *
	 * @param args
	 * @return
	 */
	private static JungFuncArgs introvertedSensing(JungFuncArgs args) {
		final int height = args.believes.personality.calculateOperationHeight(Si, args.tree);
		// Update our own adminstration from last reply
		DialogueTree currentPlie = args.tree.getLeftMostTreeAt(height);
		List<Utterance> replies = args.believes
			.getUtterancesReversed()
			.collect(Collectors.toList());
		Believes believes = args.believes.setLearnedConnections(
			updateAdministration(
				args.believes.learnedConnections,
				replies,
				currentPlie.utterance.getByWhom()
			)
		);

		// do the figure out who said what
		final Utterance utt = currentPlie.utterance;

		// check if current reply is in own adminstration.
		final Stream<Connection> found = believes.learnedConnections.getConnectionsExpand(utt.getWhat(), believes::getActors);
		List<DialogueTree> trees = found.map(DialogueTree::createFromConnection).collect(Collectors.toList());
		if(!trees.isEmpty()){
			// if so we add as option our reply we used there
			DialogueTree result = args.tree.copyWithAboveLeftMostLeaf(height,
				x->x.appendOptions(trees.stream())
			);
			return args.set(believes, result);
		}

		// if not we just call SE on it


		DialogueTree preffered = args.tree.copyWithAboveLeftMostLeaf(height,
			tree ->
				believes.findProgrammedOptions(tree.utterance)
				.findAny()
				.map(x -> tree
					.appendOptions( // remove all options in favor of selected
						Arrays.asList(x).stream()
					)
				).orElse(tree)
			);
		// now lets just reduce the options
		return args.set(believes, preffered);
	}

	public static ConnectionDatabase updateAdministration(ConnectionDatabase current, List<Utterance> memory, Actor actorMe){
		return memory.stream().filter(x-> x.getByWhom().equals(actorMe)).findFirst().flatMap(isaid ->
			// note the !
			memory.stream().filter(x->!x.getByWhom().equals(actorMe)).findFirst().map(
				theysaid->
				theysaid.when.isBefore(isaid.when) ? // conditional
				// i reply to them
				current.putInCopy(
					theysaid.getWhat(),
					Connection.create(
						isaid.getWhat(),
						isaid.getByWhom(),
						isaid.perlocutionaryValues
					)
				)
				: // they reply to me
				current.putInCopy(
					isaid.getWhat(),
					Connection.create(
						theysaid.getWhat(),
						theysaid.getByWhom(),
						theysaid.perlocutionaryValues
					)
				)
			)
		).orElse(current);
	}
}
