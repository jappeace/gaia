package nl.jappieklooster.ymlbot.test.structural;

import com.google.common.collect.Lists;
import nl.jappieklooster.ymlbot.test.MockBelievesFactory;
import org.junit.Assert;
import org.junit.Test;
import org.salve.drools.model.Utterance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BelieveTest extends ADataStructTest {
	@Test
	public void test_last_utt(){
		Utterance one = create(MockBelievesFactory.goodbyes);
		Utterance two = create(MockBelievesFactory.badbyes);
		Utterance three = create(MockBelievesFactory.hellos);
		Utterance expected = create(MockBelievesFactory.areyousick);

		believes = believes.addUtterance(one);
		believes = believes.addUtterance(two);
		believes = believes.addUtterance(three);
		believes = believes.addUtterance(expected);

		Assert.assertEquals("should equal", Optional.of(expected), believes.lastUtterance());

		List<Utterance> order = Lists.newArrayList( one, two, three, expected);

		Assert.assertEquals("This specific order", order, believes.getUtterances().collect(Collectors.toList()));
	}
}
