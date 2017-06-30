package nl.jappieklooster.ymlbot.test.structural;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import io.atlassian.fugue.Pair;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.salve.drools.model.Actor;
import org.salve.drools.model.Symbol;
import org.salve.drools.model.Utterance;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.DialogueTree;
import org.salve.personality.model.Personality;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * In here we test the height determination for personality functions.
 * Note that the content of dialogue trees is ignored.
 * We only care about height.
 */
public class PersonalityTests {

	public DialogueTreeBuilder builder;
	@Before
	public void before_test(){
		MockBelievesFactory factory = new MockBelievesFactory();
		builder = DialogueTreeBuilder.create(
			factory.symbols,
			factory.createTestBelieves(),
			MockBelievesFactory.actor_doctor,
			MockBelievesFactory.hellos
		);
	}
	@Test
	public void empty_personality_should_return_enforce_old_rules(){
		Personality person = Personality.empty;

		DialogueTree tree = builder.getCurrent();

		final String msg = "Zero because nothing to operate upon";
		Assert.assertEquals(msg , 1, person.calculateOperationHeight(JungianFunction.Fe, tree));;
		Assert.assertEquals(msg, 0, person.calculateOperationHeight(JungianFunction.Si, tree));;
	}
	@Test
	public void empty_should_place_irrational_lower(){
		Personality person = Personality.empty;

		builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient);
		DialogueTree tree = builder.getCurrent();

		Assert.assertEquals("Rational should be on height", 1, person.calculateOperationHeight(JungianFunction.Fe, tree));;
		Assert.assertEquals("Irrational should be at 0", 0, person.calculateOperationHeight(JungianFunction.Si, tree));;
	}

	@Test
	public void empty_tree_test(){
		Personality person = new Personality(Lists.newArrayList(JungianFunction.Ne, JungianFunction.Fe));

		DialogueTree higherTree = builder.getCurrent();

		test(person, higherTree, 0, 0);
	}
	@Test
	public void with_irrational_upfront_rational_should_be_one(){
		Personality person = new Personality(Lists.newArrayList(JungianFunction.Ne, JungianFunction.Fe));

		Utterance selected = builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient);
		DialogueTree tree = builder.getCurrent();

		test(person, tree, 1, 1);

		builder.withOption(selected, subbuilder ->{
			subbuilder.addOption(MockBelievesFactory.hellos);
		});
		DialogueTree higherTree = builder.getCurrent();
		test(person, higherTree, 2, 2);
	}
	@Test
	public void later_function_didnt_interfere(){
		Personality person = new Personality(Lists.newArrayList(JungianFunction.Ne, JungianFunction.Fe, JungianFunction.Si));

		builder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, subbuilder ->{
			subbuilder.addOption(MockBelievesFactory.hellos);
		});
		DialogueTree tree = builder.getCurrent();
		test(person, tree, 2, 2);
	}
	public static void test(Personality personality, DialogueTree tree){
		test(personality, tree, 1,0);
	}
	public static void test(Personality personality, DialogueTree tree, int expectedFe, int expectedNe){
		Assert.assertEquals("Rationale should work one above the option layer", expectedFe,
			personality.calculateOperationHeight(JungianFunction.Fe, tree)
		);
		Assert.assertEquals("Should work at the option layer", expectedNe,
			personality.calculateOperationHeight(JungianFunction.Ne, tree)
		);
	}
	@Test
	public void test_all_functions_full_tree_riir(){
		// ISTP: Ti – Se – Ni – Fe
		Personality personality = new Personality(Lists.newArrayList(JungianFunction.Ti, JungianFunction.Se, JungianFunction.Ni, JungianFunction.Fe));
		test_full_tree_personality(personality, 3,2,2,2);
	}
	@Test
	public void test_all_functions_full_tree_irri(){
		// ESFP: Se – Fi – Te – Ni
		Personality personality = new Personality(Lists.newArrayList(JungianFunction.Se, JungianFunction.Fi, JungianFunction.Te, JungianFunction.Ni));
		test_full_tree_personality(personality, 3,3,2,1);
	}

	public void test_full_tree_personality(Personality personality, Integer... heights){
		// because we went one ply deeper
		// it shouldn't matter
		builder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, subbuilder ->{
			subbuilder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, sjubsjubbuilder ->{
				sjubsjubbuilder.addOption(MockBelievesFactory.hellos);
			});
		});
		DialogueTree tree = builder.getCurrent();

		// matchup heights with functions and assert them all
		Streams.zip(personality.stream(), Stream.of(heights), Pair::new).forEach(pair -> {
			Assert.assertEquals("Wrong height for " + pair.left(),
				// we need to explictly cassed otherwise it thinks we want to
				// compare objects
				(int) pair.right(),
				personality.calculateOperationHeight(pair.left(), tree)
			);
		});
	}

}
