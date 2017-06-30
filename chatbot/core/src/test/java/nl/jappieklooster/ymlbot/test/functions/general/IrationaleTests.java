package nl.jappieklooster.ymlbot.test.functions.general;

import org.salve.personality.model.DialogueTree;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import nl.jappieklooster.ymlbot.test.functions.AJungFuncTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.Personality;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.salve.personality.JungianFunction.*;


/**
 * This class contains some general contracts for irrationale functions,
 * think of they should generate options and change not change order
 *
 * this can be an interface and be mixed in as a trait,
 * but junit 4 doesn't support that, hope for junit 5.
 */
@RunWith(Parameterized.class)
public class IrationaleTests extends AJungFuncTest {

	@Parameters(name = "{0}")
	public static Iterable<? extends Object> learners() {
		return Arrays.asList(Se, Si, Ne, Ni);
	}
	JungianFunction underTest;
	public IrationaleTests(JungianFunction underTest){
		this.underTest = underTest;
	}
	@Override
	public JungianFunction getTestTarget() {
		return underTest;
	}
	/**
	 * this seems kindoff dumb but an important property to have for irational
	 */
	@Test
	public void irationale_with_no_opts_should_change(){
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(JungFuncArgs.create(getBelieves(), input));
		Assert.assertNotEquals("we expected the ", input, results.tree);
	}
	@Test
	public void irationale_should_not_modify_order_of_opts(){
		DialogueTreeBuilder builder = getBuilder();
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.whyhere);
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(JungFuncArgs.create(getBelieves(), input));

		List<DialogueTree> expected = input
			.getOptions()
			.collect(Collectors.toList());

		List<DialogueTree> actual = results.tree
			.getOptions()
			.map(x->x.replaceOptions(Collections.emptyList())) // prune the result
			.collect(Collectors.toList());
		Assert.assertEquals("this order shouldn't have changed", expected, actual);
	}
	@Test
	public void irationale_should_not_destroy_existing_options_only_append(){
		DialogueTreeBuilder builder = getBuilder();
		// trick them to work on next layer
		setPersonality(Personality.create(getTestTarget()));

		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.whyhere);
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(JungFuncArgs.create(getBelieves(), input));

		List<DialogueTree> expected = input
			.getOptions()
			.collect(Collectors.toList());

		List<DialogueTree> resultList = results.tree
				.getOptions()
				.map(x -> x.replaceOptions(Collections.emptyList())) // prune the result
				.collect(Collectors.toList());
		try {
			List<DialogueTree> actual = resultList.subList(0, 7);
			Assert.assertEquals("this order shouldn't have changed", expected, actual);
		}catch (IndexOutOfBoundsException ex){
			throw new RuntimeException(getTestTarget() + " destroyed existing " +
				"options it has only: " + resultList.size() , ex);
		}
	}
}
