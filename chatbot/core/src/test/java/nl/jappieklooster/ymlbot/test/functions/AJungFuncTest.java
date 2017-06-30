package nl.jappieklooster.ymlbot.test.functions;

import nl.jappieklooster.ymlbot.test.functions.IJungFuncTest;
import org.salve.personality.model.Believes;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Before;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.Personality;

import java.util.stream.Collectors;

/**
 * Shared prelude of tests
 *
 * Should *not* extend this but rather rationale or irrationale to get some free
 * tests
 *
 * Its usually a bad idea to inherit fields,
 * but since we're dealing with tests (and thus a rather flat inheritance model)
 * and this allows significant code de-duplication I think this is okay..
 */
public abstract class AJungFuncTest implements IJungFuncTest {
	final JungianFunction target = getTestTarget();
	public abstract JungianFunction getTestTarget();

	public Believes believes;
	public DialogueTreeBuilder builder;
	@Before
	public void before_test(){
		MockBelievesFactory factory = new MockBelievesFactory();
		believes = factory.createTestBelieves();
		builder = DialogueTreeBuilder.create(
			factory.symbols,
			believes,
			MockBelievesFactory.actor_doctor,
			MockBelievesFactory.hellos
		);
	}

	@Override
	public DialogueTreeBuilder getBuilder() {
		return builder;
	}

	@Override
	public Believes getBelieves() {
		return believes;
	}

	/**
	 * We need to be able to change personality to do tests upon its effect
	 * However we do *not* want to be able to do this within the believes.
	 *
	 * As a comprimise a general purpose create function was created that
	 * distrusts input but allows all fields to be set.
	 * @param to
	 */
	public void setPersonality(Personality to){
		Believes old = Believes.copy(believes);
		believes = Believes.create(
			old.programmedConnections,
			old.learnedConnections,
			old.goals,
			old.values,
			old.learnedValues,
			old.getUtterances().collect(Collectors.toList()),
			old.getActors().collect(Collectors.toSet()),
			old.self,
			to);
	}
}
