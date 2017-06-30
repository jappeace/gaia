package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.Set;
import java.util.stream.Collectors;

public class SeTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Se;
	}

	@Test
	public void se_generates_all_available_options(){
		final DialogueTree input = builder.getCurrent();
		// okay I guess the patient would say that?
		builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Happy"));
		builder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Persuading", "Scary"));
		builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Angry"));
		builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Happy"));
		builder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Persuading", "Scary"));
		builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
		final Set<DialogueTree> expected = builder.getCurrent().getOptions().collect(Collectors.toSet());
		JungFuncArgs actual = apply(believes, input);
		test(expected, actual);
	}

	@Test
	public void se_generates_next_ply(){
		// okay I guess the patient would say that?
		Utterance expectSelect = builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_patient);
		builder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_patient);
		builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient);

		final DialogueTree input = builder.getCurrent();

		builder.withOption(expectSelect, deepBuilder -> {
			//TODO this test shouldn't break if this order changes
			deepBuilder.addOption(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Enlightening"));
			deepBuilder.addOption(MockBelievesFactory.maybeimsick, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Angry"));
			deepBuilder.addOption(MockBelievesFactory.imthedoctor, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("Angry"));
			deepBuilder.addOption(MockBelievesFactory.ilikevistingyou, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Happy"));
		});
		final Set<DialogueTree> expected = builder.getCurrent().getOptions().collect(Collectors.toSet());

		JungFuncArgs actual = apply(believes, input);
		test(expected, actual);
	}

	private void test(Set<DialogueTree> expectedTrees, JungFuncArgs actual){
		believesShouldRemainTheSame(actual.believes);

		final Set<DialogueTree> result = actual.tree.getOptions().collect(Collectors.toSet());
		System.out.println(result);
		System.out.println(expectedTrees);

		Assert.assertEquals("Se should find all options in any order, but nothing more",
			expectedTrees,
			result
		);
	}

}
