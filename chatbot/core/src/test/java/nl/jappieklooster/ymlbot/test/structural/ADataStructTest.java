package nl.jappieklooster.ymlbot.test.structural;

import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Before;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.personality.model.Believes;


/**
 * Tests for the basic datastructures can use this
 */
abstract public class ADataStructTest {
	MockBelievesFactory factory;
	Believes believes;
	@Before
	public void before(){
		factory = new MockBelievesFactory();
		believes = factory.createTestBelieves();
	}

	Utterance create(String what){
		return Utterance.create(MockBelievesFactory.actor_doctor, factory.symbols.get(what), PerlocutionaryValueSet.empty);
	}
}
