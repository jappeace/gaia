package org.salve.drools.model.db;

import org.salve.drools.model.Symbol;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

/**
 * Immutable encapsulation of the symbols
 *
 * This class is only used to support the construction of Connection database.
 * This class is a lookup table for symbols based upon their names.
 * Connectiondatabase is however a lookuptable based upon the symbols
 * themselves.
 * Which is much more convienient since no intermediate step is
 * required between having a connection and getting a symbol (it used to be the
 * case this class was used to get the to value which was a string, now its a
 * symbol)
 */
@Immutable
public class SymbolDatabase extends Database<String, Symbol> {
	public SymbolDatabase(Map<String, Symbol> table) {
		super(table);
	}

}
