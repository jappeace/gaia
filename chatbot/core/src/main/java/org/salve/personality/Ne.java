package org.salve.personality;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;
/**
 * $N_e$ on the other hand just takes the top $x$ of the current dialogue options
 * and expands those, but then next step it will again consider the entire existing
 * tree to find the best $x$ of each ply.
 * This will of course be a much more shallow consideration than $N_i$, but
 * much more broad. Which is of course the behavior we are looking for in both
 * $N_i$ and $N_e$ (see section [[Jungian types]]).
 */
public class Ne implements Function<JungFuncArgs, JungFuncArgs> {

	public static final int PlyDepthLimit = 2;
	public static final int PlyBreathhLimit = 2;

	private final Optional<Ne> nextPly; // we just keep constructing the nextply until we reach the limit

	private final int depth;
	public Ne(int ply_depth) {
		depth = ply_depth;
		if(ply_depth >= PlyDepthLimit){
			nextPly = Optional.empty();
		}else{
			// this may cause a stackoverflow.. reducing the limit may help
			nextPly = Optional.of(new Ne(ply_depth +1));
		}
	}

	@Override
	public JungFuncArgs apply(JungFuncArgs args) {
		return nextPly
			.map(next -> goDeeper(next, args))
			.orElse(args);
	}
	static JungFuncArgs goDeeper(Ne next, JungFuncArgs args) {
		final int height = args.believes.personality.calculateOperationHeight(JungianFunction.Ne, args.tree);
		DialogueTree opts = findOptsAndSort(args, height);
		DialogueTree added = pruneAndTraverseOptions(next, args, opts);

		return args.setTree(args.tree.copyWithAboveLeftMostLeaf(
				height,
				tree ->  tree.appendOptions(added.getOptions())
			)
		);
	}
	static DialogueTree pruneAndTraverseOptions(Ne next, JungFuncArgs args, DialogueTree sortresulttree){
		List<DialogueTree> allOpts = sortresulttree.getOptions().collect(Collectors.toList());
		// crop the list to the specified amount
		List<DialogueTree> selected = allOpts.subList(0, min(allOpts.size(), PlyBreathhLimit));
		DialogueTree tree = sortresulttree.replaceOptions(selected);
		return tree.replaceOptions(
			// for each selected option apply ne
			tree.getOptions().map(x -> next.apply(args.setTree(x)).tree).collect(Collectors.toList())
		);
	}

	static DialogueTree findOptsAndSort(JungFuncArgs args, int height) {
		final Believes believes = args.believes;
		final DialogueTree tree = args.tree;

		final DialogueTree result = tree.copyWithAboveLeftMostLeaf(height, currentPlie -> {
			final Utterance utt = currentPlie.utterance;
			final Stream<DialogueTree> options = believes.findProgrammedOptions(utt);
			return currentPlie.appendOptions(options);
		});

		return args.setTree(result).applyNext().tree;
	}
}
