package org.salve.drools.model;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Immutable
public class Before {
	public final Optional<Before> before;
	public final Informative informative;

	public Before(Optional<Before> before, Informative informative) {
		this.before = before;
		this.informative = informative;
	}

	public static Before create(Informative informative){
		return new Before(Optional.empty(), informative);
	}

	public boolean isBefore(Stream<Utterance> previousUtts){
		List<Utterance> uttlist = previousUtts
			.filter(utt -> utt.informative.who.equals(informative.who))
			.collect(Collectors.toList());
		return uttlist.stream()
			.findFirst()
			.map(first ->
				first.informative.equals(informative) && // the actual check
				before.map(next -> {
					if(uttlist.size() < 2){
						// no other utts in list, but have another before
						return false;
					}
					// chop off current head (with a view)
					List<Utterance> nextUttList = uttlist.subList(1, uttlist.size() - 1);
					// recursion
					return  next.isBefore(nextUttList.stream());
				})
				.orElse(true) // no next before, so this part of the && is true
			).orElse(false); // no utts in the list, while we want one
	}
}
