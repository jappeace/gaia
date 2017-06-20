package nl.jappieklooster.ymlbot.model;

import java.util.List;

/**
 * This class doesn't even exist after loading, its just a utility
 * class to make it easier to enter connections
 */
public class RawConnections {
	public RawBefore before = RawBefore.none;
	/**
	 * From list
	 */
	public List<String> from;
	/**
	 * To list
	 */
	public List<RawConnection> to;

	public RawConnection to_defaults = RawConnection.createDefaults();
}
