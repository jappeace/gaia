package org.salve.drools.model.template.db;

import com.google.common.collect.Maps;
import org.javatuples.Pair;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.db.Database;
import org.salve.drools.model.template.InsertQuery;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.template.TemplateValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryDatabase extends Database<TemplateAttribute, InsertQuery> {
	public static final QueryDatabase empty = new QueryDatabase(Maps.newHashMap());
	public QueryDatabase(Map<TemplateAttribute, InsertQuery> data) {
		super(data);
	}

	/**
	 * tries to collect all keys and put them in a matchedQueryDatabase.
	 * If a single key isn't found, none is returned.
	 *
	 * Note that this is theoretically an N^2 operation, although the amount of
	 * utts is expected to be low.
	 *
	 * Also note that if this db is empty it will always find a match,
	 * in constant time. We expect most connections to behave like that.
	 * @param utteranceStream
	 * @return
	 */
	public Optional<MatchedQueryDatabase> findMatch(Stream<Utterance> utteranceStream){

		Map<TemplateAttribute, TemplateValue> result = entries().flatMap(entry -> {
			Optional<TemplateValue> value = utteranceStream
				// filter out that don't satisfy the query
				.filter(x -> x.informative.equals(entry.getValue().query))
				// get the match we want (an utterance can have multiple named)
				// to make this work with findany, we flatmap out the none options
				.flatMap(x->
					x.capturedDB
						.get(entry.getValue().match)
						.map(Stream::of)
						.orElse(Stream.empty())
				)
				.findFirst();
			return value
				// don't forget the key we matched against
				.map(tempval -> Stream.of(new Pair<>(entry.getKey(), tempval)))
				// again getting rid of option
				.orElse(Stream.empty());
		}).collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));

		if(!keys().collect(Collectors.toSet()).equals(result.keySet())){
			// if not all keys are filled the query has failed
			return Optional.empty();
		}
		return Optional.of(new MatchedQueryDatabase(result));
	}
}
