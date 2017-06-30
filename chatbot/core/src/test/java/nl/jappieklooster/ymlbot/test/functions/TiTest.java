package nl.jappieklooster.ymlbot.test.functions;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.Personality;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TiTest extends AJungFuncTest {
	@Override
	public JungianFunction getTestTarget() {
		return JungianFunction.Ti;
	}

	@Test
	public void if_no_goals_choose_with_man_opts(){
		test(donothing->{});
	}

	/**
	 * this is an undesirable property because if Ti is the dominant function
	 * then whatever its irrational function chooses will be preffered
	 * if there are no goals
	 *
	 * So we wan't Ti to ignore the subtree in its 'options' consideration
	 */
	@Test
	public void if_no_goals_choose_with_man_opts_regardless_of_existing_subtree(){
		test(builder -> {
			builder.addOptionAndWith(MockBelievesFactory.hellos, MockBelievesFactory.actor_doctor, subBuilder ->{
				// these options should be ignored since they're a level lower
				subBuilder.addOption(MockBelievesFactory.hellos);
				subBuilder.addOption(MockBelievesFactory.hellos);
				subBuilder.addOption(MockBelievesFactory.hellos);
				subBuilder.addOption(MockBelievesFactory.hellos);
				subBuilder.addOption(MockBelievesFactory.hellos);
			});
		});
	}
	public void test(Consumer<DialogueTreeBuilder> andThen){
		Believes inputBelieves = believes.setGoals(Collections.emptySet());
		DialogueTreeBuilder expectedOrderBuilder = DialogueTreeBuilder.copy(builder);
		Utterance expectedSelected = expectedOrderBuilder.addOption(MockBelievesFactory.whyhere);
		expectedOrderBuilder.addOption(MockBelievesFactory.whyhere);
		expectedOrderBuilder.addOption(MockBelievesFactory.whyhere);
		expectedOrderBuilder.addOption(MockBelievesFactory.needmedicine);
		expectedOrderBuilder.addOption(MockBelievesFactory.needmedicine);
		andThen.accept(expectedOrderBuilder);

		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.whyhere);
		builder.addOption(MockBelievesFactory.needmedicine);
		builder.addOption(MockBelievesFactory.whyhere);
		andThen.accept(builder);
		JungFuncArgs input = JungFuncArgs.create(inputBelieves, builder.getCurrent());
		input = input.insertNextFunc(
			() -> new AddSameDialogueOpts ( new LinkedList<>(builder.getCurrent().getOptions().collect(Collectors.toList())) )
		);

		JungFuncArgs result = apply(input);

		Assert.assertEquals("we expect this option to be selected because it had the most choice",
			Optional.of(expectedSelected),
			result.tree.getPrefferedUtterance()
		);
		Assert.assertEquals("This precise order is expected",
			expectedOrderBuilder.getCurrent(),
			result.tree
		);
	}
	@Test
	public void still_choose_many_opts_with_personality(){
		setPersonality(Personality.create(JungianFunction.Ti));
		if_no_goals_choose_with_man_opts();
	}
	@Test
	public void still_choose_many_opts_with_personality_irfunc_before(){
		setPersonality(Personality.create(JungianFunction.Unit, JungianFunction.Ti));
		if_no_goals_choose_with_man_opts();
	}

	/**
	 * Adds options that are the same as the inserted option
	 * (ie whyhere will prodcue n whyhere)
	 */
	class AddSameDialogueOpts implements Function<JungFuncArgs, JungFuncArgs> {
		LinkedList<DialogueTree> opts;
		public AddSameDialogueOpts (LinkedList<DialogueTree> opts){
			this.opts = opts;
		}

		@Override
		public JungFuncArgs apply(JungFuncArgs jungFuncArgs) {
			List<DialogueTree> newOpts =
				opts.stream().filter(
					x-> x.utterance.getWhat().equals(jungFuncArgs.tree.utterance.getWhat())
				).collect(Collectors.toList());

			return jungFuncArgs.setTree(jungFuncArgs.tree.replaceOptions(newOpts));
		}
	}

}
