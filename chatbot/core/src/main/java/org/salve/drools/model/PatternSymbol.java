package org.salve.drools.model;

import org.javatuples.Pair;
import org.salve.drools.Functions;
import org.salve.drools.SymbolCapture;
import org.salve.drools.model.template.db.CapturedMatchDB;

import javax.annotation.concurrent.Immutable;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Immutable Pattern symbol tupple
 *
 * This is a mapping from a regex to a symbol.
 * This will be use to see if the user utterence complies with the regex,
 * if so a symbol is attached to that regex.
 * With this symbol the connectiondatabase can be queried to see how this symbol
 * fits in the broader system.
 */
@Immutable
public class PatternSymbol {
	public final Pattern pattern; // its immutable too (see doc)
	public final Symbol symbol;
	private final int hashValue;
	/**
	 * used for comparing of pattern , the java pattern uses the object
	 * implementation which is identity rather than equality
	 */
	private final String rawRegexPattern;

	public PatternSymbol(Pattern pattern, Symbol symbol) {
		this.pattern = pattern;
		this.symbol = symbol;
		rawRegexPattern = pattern.pattern();
		hashValue = symbol.hashCode() * 71 - rawRegexPattern.hashCode() * 59;
	}

	public static Stream<SymbolCapture> match_respond(Set<PatternSymbol> patterns, String str){
		return patterns.stream()
			.map(ps ->  new Pair<>(ps.pattern.matcher(str), ps))
			.filter(x -> x.getValue0().find())
			.map(pair ->
				new SymbolCapture(
					pair.getValue1().symbol,
					CapturedMatchDB.create(pair.getValue1().pattern, pair.getValue0())
				)
			);
	}

	@Override
	public String toString(){
		return "PatternSymbol(" + rawRegexPattern +"->" + symbol.scene +"/" + symbol.name + ")";
	}
	@Override
	public int hashCode(){
		return hashValue;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other -> rawRegexPattern.equals(other.rawRegexPattern) &&
				symbol.equals(other.symbol)
		);
	}

}
