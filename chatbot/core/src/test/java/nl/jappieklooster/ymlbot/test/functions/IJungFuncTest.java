package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import org.junit.Assert;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

/**
 * At some point I wanted to make RationalFUnc test and irattionalefunctest be
 * like traits with default implementations, but junit 4 doesn't support that.
 * Junit 5 will however and once that becomes stable you should do that
 * (it makes inherticance flat).
 */
public interface IJungFuncTest {
	JungianFunction getTestTarget();
	DialogueTreeBuilder getBuilder();
	Believes getBelieves();
	default void believesShouldRemainTheSame(Believes actual){
		Assert.assertEquals(
			"believes should remain the same for " + getTestTarget().name(),
			getBelieves(), actual
		);
	}
	default void believesShouldChange(Believes actual){
		Assert.assertNotEquals(
			"believes should change for " + getTestTarget().name(),
			getBelieves(), actual
		);
	}
	default JungFuncArgs apply(Believes inputBelieves, DialogueTree inputTree){
		return apply(JungFuncArgs.create(inputBelieves, inputTree));
	}
	default JungFuncArgs apply(JungFuncArgs input){
		return getTestTarget().getFunction().apply(input);
	}
}
