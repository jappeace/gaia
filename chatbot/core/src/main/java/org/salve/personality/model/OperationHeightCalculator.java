package org.salve.personality.model;

import java.util.function.Function;

@FunctionalInterface
public interface OperationHeightCalculator extends Function<DialogueTree, Integer> {
}
