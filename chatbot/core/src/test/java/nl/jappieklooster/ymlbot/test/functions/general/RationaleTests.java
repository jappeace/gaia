package nl.jappieklooster.ymlbot.test.functions.general;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.functions.AJungFuncTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.Personality;

import java.util.*;
import java.util.stream.Collectors;

import static org.salve.personality.JungianFunction.*;
import static nl.jappieklooster.ymlbot.test.MockBelievesFactory.*;

/**
 * This class contains some general contracts for rationale functions,
 * think of they shouldn't generate options and they should change order
 */
@RunWith(Parameterized.class)
public class RationaleTests extends AJungFuncTest {

	@Parameters(name = "{0}")
	public static Iterable<? extends Object> learners() {
		return Arrays.asList(Fi, Fe, Te, Ti);
	}
	JungianFunction underTest;
	public RationaleTests(JungianFunction underTest){
		this.underTest = underTest;
	}
	@Override
	public JungianFunction getTestTarget() {
		return underTest;
	}

	/**
	 * this seems kindoff dumb but an important property to have for rational
	 */
	@Test
	public void rationale_with_no_opts_should_do_nothing(){
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(JungFuncArgs.create(getBelieves(), input));
		Assert.assertEquals("there was nothing to modify", input, results.tree);
	}

	// TODO make a recursive variant
	@Test
	public void rationale_preffers_alternation(){
		// note that builder by default lets the doctor utter first
		builder.addOption(hellos, actor_doctor);
		builder.addOption(hellos, actor_doctor);
		builder.addOption(hellos, actor_doctor);
		Utterance expected = builder.addOption(hellos, actor_patient);
		builder.addOption(hellos, actor_doctor);
		builder.addOption(hellos, actor_doctor);

		JungFuncArgs result = apply(getBelieves(), builder.getCurrent());

		Assert.assertEquals("Should select the patient", Optional.of(expected), result.tree.getPrefferedUtterance());
	}

	/**
	 * Java has a rule that enclosed variables should be final.
	 * the rule is pointless since final doesn't mean constant in Java.
	 */
	class LamdaHack<T>{
		T value;
	}

	@Test
	public void rationale_preffers_recursive_alternation(){
		// note that builder by default lets the doctor utter first
		final LamdaHack<Utterance> hack = new LamdaHack<>();
		builder.addOption(hellos, actor_doctor);
		builder.addOptionAndWith(hellos, actor_patient, deeper -> {
			deeper.addOption(hellos, actor_patient);
			deeper.addOption(hellos, actor_patient);
			hack.value = deeper.addOption(hellos, actor_doctor, PerlocutionaryValueSet.create("Joyfull"));
			deeper.addOption(hellos, actor_patient);
		});
		Utterance expected = hack.value;
		builder.addOption(hellos, actor_doctor);
		builder.addOption(hellos, actor_doctor);

		JungFuncArgs result = apply(getBelieves(), builder.getCurrent());

		System.out.println("result: " + result.tree);
		Assert.assertEquals("Should select the doctor recursivly",
			Optional.of(expected),
			result.tree.getPrefferdOption().flatMap(DialogueTree::getPrefferedUtterance)
		);
	}

	@Test
	public void rationale_should_only_change_low_level_order(){
		DialogueTreeBuilder builder = getBuilder();
		// modify the level it should operate upon
		setPersonality(Personality.create(Unit, Unit, getTestTarget()));

		builder.addOptionAndWith(whyhere, actor_any, changeThis -> {
			changeThis.addOption(needmedicine, actor_patient, PerlocutionaryValueSet.create("Enlightening"));
			changeThis.addOption(imthedoctor, actor_doctor, PerlocutionaryValueSet.create("Angry"));
			changeThis.addOption(maybeimsick, actor_patient, PerlocutionaryValueSet.create("Angry"));
			changeThis.addOption(ilikevistingyou, actor_patient, PerlocutionaryValueSet.create("Happy"));
		});
		builder.addOption(needmedicine);
		builder.addOption(whyhere);
		builder.addOption(needmedicine);
		builder.addOption(hellos);
		builder.addOption(needmedicine);
		builder.addOption(hellos);
		builder.addOption(whyhere);
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(JungFuncArgs.create(getBelieves(), input));

		List<DialogueTree> expected = input
			.getOptions()
			.map(x->x.replaceOptions(Collections.emptyList())) // prune the result
			.collect(Collectors.toList());

		List<DialogueTree> actual = results.tree
			.getOptions()
			.map(x->x.replaceOptions(Collections.emptyList())) // prune the result
			.collect(Collectors.toList());
		Assert.assertEquals("this order shouldn't have changed", expected, actual);

		Assert.assertEquals("The (unsorterted) preffered option should " +
				"have 4 nodes (no nodes get eaten by rationale functions)",
			results.tree.getPrefferdOption().map(opt -> opt.getOptions().count()).orElse(0L),
			(Long) 4L
		);
	}
}
