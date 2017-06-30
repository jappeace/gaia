package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.RecursiveExpectationBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;
import org.salve.personality.Ni;

public class NiTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Ni;
	}

	@Test
	public void ni_should_create_depth_first_options_hello_always(){
		final DialogueTree initial = builder.getCurrent();

		final RecursiveExpectationBuilder expectBuilder = new RecursiveExpectationBuilder(1, Ni.PlyDepthLimit,0);
		expectBuilder.accept(builder);
		final DialogueTree expectedTree = builder.getCurrent();

		JungFuncArgs arguments = JungFuncArgs.create(believes,initial)
			.insertNextFunc(() -> RecursiveExpectationBuilder::mock_function_sort_opts_alphabetacly);
		JungFuncArgs result = apply(arguments);

		believesShouldRemainTheSame(result.believes);
		Assert.assertEquals("There should be a hello chain in depth",
			expectedTree,
			result.tree
		);
	}
	@Test
	public void ni_should_create_alphabatical_responses(){
		Utterance whyHere = builder.addOption(MockBelievesFactory.whyhere, MockBelievesFactory.actor_patient);
		final DialogueTree initial = builder.getCurrent();

		builder.withOption(whyHere, builder ->{
		builder.addOptionAndWith(MockBelievesFactory.imthedoctor, MockBelievesFactory.actor_doctor, builderoot ->{
			builderoot.addOptionAndWith(MockBelievesFactory.okthanks, MockBelievesFactory.actor_patient, builder2 ->{
					builder2.addOptionAndWith(MockBelievesFactory.areyousick, MockBelievesFactory.actor_doctor, builder3 ->{
						builder3.addOption(MockBelievesFactory.yesnomaybe, MockBelievesFactory.actor_patient);
					});
				});
			});
		});

		final DialogueTree expectedTree = builder.getCurrent();

		JungFuncArgs arguments = JungFuncArgs.create(believes,initial)
			.insertNextFunc(() -> RecursiveExpectationBuilder::mock_function_sort_opts_alphabetacly);
		JungFuncArgs result = apply(arguments);

		believesShouldRemainTheSame(result.believes);
		Assert.assertEquals("There should be a dialogue thought chain occuring here",
			expectedTree,
			result.tree
		);
	}
}
