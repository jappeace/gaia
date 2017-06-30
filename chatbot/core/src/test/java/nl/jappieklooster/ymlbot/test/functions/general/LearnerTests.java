package nl.jappieklooster.ymlbot.test.functions.general;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.functions.AJungFuncTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.Arrays;

/**
 * Tests for functions that do learning
 */
@RunWith(Parameterized.class)
public class LearnerTests extends AJungFuncTest {

	@Parameters(name = "{0}")
	public static Iterable<? extends Object> learners() {
		return Arrays.asList(JungianFunction.Si, JungianFunction.Fe);
	}
	JungianFunction underTest;
	public LearnerTests(JungianFunction underTest){
		this.underTest = underTest;
	}
	@Override
	public JungianFunction getTestTarget() {
		return underTest;
	}
	@Test
	public void no_history_means_no_leanrning(){
		DialogueTree inputTree = getBuilder().getCurrent();

		JungFuncArgs result = apply(getBelieves(), inputTree);

		believesShouldRemainTheSame(result.believes);
	}

	@Test
	public void should_do_some_learning_with_history(){
		DialogueTree inputTree = getBuilder().getCurrent();

		Utterance patientSaid = Utterance.create(
			MockBelievesFactory.actor_patient,
			getBuilder().get(MockBelievesFactory.areyousick),
			PerlocutionaryValueSet.create("Concerned")
		);
		Utterance doctorSaid = Utterance.create(
			MockBelievesFactory.actor_doctor,
			getBuilder().get(MockBelievesFactory.imthedoctor),
			PerlocutionaryValueSet.create("Angry")
		);
		Believes inputBelieves = getBelieves().addUtterance(
			patientSaid
		).addUtterance(
			doctorSaid
		);
		JungFuncArgs result = apply(inputBelieves, inputTree);
		believesShouldChange(result.believes);
	}

}
