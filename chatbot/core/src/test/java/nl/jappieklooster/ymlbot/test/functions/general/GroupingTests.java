package nl.jappieklooster.ymlbot.test.functions.general;

import com.google.common.collect.Sets;
import org.salve.personality.model.Believes;
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
import static org.salve.personality.JungianFunction.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


/**
 * Tests for functions that do learning
 */
@RunWith(Parameterized.class)
public class GroupingTests extends AJungFuncTest {

	@Parameters(name = "{0}")
	public static Iterable<? extends Object> learners() {
		return Arrays.asList(Fi, Te, Ti); // Fe not so much
	}
	JungianFunction underTest;
	public GroupingTests(JungianFunction underTest){
		this.underTest = underTest;
	}
	@Override
	public JungianFunction getTestTarget() {
		return underTest;
	}

	BiFunction<Believes, DialogueTree, JungFuncArgs> createArgs = JungFuncArgs::create;
	@Test
	public void should_group_these_opts(){
		DialogueTreeBuilder builder = getBuilder();
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.hellos);
		builder.addOption(MockBelievesFactory.whyhere);
		DialogueTree input = getBuilder().getCurrent();
		JungFuncArgs results = apply(createArgs.apply(getBelieves(), input));

		List<DialogueTree> actual = results.tree
			.getOptions()
			.collect(Collectors.toList());

		// this can be done with a fold, but not the java ones
		Set<DialogueTree> seen = Sets.newHashSet();
		Optional<DialogueTree> active = Optional.empty();
		for(DialogueTree tree : actual){
			Optional<DialogueTree> current = Optional.of(tree);
			if(active.equals(current)){
				continue;
			}
			Assert.assertFalse("This list isn't grouped:" + actual, seen.contains(tree));
			active = current;
			seen.add(tree);
		}
	}
}
