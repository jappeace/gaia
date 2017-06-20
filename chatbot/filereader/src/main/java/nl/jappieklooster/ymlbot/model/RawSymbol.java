package nl.jappieklooster.ymlbot.model;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.apache.commons.io.FilenameUtils;
import org.salve.drools.model.Scene;
import org.salve.drools.model.Symbol;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.db.SymbolDatabase;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.STLexer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A symbol that was just read from the file and hasn't
 * been processed yet
 *
 * Processing involves changing both patterns and regexes to Patterns
 * (pre compiled regexes)
 * and the remaining data gets inserted in Symbol
 * A list of the compiled regexes then points towards the symbols.
 */
public class RawSymbol {
	public String name; // defined implicitly by filename, user can't override
	public String scene; // defined implicitly by folder, user can't override
    // arraylists are almost always better
    // see: http://stackoverflow.com/questions/322715/when-to-use-linkedlist-over-arraylist
	public List<String> literals = new ArrayList<>(1);
	public List<String> patterns = new ArrayList<>(1);
	public List<String> regexes = new ArrayList<>(1);

	public Symbol create(){
		// we want to find *all* defined attributes, they're required,
		// even if there exist default literals,
		// because having extra is probably a bug.
		Set<TemplateAttribute> required = this.literals.stream()
			.map(ST::new)
			.flatMap(st ->
				getListOfAttributes(st).stream()
			)
			.map(TemplateAttribute::new)
			.collect(Collectors.toSet());

		return new Symbol(this.name, this.literals, new Scene(this.scene), required);
	}

	public static Symbol get(SymbolDatabase db, String scene, String name){
		return db.get(FilenameUtils.concat(scene, name)).orElseThrow(
			()-> new RuntimeException("Could not find" + name + " in " + scene)
		);
	}

	public static Set<String> getListOfAttributes(ST template){
		Set<String> expressions = new HashSet<String>();
		TokenStream tokens = template.impl.tokens;
		for (int i = 0; i < tokens.range(); i++) {
			Token token = tokens.get(i);
			if (token.getType() == STLexer.ID) {
				expressions.add(token.getText());
			}
		}
		return expressions;
	}
}
