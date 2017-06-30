package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.Collections;
import java.util.Optional;

public class TeTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Te;
	}

	@Test
	public void no_goals_look_for_ones_that_change_scene(){
		Believes inputBelieves = believes.setGoals(Collections.emptySet());
		builder.addOption(MockBelievesFactory.hellos);
		Utterance expected = builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		DialogueTree inputTree = builder.getCurrent();

		JungFuncArgs result = apply(inputBelieves, inputTree);

		believes = inputBelieves;
		believesShouldRemainTheSame(result.believes);

		Assert.assertEquals("the scene changing dialogue is preffered",
			Optional.of(expected),
			result.tree.getPrefferedUtterance()
		);
	}
}
