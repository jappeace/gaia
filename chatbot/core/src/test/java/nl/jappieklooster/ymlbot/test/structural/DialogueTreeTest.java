package nl.jappieklooster.ymlbot.test.structural;

import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.drools.model.Connection;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.personality.model.DialogueTree;

public class DialogueTreeTest extends ADataStructTest {
	@Test
	public void test_construction_utt(){
		believes = believes.addUtterance(create(MockBelievesFactory.hellos));
		DialogueTree result =
			DialogueTree.createFromUtteranceAndBelieves(create(MockBelievesFactory.whyhere), believes);
		Assert.assertEquals("Should have the predifined perloc values", PerlocutionaryValueSet.create("Angry"), result.utterance.perlocutionaryValues);

	}
	@Test
	public void test_construction_connection(){
		believes = believes.addUtterance(create(MockBelievesFactory.hellos));

		Connection conn = believes
			.findToFromLastUttTo(
				create(MockBelievesFactory.whyhere).informative
			).get();

		DialogueTree result =
			DialogueTree.createFromConnection(conn);
		Assert.assertEquals("Should have the predifined perloc values", PerlocutionaryValueSet.create("Angry"), result.utterance.perlocutionaryValues);

	}
}
