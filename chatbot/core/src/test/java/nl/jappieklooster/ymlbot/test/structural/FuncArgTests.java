package nl.jappieklooster.ymlbot.test.structural;

import nl.jappieklooster.ymlbot.test.DialogueTreeBuilder;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.salve.personality.model.Believes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salve.personality.JungFuncAccessor;
import org.salve.personality.JungFuncArgs;
import org.salve.personality.JungianFunction;

import java.util.Arrays;
import java.util.List;

public class FuncArgTests {

	Believes believes;
	DialogueTreeBuilder builder;
	@Before
	public void before_test() {
		MockBelievesFactory factory = new MockBelievesFactory();
		believes = factory.createTestBelieves();
		builder = DialogueTreeBuilder.create(factory.symbols, believes, MockBelievesFactory.actor_doctor, MockBelievesFactory.hellos);
	}

	/**
	 * This order is really important for certain jungian functions
	 */
	@Test
	public void check_order_of_insert_personalities(){
		// just three functions, to allow a flip of order to be detected
		List<JungFuncAccessor> expectedOrder = Arrays.asList(JungianFunction.Fe, JungianFunction.Ni, JungianFunction.Unit);

		JungFuncArgs target = JungFuncArgs.create(believes, builder.getCurrent());
		JungFuncArgs result = target.insertNextFuncs(expectedOrder);

		Assert.assertEquals("First element of the 'next' should be the same as the expected order next",
			expectedOrder.get(0),
			result.next.get().getValue0()
		);
	}
}
