package org.salve.personality;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Using this rather than the enum (JungianFunction)
 * allows injection of other functions which is primarly usefull for testing.
 */
@FunctionalInterface
public interface JungFuncAccessor{
	Function<JungFuncArgs, JungFuncArgs> getFunction();
}
