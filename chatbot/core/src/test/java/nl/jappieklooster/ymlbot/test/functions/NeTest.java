package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.RecursiveExpectationBuilder;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.HashMap;
import java.util.Map;

public class NeTest extends AJungFuncTest {
	// TODO, add test to show desctructiveness of NE, or perheps for all irrational?
	// Irational should never delete items??
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Ne;
	}

	Map<Pair<Integer, Integer>, Pair<String, PerlocutionaryValue[]>> expectations;
	@Before
	public void resetExpectation(){
		 expectations = new HashMap<>();
	}
	private void expect(Integer x, Integer y, String string, PerlocutionaryValue... values){
		expectations.put(new Pair<>(x, y), new Pair<>(string, values));
	}
	@Test
	public void ne_should_make_a_tree_structure_out_of_hellos(){
		final DialogueTree initial = builder.getCurrent();
		// left implicitly is the first hellos by the doctor
		builder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient,
			builder -> {
				builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("happy"));
				builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("happy"));
			},
			PerlocutionaryValueSet.create("happy")
		);
		builder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor,
			builder -> {
				builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_patient, PerlocutionaryValueSet.create("happy"));
				builder.addOption(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, PerlocutionaryValueSet.create("happy"));
			}, PerlocutionaryValueSet.create("Persuading", "Scary")
		);

		final DialogueTree expectedTree = builder.getCurrent();


		JungFuncArgs arguments = JungFuncArgs.create(believes,initial)
			.insertNextFunc(() -> RecursiveExpectationBuilder::mock_function_sort_opts_alphabetacly);
		JungFuncArgs result = apply(arguments);

		believesShouldRemainTheSame(result.believes);
		System.out.println("expected: " + expectedTree);
		System.out.println("result:   " + result.tree);
		Assert.assertEquals("There should be a range of responses in aplhabetical order",
			expectedTree,
			result.tree
		);
	}

}
