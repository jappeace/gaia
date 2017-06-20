package org.salve.drools.model.db;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.javatuples.Pair;
import org.salve.drools.SymbolCapture;
import org.salve.drools.model.PatternSymbol;
import org.salve.drools.model.Scene;
import org.salve.drools.model.Symbol;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.salve.drools.UnparsedUserUtterance;

/**
 * Immutable pattern database
 *
 * used by drools to figure out what patterns were matched
 * They are grouped per scene.
 */
@Immutable
public class PatternDatabase extends Database<Scene, Set<PatternSymbol>>{
	public PatternDatabase(Map<Scene, Set<PatternSymbol>> data) {
		super(data);
	}

	public List<SymbolCapture> parseUtterance(UnparsedUserUtterance utt, Scene activeScene){
		return get(activeScene).map(patterns ->
			PatternSymbol.match_respond(
				patterns, utt.getValue()
			)
			.collect(Collectors.toList())
		).orElse(Collections.emptyList());
	}
}
