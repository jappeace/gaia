package nl.jappieklooster.ymlbot.test.functions.general;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.functions.AJungFuncTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;
import static org.salve.personality.JungianFunction.*;

import java.util.Arrays;
import java.util.Optional;

@RunWith(Parameterized.class)
public class ThinkingTests extends AJungFuncTest{
	@Parameterized.Parameters(name = "{0}")
	public static Iterable<? extends Object> thinkers() {
		return Arrays.asList(Te, Ti);
	}
	JungianFunction underTest;

	public ThinkingTests(JungianFunction underTest){
		this.underTest = underTest;
	}
	@Override
	public JungianFunction getTestTarget() {
		return underTest;
	}

	@Test
	public void prioretise_goals_always(){
		final DialogueTreeBuilder inputBuilder = DialogueTreeBuilder.copy(builder);
		inputBuilder.addOption(MockBelievesFactory.whyhere);
		inputBuilder.addOption(MockBelievesFactory.hellos);
		final Utterance expectedUttLeftMost = inputBuilder.addOption(MockBelievesFactory.needmedicine);

		final DialogueTree input = inputBuilder.getCurrent();

		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.hellos);

		final DialogueTree expected = builder.getCurrent();

		JungFuncArgs results = apply(believes, input);

		believesShouldRemainTheSame(results.believes);

		Assert.assertEquals("needmedicne priotized the most",
			Optional.of(expectedUttLeftMost),
			results.tree.getPrefferedUtterance()
		);

		Assert.assertEquals("however whyhere was a secondary goal",
			expected,
			results.tree
		);
	}
}
