package nl.jappieklooster.ymlbot.test;

import org.salve.drools.model.Actor;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.personality.AlternateOnEqual;
import org.salve.personality.JungFuncArgs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecursiveExpectationBuilder implements Consumer<DialogueTreeBuilder> {
	private final int curdepth;
	private final int targetDepth;
	private final int targetBreath;
	public RecursiveExpectationBuilder(int targetBreath, int targetDepth, int curdepth) {
		this.curdepth = curdepth;
		this.targetDepth = targetDepth;
		this.targetBreath = targetBreath;
	}
	@Override
	public void accept(DialogueTreeBuilder dialogueTreeBuilder) {
		if(curdepth >= targetDepth){
			// base case
			return;
		}
		// flip actor each ply
		final Actor actor = (curdepth % 2) == 0 ? MockBelievesFactory.actor_patient : MockBelievesFactory.actor_doctor;

		for(int i = 0; i < targetBreath; i++){
			dialogueTreeBuilder.addOptionAndWith(
				MockBelievesFactory.hellos,
				actor,
				// go deeper
				new RecursiveExpectationBuilder(targetBreath, targetDepth, curdepth+1),
				PerlocutionaryValueSet.create("Happy"));
		}
	}

	/**
	 * A mocking function so that we don't have to use other jungian functions
	 * to test ni.
	 *
	 * This function will sort the symbols by name.
	 * Which conveniently causes ni to generate a hellos loop
	 * @param argumetns
	 * @return
	 */
	public static JungFuncArgs mock_function_sort_opts_alphabetacly(JungFuncArgs argumetns){
		DialogueTree tree = argumetns.tree;

		List<DialogueTree> sortedOpts = tree.getOptions().sorted(
			new AlternateOnEqual(tree, (one, two) -> one.utterance.getWhat().name.compareTo(two.utterance.getWhat().name))
		).collect(Collectors.toList());

		DialogueTree result = tree.replaceOptions(
			sortedOpts
		);
		return argumetns.setTree(result);
	}
}

