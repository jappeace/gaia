package org.salve.personality.model;

import org.salve.drools.Functions;
import org.salve.drools.model.*;
import org.salve.drools.model.values.PerlocutionaryValueSet;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides the main reasoning functionality of the agent
 * Every Dialoge tree consists of a list of other dialogue trees.
 * If this list is empty then the dialogue tree is a leaf
 *
 * You can only go down the dialogue tree, there is no way to go back up.
 *
 * We only consider the left most part of the tree to be importent,
 * (ie the 'selected' utterance).
 * However, the order of the options in the dialgue tree can be changed.
 * To modify a lower level the copywithX functions can be used which take a
 * function that can modify the tree.
 * This is done to gaurantee immutability
 */
@Immutable
public class DialogueTree {
	public final Utterance utterance;
	public final Connection connectionUsed;
	/**
	 * Although possible to modify this variable from this class,
	 * its strongly discouraged since it will break the height attribute.
	 */
	private final List<DialogueTree> options;
	/**
	 * Height from the left most leaf (we don't care about the rest)
	 * since we're immutable we can determine this on construction
	 */
	public final int height;
	/**
	 * the amount of nodes in this dialogue tree,
	 * usefull for equality comparision (cheap to check) and also to give a quick
	 * overview of difference in trees.
	 */
	public final int node_count;
	/**
	 * this may seem dumb, but this number is arbitrary
	 * you could either say leaf height = 1, 0, or -50.
	 * It doesn't really matter to the computation as long as its consistent
	 * (may cause some cofnusing to string situations with -50 though)
	 */
	private static final int leaf_height = 0;
	private static final List<DialogueTree> empty = new ArrayList<>(1);

	public DialogueTree(Utterance utterance, Connection connection_used, List<DialogueTree> options) {
		this.utterance = utterance;
		this.connectionUsed = connection_used;
		this.options = new ArrayList<>(options);
		height = options.stream()
			.map(x->x.height)
			.findFirst()
			.map(x->x+1) // we have some options, so are one deeper than max
			.orElse(leaf_height); // a leaf node
		node_count = this.options.stream().map(x->x.node_count).reduce(1 /* count self */, Integer::sum);
		setRecalcHash();
	}

	/**
	 * In certain situations the hash function needs to be recalculated
	 * (for example when modifying some of the private variables)
	 */
	private void setRecalcHash(){
		this.hash_code = () -> {
			final int result = utterance.hashCode() * 3 +
				connectionUsed.hashCode() * 5 +
				options.hashCode() * 7 -
				height * 193 + node_count * 89;
			this.hash_code = () -> result;
			return result;
		};
	}

