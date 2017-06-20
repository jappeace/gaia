package org.salve.drools;

import org.salve.drools.model.Symbol;
import org.salve.drools.model.template.db.CapturedMatchDB;

public class SymbolCapture {
	public final Symbol symbol;
	public final CapturedMatchDB db;

	public SymbolCapture(Symbol symbol, CapturedMatchDB db) {
		this.symbol = symbol;
		this.db = db;
	}

	@Override
	public String toString(){
		return symbol + " & " + db;
	}
}
