package org.salve.drools;

import com.google.common.collect.Lists;
import org.salve.drools.model.Symbol;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Immutable
public class ParsedUtterance {
	private final List<SymbolCapture> symbolSocialPracticeList;
	private final boolean isEmpty;

	public ParsedUtterance(List<SymbolCapture> symbolSocialPracticeList) {
		this.symbolSocialPracticeList = Lists.newArrayList(symbolSocialPracticeList);
		this.isEmpty = symbolSocialPracticeList.isEmpty();
	}

	public Stream<SymbolCapture> getSymbolSocialPracticeList() {
		return symbolSocialPracticeList.stream();
	}
	public List<SymbolCapture> getSymbolCapturedMatchDBList(){
		return getSymbolSocialPracticeList().collect(Collectors.toList());
	}

	public List<Symbol> symbolList(){
		return getSymbolSocialPracticeList().map(x->x.symbol).collect(Collectors.toList());
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public String toString(){
		return symbolSocialPracticeList.toString();
	}
}