	private Supplier<Integer> hash_code;
	@Override
	public int hashCode(){
		return hash_code.get();
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
		other -> {
			if(this.height != other.height){
				return false;
			}
			if(this.node_count != other.node_count){
				return false;
			}
			if(!this.utterance.equals(other.utterance)){
				return false;
			}
			if(!this.connectionUsed.equals(other.connectionUsed)){
				return false;
			}
			return this.options.equals(other.options);
		});
	}

	public static DialogueTree createFromConnection(Connection conn){
		return new DialogueTree(Utterance.create(conn.restricted_to, conn.to, conn.values), conn, empty);
	}

	public static DialogueTree createFromUtteranceAndBelieves(Utterance utt, Believes believes){
		final Optional<Symbol> last = believes.lastUtterance().map(Utterance::getWhat);
		final Connection connection = believes.findToFromLastUttTo(utt.informative)
			// if the (flattened) option was none we just return a default
			.orElse(Connection.create(utt.getWhat(), utt.getByWhom(), PerlocutionaryValueSet.confused));
		return new DialogueTree(
			utt.setPerlocutionaryValues(connection.values), // the connection knows
			connection,
			empty
		);
	}

	public DialogueTree addOption(Utterance utterance, Connection connection_used){
		return addOption(new DialogueTree(utterance, connection_used, empty));
	}
	public DialogueTree addOption(DialogueTree tree){
		final List<DialogueTree> options = new ArrayList<>(this.options);
		options.add(tree);
		final DialogueTree result = new DialogueTree(this.utterance, this.connectionUsed, options);
		return result;
	}

	public DialogueTree replaceOptions(List<DialogueTree> with){
		return new DialogueTree(this.utterance, this.connectionUsed, with);
	}

	/**
	 * Add extra options to the end of the options list.
	 * @param with
	 * @return
	 */
	public DialogueTree appendOptions(Stream<DialogueTree> with){
		return replaceOptions(Stream.concat(getOptions(), with).collect(Collectors.toList()));
	}

	public Stream<DialogueTree> getOptions(){
		return this.options.stream();
	}

	public Optional<Utterance> getPrefferedUtterance(){
		return this.getOptions().findFirst().map(x -> x.utterance);
	}
	public Optional<DialogueTree> getPrefferdOption(){
		return this.getOptions().findFirst();
	}
	public boolean isPrefferedBy(Actor actor){
		return getPrefferedUtterance().map(utt -> utt.getByWhom().equals(actor)).orElse(false);
	}

	public DialogueTree getLeftMostTreeAt(int height){
		if(thisIsAtHeight(height)){
			return this;
		}
		return getOptions()
			.findFirst()
			.map(x -> x.getLeftMostTreeAt(height))
			.orElse(this);
	}
	public DialogueTree getLeftMostLeaf(){
		return getLeftMostTreeAt(leaf_height);
	}


	/**
	 * To modify the left most value use this function, the result of the
	 * mappped value will be used as the replacement of the left most value in
	 *
	 * the tree
	 * @param f CurrentValue -> Replacement
	 * @return The new tree with the new value
	 */
	public DialogueTree copyWithLeftMostLeaf(Function<DialogueTree, DialogueTree> f){
		return copyWithAboveLeftMostLeaf(leaf_height, f);
	}
	private boolean thisIsAtHeight(int target){
		return this.height <= target;
	}


	/**
	 * Generalization of 'copyWithStartAtUntilLeaf' and
	 * 'copyWithAboveLeftMostLeaf'
	 *
	 * You could very easily traverse the tree with this if you attach whenNot
	 * into the called function of the argument dialogueTree.
	 *
	 * Whenat will always be exeucted on the leaf.
	 *
	 * @param height at which point to use another function
	 * @param whenNot what to do when not there yet
	 * @param whenAt what to do when there
	 * @return
	 */
	private DialogueTree withPrefferdIfAtHeight(
		int height,
		Function<DialogueTree, DialogueTree> whenNot,
		Function<DialogueTree, DialogueTree> whenAt){
		if(thisIsAtHeight(height)){ // in practice equal, but we just don't want stackoverflows
			// note return
			return whenAt.apply(this);
		}
		// we execute whenNot on preffered, because if we were at height the
		// previous condition woudl've been true
		// however if there is no prefered we are at leaf level.
		return mapPreffered(whenNot, whenAt);
	}

	/**
	 * If we have a preffered, execute withPrefferd on it, If we don't have,
	 * execute ifNoPrefferedWithThis on the current object.
	 * @param withPreffered
	 * @param ifNoPreferedWithThis
	 * @return
	 */
	private DialogueTree mapPreffered(Function<DialogueTree, DialogueTree> withPreffered,
									  Function<DialogueTree, DialogueTree> ifNoPreferedWithThis){
		final Optional<DialogueTree> prefferedOption = getOptions().findFirst();
		return prefferedOption.map(preffered -> {
			final List<DialogueTree> options = getOptions().collect(Collectors.toList());
			options.set(0, withPreffered.apply(preffered)); // 0 being preffered
			return replaceOptions(options);

		}).orElse(// there is no first option
			ifNoPreferedWithThis.apply(this)
		);
	}

	/**
	 * go down until height, then keep applying function until leaf
	 * @param height
	 * @param function
	 * @return
	 */
	public DialogueTree copyWithStartAtUntilLeaf(int height, Function<DialogueTree, DialogueTree> function){
		// TODO fix this blackmagick
		if(height < leaf_height){
			return this;
		}
		return withPrefferdIfAtHeight(
			height,
			tree -> tree.copyWithStartAtUntilLeaf(height,function),
			tree -> {
				final DialogueTree result = function.apply(tree);
				return result.mapPreffered(
					prefferd -> prefferd.copyWithStartAtUntilLeaf(height, function),
					Function.identity()
				);
			}
		);
	}
	/**
	 * A more generalized form that can opperate on any height
	 * @param height
	 * @param function
	 * @return
	 */
	public DialogueTree copyWithAboveLeftMostLeaf(int height, Function<DialogueTree, DialogueTree> function){
		return withPrefferdIfAtHeight(
			height,
			tree -> tree.copyWithAboveLeftMostLeaf(height,function),
			function
		);
	}
	@Override
	public String toString(){
		final String opts = options.isEmpty() ? "0" : ", " +options.size()+":" + options.toString();
		return "Dia(" +
			"h:"+ height + ", " +
			"n:" + node_count + ", " +
			utterance.toString() +
			opts +
		")";
	}
	public Scene getScene(){
		return utterance.getWhat().scene;
	}
}
