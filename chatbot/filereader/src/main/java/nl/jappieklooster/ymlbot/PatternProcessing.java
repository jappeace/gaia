package nl.jappieklooster.ymlbot;

import org.javatuples.Pair;
import org.salve.drools.Functions;
import org.salve.drools.model.PatternSymbol;
import org.salve.drools.model.Symbol;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.db.PatternDatabase;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PatternProcessing {
	public static PatternDatabase createSceneContained(Map<Symbol, Set<Pattern>> from){
		return new PatternDatabase(
			Functions.streamToHashMapSet(
				flatten(from),
				key -> key.symbol.scene,
				Function.identity()
			)
		);
	}
	public static PatternDatabase createSceneNextTo(Map<Symbol, Set<Pattern>> from, ConnectionDatabase db){
		ConnectionDatabase dual = db.createDual();
		return new PatternDatabase(
			Functions.streamToHashMapSet(
				flatten(from)
				.flatMap(patternSymbol ->
					dual.getConnections(patternSymbol.symbol)
						.filter(connection -> !connection.to.scene.equals(patternSymbol.symbol.scene))
						.map(connection -> new Pair<>(connection.to.scene, patternSymbol))

				),
				Pair::getValue0,
				Pair::getValue1
			)
		);
	}
	public static Stream<PatternSymbol> flatten(Map<Symbol, Set<Pattern>> from){
		return from.entrySet()
			.stream()
			.flatMap(entry ->
				entry.getValue().stream().map(
					pattern -> new PatternSymbol(pattern, entry.getKey())
				)
			);
	}
}
