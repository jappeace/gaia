package org.salve.personality;

import org.salve.personality.model.DialogueTree;

import java.util.Comparator;

/**
 * If equal alternation will be preffered, can decorate other comperators
 *
 * Basically each rational function uses this as a tie resolver.
 */
public class AlternateOnEqual implements Comparator<DialogueTree>{

	private final Comparator<DialogueTree> client;
	private final DialogueTree parent;

	public AlternateOnEqual(DialogueTree parent, Comparator<DialogueTree> client) {
		this.client = client;
		this.parent = parent;
	}

	@Override
	public int compare(DialogueTree one, DialogueTree two) {
		int result = client.compare(one, two);
		//  alternation on equality
		if(result == Compare.equal)	{
			if(one.utterance.getByWhom().equals(two.utterance.getByWhom())){
				return Compare.equal;
			}
			// we want to alternate so 2 one
			if(one.utterance.getByWhom().equals(parent.utterance.getByWhom())){
				return Compare.onepreffered;
			}
			if(two.utterance.getByWhom().equals(parent.utterance.getByWhom())){
				return Compare.twopreffered;
			}
			return Compare.equal; // don't know how this happend
		}
		return result;
	}
}
