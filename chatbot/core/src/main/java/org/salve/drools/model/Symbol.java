package org.salve.drools.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.salve.drools.Functions;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.template.db.MatchedQueryDatabase;
import org.stringtemplate.v4.ST;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable symbol
 *
 * We do not need to store the matching criteria in the symbol itself
 * This is stored in a seperate structure to point towards the symbols
 * Therefore we make a distinction between "raw" symbol, and the symbol.
 * A symbol is just enough information to deliberation upon.
 * The connections and a possible list of utterence associated with the symbol
 */
@Immutable
public class Symbol {
	public final String name; // filename
	public final Scene scene;

	private final List<String> literals;
	private final Set<TemplateAttribute> requiredTemplateVars;

	private final int hash_value;

	public Symbol(String name, List<String> literals, Scene scene, Set<TemplateAttribute> requiredTemplateVars) {
		this.name = name;
		this.literals = Lists.newArrayList(literals);
		this.requiredTemplateVars = Sets.newHashSet(requiredTemplateVars);
		// since the object is immutable, we can do this on construction
		this.hash_value = scene.hashCode()*3+name.hashCode()*7;
		this.scene = scene;
	}

	public Stream<TemplateAttribute> getRequiredTemplateVars(){
		return requiredTemplateVars.stream();
	}

	@Override
	public int hashCode(){
		return hash_value;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other ->
				this.name.equals(other.name) &&
				this.scene.equals(other.scene)
		);
	}
	@Override
	public String toString(){
		final String seperator = ", ";
		final String literals =
				this.literals.stream().reduce("", (a,b) -> b + seperator + a);
		final String literals_str = literals.length() > seperator.length() ? ", [" +
				literals.substring(0,literals.length()-seperator.length()) +
			"]" : "";
		return "Symbol(" + this.name + literals_str + ")";
	}
	public String randomLiteral(Random rng, MatchedQueryDatabase db){
		assert(db.keys().collect(Collectors.toSet()).equals(requiredTemplateVars));
		String selected = literals.get(rng.nextInt(literals.size()));
		ST template = new ST(selected);
		db.entries().forEach(entry ->
			template.add(entry.getKey().name, entry.getValue().name)
		);
		return template.render();
	}
	public Scene getScene(){
		return scene;
	}
}
