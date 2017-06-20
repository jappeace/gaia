package org.salve.drools.model.db;

public class PatternCollection {
	public final PatternDatabase inScene;
	public final PatternDatabase neighbourToScene;

	public PatternCollection(PatternDatabase inScene, PatternDatabase neighbourToScene) {
		this.inScene = inScene;
		this.neighbourToScene = neighbourToScene;
	}
}
