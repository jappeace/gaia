package nl.jappieklooster.ymlbot.test.structural;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.salve.drools.Functions;
import org.salve.drools.model.*;
import org.salve.drools.model.template.db.QueryDatabase;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.personality.model.Believes;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BeforeTest{

	Believes believes;
	Set<Connection> expected;
	Utterance input;
	Connection beforeconn;
	Utterance some_other_utt;
	Utterance other_actor_utt;


	@org.junit.Before
	public void setup_a_before_connection(){
		MockBelievesFactory factory = new MockBelievesFactory();

		Before before = Before.create(new Informative(MockBelievesFactory.actor_doctor, factory.symbols.get(MockBelievesFactory.hellos)));
		beforeconn = new Connection(
			Optional.of(before),
			factory.symbols.get(MockBelievesFactory.imthedoctor),
			MockBelievesFactory.actor_doctor,
			PerlocutionaryValueSet.empty,
			QueryDatabase.empty);
		factory.setconnect(MockBelievesFactory.hellos, Sets.newHashSet(beforeconn));
		believes = factory.createTestBelieves();
		expected = factory.createConnections(
			MockBelievesFactory.restricted(MockBelievesFactory.whyhere, MockBelievesFactory.actor_doctor, "Angry"),
			MockBelievesFactory.restricted(MockBelievesFactory.whyhere, MockBelievesFactory.actor_patient, "Angry"),
			MockBelievesFactory.restricted(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, "Happy"),
			MockBelievesFactory.restricted(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient, "Happy"),
			MockBelievesFactory.restricted(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_doctor, "Persuading", "Scary"),
			MockBelievesFactory.restricted(MockBelievesFactory.needmedicine, MockBelievesFactory.actor_patient, "Persuading", "Scary")
		);
		input = Utterance.create(
			MockBelievesFactory.actor_doctor,
			factory.symbols.get(MockBelievesFactory.hellos),
			PerlocutionaryValueSet.empty
		);
		some_other_utt = Utterance.create(
			MockBelievesFactory.actor_doctor,
			factory.symbols.get(MockBelievesFactory.goodbyes),
			PerlocutionaryValueSet.empty
		);
		other_actor_utt = Utterance.create(
			MockBelievesFactory.actor_patient,
			factory.symbols.get(MockBelievesFactory.goodbyes),
			PerlocutionaryValueSet.empty
		);
	}
	private void does_test(String... strings){
		expected.add(beforeconn);
		test("This does include the before " + Functions.concat(strings));
	}
	private void not_test(String... strings){
		test("This does not include the before " + Functions.concat(strings));
	}
	private void test(String msg){
		Set<Connection> found = believes.findProgrammedConnections(input).collect(Collectors.toSet());
		Assert.assertEquals(msg, expected, found);
	}
	@Test
	public void nothing_said_didnt_include_before(){
		not_test();
	}
	@Test
	public void said_it_did_include(){
		believes = believes.addUtterance(input);
		does_test();
	}
	@Test
	public void said_sthgn_else_didnt_include(){
		believes = believes.addUtterance(some_other_utt);
		not_test();
	}
	@Test
	public void said_sthgn_else_then_it_did_include(){
		believes = believes.addUtterance(some_other_utt);
		believes = believes.addUtterance(input);
		does_test();
	}
	@Test
	public void said_it_sthng_didnt_include(){
		believes = believes.addUtterance(input);
		believes = believes.addUtterance(some_other_utt);
		not_test();
	}

	/**
	 *
	 */
	@Test
	public void it_other_actor_utt_did_include(){
		believes = believes.addUtterance(input);
		believes = believes.addUtterance(other_actor_utt);
		does_test(" Before tags only look at the before utterence of " +
			"whichever actor they target, this is because it doesn't make sense " +
			"to target the other actor's utterences since they won't match the " +
			"informative anyway, also this is the AIML that tag behavior");
	}
}
