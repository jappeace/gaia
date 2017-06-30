package nl.jappieklooster.ymlbot.test;

import org.salve.drools.model.*;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.personality.model.Believes;
import org.salve.personality.model.DialogueTree;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A class that pretends dialoguetree is mutable
 * obviously not thread safe
 */
public class DialogueTreeBuilder {
	private final Believes believes;
	private DialogueTree dialogueTree;
	private final Map<String, Symbol> symboldata;

	private DialogueTreeBuilder(Believes believes, DialogueTree tree){
		this.believes = believes;
		this.symboldata = new HashMap<>();
		this.dialogueTree = tree;
	}

	public Symbol get(String what){
		// is this function questionable?
		// no null checks for example, Database provides that trough optional
		// on the other hand its test code.
		return symboldata.get(what);
	}

	public static DialogueTreeBuilder copy(DialogueTreeBuilder from){
		DialogueTreeBuilder result = new DialogueTreeBuilder(from.believes, from.dialogueTree);
		result.symboldata.putAll(from.symboldata);
		return result;
	}
	public static DialogueTreeBuilder create(Map<String, Symbol> data, Believes believes, Actor rootUtterer, String saidWhat){
		DialogueTree tree = DialogueTree.createFromUtteranceAndBelieves(
			Utterance.create(rootUtterer, data.get(saidWhat), PerlocutionaryValueSet.empty),
			believes
		);
		DialogueTreeBuilder result = new DialogueTreeBuilder(believes, tree);
		result.symboldata.putAll(data);
		return result;
	}

	public Utterance addOption(String what){
		return addOption( what, MockBelievesFactory.actor_any);
	}
	public Utterance addOption(String what, Actor who){
		return addOption(what, who, PerlocutionaryValueSet.empty);
	}
	/**
	 *
	 * @param who
	 * @param what
	 * @return the inserted utterence
	 */
	public Utterance addOption(String what, Actor who, PerlocutionaryValueSet values ){
		return addOptionAndWith(what, who, DialogueTreeBuilder::getCurrent, values).utterance;
	}
	public DialogueTreeBuilder createWithRoot(Utterance which){
		return createWithRoot(searchOption(which));
	}
	public DialogueTree searchOption(Utterance which){
		return dialogueTree
				.getOptions()
				.filter(x->x.utterance.equals(which))
				.findAny()
				.orElseThrow(() ->
					new NoSuchElementException("could not find + " + which)
				);
	}

	public DialogueTreeBuilder createWithRoot(DialogueTree tree){
		DialogueTreeBuilder result = new DialogueTreeBuilder(believes, tree);
		result.symboldata.putAll(symboldata);
		return result;
	}

	public DialogueTree addOptionAndWith(String what, Actor who, Consumer<DialogueTreeBuilder> commands){
		return addOptionAndWith(what, who, commands, PerlocutionaryValueSet.empty);
	}

	public DialogueTree addOptionAndWith(String what, Actor who, Consumer<DialogueTreeBuilder> commands, PerlocutionaryValueSet values){
		final Utterance inserted = Utterance.create(who, symboldata.get(what), values);
		final Symbol from = this.dialogueTree.utterance.getWhat();
		final Symbol to = symboldata.get(what);
		final Connection connection = 			believes.programmedConnections.getFromTo(from, to)
				.orElseThrow(()->new NoSuchElementException(
					"connection from " +from + " to " + to + " doesn't exist"
				));
		DialogueTree newTree = DialogueTree.createFromConnection(connection.setRestrictedTo(who));

		DialogueTreeBuilder subBuilder = createWithRoot(newTree);
		commands.accept(subBuilder);
		DialogueTree modified = subBuilder.getCurrent();
		this.dialogueTree = this.dialogueTree.addOption(modified);
		return modified;
	}

	public DialogueTree getCurrent(){
		return dialogueTree;
	}

	public DialogueTree withOption(Utterance which, Consumer<DialogueTreeBuilder> commands){
		final DialogueTreeBuilder treebuilder = this;
		dialogueTree = dialogueTree.replaceOptions(
			dialogueTree.getOptions().map(
				current -> {
					if(current.utterance.equals(which)){
						DialogueTreeBuilder argument = copy(treebuilder);
						argument.dialogueTree = current;
						commands.accept(argument);
						return argument.getCurrent();
					}else{
						return current;
					}
				}
			).collect(Collectors.toList())
		);
		return dialogueTree;
	}
}
