package org.salve.drools.model.template;

import org.salve.drools.Functions;
import org.salve.drools.model.Informative;

import java.util.function.Function;

public class InsertQuery {
	public final Informative query;
	public final TemplateAttribute match;

	private final int hash_value;
	public InsertQuery(Informative query, TemplateAttribute match) {
		this.query = query;
		this.match = match;
		this.hash_value = query.hashCode() * 3 - match.hashCode();
	}
	@Override
	public int hashCode(){
		return hash_value;
	}
	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other ->
				this.match.equals(other.match) &&
				this.query.equals(other.query)
		);
	}
	public String toString(){
		return "Query("+query+", "+ match + ")";
	}
}
