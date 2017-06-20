package nl.jappieklooster.ymlbot.test;

import com.esotericsoftware.yamlbeans.YamlException;
import nl.jappieklooster.ymlbot.model.RawConnection;
import org.junit.Assert;
import org.junit.Test;

public class LoadTests {

	@Test
	public void raw_connection_when_no_value_overwrites() throws YamlException {
		final String expected = "blah";
		RawConnection defaults = new RawConnection();
		defaults.scene = expected;

		RawConnection target = new RawConnection();
		RawConnection result = target.overwriteIfDefault(defaults);
		Assert.assertEquals("should be overwritten", expected, result.scene);
		Assert.assertEquals("should stay the same", new RawConnection().scene, target.scene);
	}
	@Test
	public void raw_connection_when_has_value_no_overwrites() throws YamlException {
		final String notexpected = "blah";
		final String expected = "bloeh";
		RawConnection defaults = new RawConnection();
		defaults.scene = notexpected;

		RawConnection target = new RawConnection();
		target.scene = expected;
		RawConnection result = target.overwriteIfDefault(defaults);
		Assert.assertEquals("shouldn't be overwritten but expected", expected, result.scene);
		Assert.assertEquals("should stay the same", expected, target.scene);
	}
}
