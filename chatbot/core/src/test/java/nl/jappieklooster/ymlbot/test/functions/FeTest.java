package nl.jappieklooster.ymlbot.test.functions;

import com.google.common.collect.Maps;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.values.PersonalValues;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.functions.general.GroupingTests;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.*;

public class FeTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Fe;
	}
	@Test
	public void making_utt_should_change_believes(){
		DialogueTree input = builder.getCurrent();
		believes = believes.addUtterance(
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.hellos),
				PerlocutionaryValueSet.create("Happy")
			)
		);
		JungFuncArgs result = apply(believes, input);
		believesShouldChange(result.believes);
	}
	@Test
	public void mirror_perc_value_of_previous_utt(){
		believes = believes.addUtterance(
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.hellos),
				PerlocutionaryValueSet.create("Happy")
			)
		);

		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);

		// expected because also happy
		final Utterance expectedUttLeftMost = builder.addOption(MockBelievesFactory.hellos);

		JungFuncArgs result = apply(believes, builder.getCurrent());

		// we learned happy is preffered
		believesShouldChange(result.believes);
		Assert.assertEquals("Happy should have value 1",
			1L,
			(long) result.believes.learnedValues.get(PerlocutionaryValue.create("Happy"))
			);

		// then we selected it
		Assert.assertEquals("we expect the agent to choose a happy hello",
			Optional.of(expectedUttLeftMost),
			result.tree.getPrefferedUtterance()
		);
	}
	@Test
	public void dont_change_believes_if_other_actor_dint_say_anything(){
		final List<Utterance> utts = Arrays.asList(
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.hellos),
				PerlocutionaryValueSet.create("Happy")
			),
			Utterance.create(
				MockBelievesFactory.actor_patient,
				builder.get(MockBelievesFactory.showdoor),
				PerlocutionaryValueSet.create("Angry")
			)
		);
		for(Utterance utt : utts){
			believes = believes.addUtterance(utt);
		}
		JungFuncArgs result = apply(believes, builder.getCurrent());
		believesShouldRemainTheSame(result.believes);
	}
	@Test
	public void mirror_perc_value_only_of_previous_utt(){

		final List<Utterance> utts = Arrays.asList(
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.hellos),
				PerlocutionaryValueSet.create("Happy")
			),
			Utterance.create(
				MockBelievesFactory.actor_patient,
				builder.get(MockBelievesFactory.needmedicine),
				PerlocutionaryValueSet.create("Persuading", "Scary")
			),
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.whyhere),
				PerlocutionaryValueSet.create("Angry")
			)
		);
		for(Utterance utt : utts){
			believes = believes.addUtterance(utt);
		}

		builder = builder.createWithRoot(builder.addOption(MockBelievesFactory.whyhere));
		builder.addOption(MockBelievesFactory.ilikevistingyou, MockBelievesFactory.actor_patient);
		// expect this because also angery
		Utterance expected = builder.addOption(MockBelievesFactory.maybeimsick);

		JungFuncArgs result = apply(believes, builder.getCurrent());

		believesShouldChange(result.believes);
		Assert.assertEquals("maybe im sick should be selected because its also angery (and only value learend)",
			Optional.of(expected),
			result.tree.getPrefferedUtterance()
		);
	}

	// parameterized one doesn't work
	@Test
	public void grouping_test(){
		GroupingTests test = new GroupingTests(JungianFunction.Fe);
		PersonalValues learnedValues = PersonalValues.empty;
		learnedValues = learnedValues.set(PerlocutionaryValue.create("happy"), 10);
		learnedValues = learnedValues.set(PerlocutionaryValue.create("scary"), 4);
		learnedValues = learnedValues.set(PerlocutionaryValue.create("angry"), 6);
		test.believes = believes.setLearnedValues(learnedValues);
		test.builder = builder;
		test.should_group_these_opts();
	}
	@Test
	public void ordered_test(){
		/**
		 * bigass integration test to make sure it sorts on right order
		 */

		DialogueTreeBuilder inputBuilder = DialogueTreeBuilder.copy(builder);

		Utterance doctor_happy = builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Happy"));
		Utterance doctor_angry = builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		builder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Scary", "Persuading"));
		DialogueTree expected = builder.getCurrent();

		inputBuilder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		inputBuilder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Scary", "Persuading"));
		inputBuilder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		inputBuilder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Happy"));
		inputBuilder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));

		List<Utterance> utts = Arrays.asList(doctor_happy, doctor_happy, doctor_angry);
		JungFuncArgs result = JungFuncArgs.create(believes, inputBuilder.getCurrent());
		for(Utterance utt : utts){
			result = JungFuncArgs.create(result.believes.addUtterance(utt), inputBuilder.getCurrent());
			result = apply(result);
			result = JungFuncArgs.create(result.believes.addUtterance(result.tree.getPrefferedUtterance().get()), result.tree);
		}

		believesShouldChange(result.believes);
		Assert.assertEquals("should be this precise order",
			expected,
			result.tree
		);

		Map<PerlocutionaryValue, Integer> values = Maps.newHashMap();
		values.put(PerlocutionaryValue.create("Happy"), 2);
		values.put(PerlocutionaryValue.create("Angry"), 1);
		Assert.assertEquals(
			"Should have assigned the following values",
			PersonalValues.create(values),
			result.believes.learnedValues
		);

	}
}
