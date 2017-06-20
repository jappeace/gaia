package org.salve.personality;

import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.Connection;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We can consider $N_i$ to be a depth first approach. Going several plies deep and
 * at each ply consulting the $next$ function which step to take.
 *
 * For Ni we need to do some limited recursion (as in we don't want to do it
 * forever).
 * However since Java doesn't do partial application,
 * we abuse class mechanics instead.
 */
@Immutable
public class Ni implements Function<JungFuncArgs, JungFuncArgs> {
	private final Optional<Ni> nextPly; // we just keep constructing the nextply until we reach the limit
	public static final int PlyDepthLimit = 4;
	private final int depth;

	public Ni(int ply_depth) {
		depth = ply_depth;
		if(ply_depth >= PlyDepthLimit){
			nextPly = Optional.empty();
		}else{
			// this may cause a stackoverflow.. reducing the limit may help
			nextPly = Optional.of(new Ni(ply_depth +1));
		}
	}

	@Override
	public JungFuncArgs apply(JungFuncArgs args) {
		return nextPly
			.map(next -> goDeeper(next, args))
			.orElse(args);
	}
	public JungFuncArgs goDeeper(Ni next, JungFuncArgs args){
		final int height = args.believes.personality.calculateOperationHeight(JungianFunction.Ni, args.tree) - depth;
		final JungFuncArgs opts = findOptsAndSort(height, args);

		// add just the one preffered option to the tree or else do nothing
		final JungFuncArgs selected = opts.setTree(opts
			.tree.getPrefferdOption()
			.map(option -> args.tree.copyWithAboveLeftMostLeaf(height, argtree -> argtree.addOption(option)))
			.orElse(args.tree));
		return next.apply(selected);
	}

	public static JungFuncArgs findOptsAndSort(int height, JungFuncArgs args) {
		final Believes believes = args.believes;
		final DialogueTree tree = args.tree;

        final DialogueTree currentPlie = tree.getLeftMostTreeAt(height);
        final Utterance utt = currentPlie.utterance;
        final Stream<DialogueTree> options = believes.findProgrammedOptions(utt);
        final DialogueTree candiates = currentPlie.appendOptions(
			options
        );
		return args.setTree(candiates)
			.applyNext() // sort
			.insertNextFunc(args.next.get().getValue0()); // reset next func, we don't want to iterate
	}
}
