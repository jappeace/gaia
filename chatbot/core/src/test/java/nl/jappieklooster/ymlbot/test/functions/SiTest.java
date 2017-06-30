package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.Believes;
import org.salve.drools.model.Connection;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SiTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Si;
	}

	@Test
	public void no_learned_connections_exactly_one_produces_reply(){
		DialogueTree inputTree = builder.getCurrent();

		JungFuncArgs result = apply(believes, inputTree);

		Assert.assertEquals("the initial utterance should remain the same",
			inputTree.utterance,// this was an issue at some point
			result.tree.utterance
		);
		Assert.assertEquals("There should one buty only one option available",
			1L,
			result.tree.getOptions().count()
			);
	}

	@Test
	public void use_learned_connection(){
		MockBelievesFactory factory = new MockBelievesFactory();

		DialogueTree inputTree = builder.getCurrent();

		// for some reason the bot has learned to say he is the doctor
		Connection learned = factory.createConnection(MockBelievesFactory.imthedoctor, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("Enthusiasm"));
		Utterance expected = Utterance.create(MockBelievesFactory.actor_patient, learned.to, learned.values);

		Believes inputBelieves = believes.setLearnedConnections(
			believes.learnedConnections.putInCopy(
				factory.symbols.get(MockBelievesFactory.hellos),
				learned
			)
		);

		JungFuncArgs result = apply(inputBelieves, inputTree);
		Assert.assertEquals("In this case believes shouldn't have chagned", inputBelieves, result.believes);
		Assert.assertEquals("The learned connection was used",
			Optional.of(expected),
			result.tree.getPrefferedUtterance()
		);
	}

	@Test
	public void learn_something_and_then_use_it(){
		DialogueTree inputTree = builder.getCurrent();

		Believes inputBelieves = believes;
		final List<Utterance> utts = Arrays.asList(
			// these should *not* be learned (we learn incrementely,
			// these are here to test order)
			Utterance.create(
				MockBelievesFactory.actor_doctor,
				builder.get(MockBelievesFactory.hellos),
				PerlocutionaryValueSet.create("Happy")
			),
			Utterance.create(
				MockBelievesFactory.actor_patient,
				builder.get(MockBelievesFactory.okthanks),
				PerlocutionaryValueSet.create("Happy")
			),
			// these *should* be learned.
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
		final Utterance expectedReply = utts.get(3);

		for(Utterance utt : utts){
			inputBelieves = inputBelieves.addUtterance(utt);
		}

		JungFuncArgs result = apply(inputBelieves, inputTree);
		believesShouldChange(result.believes);
		Assert.assertEquals("The learned reply should be preffered",
			Optional.of(expectedReply),
			result.tree.getPrefferedUtterance()
		);
		Assert.assertEquals("There should be only one option available " +
				"beceause the others weren't learned (incremental learning)",
			1L,
			result.tree.getOptions().count()
		);
	}
}
