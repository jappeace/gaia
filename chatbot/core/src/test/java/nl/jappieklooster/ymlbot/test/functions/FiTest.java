package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

/**
 *
 */
public class FiTest extends AJungFuncTest {

	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Fi;
	}

	@Test
	public void fi_chagnes_input_to_preference(){
		final DialogueTreeBuilder inputBuilder = DialogueTreeBuilder.copy(builder);
		inputBuilder.addOption(MockBelievesFactory.needmedicine);
		inputBuilder.addOption(MockBelievesFactory.whyhere);
		final Utterance expectedUttLeftMost = inputBuilder.addOption(MockBelievesFactory.hellos);

		final DialogueTree input = inputBuilder.getCurrent();

		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);

		final DialogueTree expected = builder.getCurrent();

		// preparing the input
		final JungFuncArgs result = apply(believes, input);
		test(expected, expectedUttLeftMost, result);
	}

	@Test
	public void fi_chagnes_input_to_preference_on_the_right_level(){
		final DialogueTreeBuilder inputBuilder = DialogueTreeBuilder.copy(builder);

		Utterance tomodify = inputBuilder.addOption(MockBelievesFactory.needmedicine);
		inputBuilder.addOption(MockBelievesFactory.whyhere);
		inputBuilder.addOption(MockBelievesFactory.hellos);

		inputBuilder.withOption(tomodify, x -> {
			x.addOption(MockBelievesFactory.areyousick, MockBelievesFactory.actor_doctor);
			x.addOption(MockBelievesFactory.havemedicine, MockBelievesFactory.actor_doctor);
		});

		final DialogueTree input = inputBuilder.getCurrent();

		final Utterance expectModified = builder.addOption(MockBelievesFactory.needmedicine);
		builder.withOption(expectModified, x -> {
			x.addOption(MockBelievesFactory.havemedicine, MockBelievesFactory.actor_doctor);
			x.addOption(MockBelievesFactory.areyousick, MockBelievesFactory.actor_doctor);
		});
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.hellos);


		final DialogueTree expected = builder.getCurrent();

		// preparing the input
		final JungFuncArgs result = apply(believes, input);

		believesShouldRemainTheSame(result.believes);
		Assert.assertEquals("should modify the lowest option layer",
			expected,
			result.tree
		);
	}

	@Test
	public void fi_does_not_change_uneccisarly(){
		final DialogueTreeBuilder inputBuilder = DialogueTreeBuilder.copy(builder);
		final Utterance expectedUttLeftMost = inputBuilder.addOption(MockBelievesFactory.hellos);
		inputBuilder.addOption(MockBelievesFactory.needmedicine);
		inputBuilder.addOption(MockBelievesFactory.whyhere);

		final DialogueTree input = inputBuilder.getCurrent();

		builder.addOption(MockBelievesFactory.hellos);
		Utterance need = builder.addOption(MockBelievesFactory.needmedicine);
		Utterance why = builder.addOption(MockBelievesFactory.whyhere);

		final DialogueTree expected = builder.getCurrent();

		// preparing the input
		final JungFuncArgs result = apply(believes, input);

		test(expected, expectedUttLeftMost, result);
	}

	private void test(DialogueTree expectedTree, Utterance leftMostExpected, JungFuncArgs actual){
		believesShouldRemainTheSame(actual.believes);

		Assert.assertEquals(
			"Left most utterence should be goodbye",
			leftMostExpected, actual.tree.getPrefferedUtterance().get()
		);
		Assert.assertEquals(
			"the output dialogue tree should equal expected.",
			expectedTree, actual.tree
		);
	}

}
