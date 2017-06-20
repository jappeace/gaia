package nl.jappieklooster.ymlbot.model;

import org.salve.drools.model.*;
import org.salve.drools.model.db.SymbolDatabase;
import org.salve.drools.model.template.InsertQuery;
import org.salve.drools.model.template.TemplateAttribute;

public class RawInsertQuery {

	public String symbol;
	public String match;

	public String actor = Actor.anyName;

	/**
	 * If it changes scene, to which
	 */
	public String scene = RawConnection.same_scene;


	public final InsertQuery create(SymbolDatabase db, String default_scene){
		if(scene.equals(RawConnection.same_scene)){
			scene = default_scene;
		}
		Symbol sym = RawSymbol.get(db, scene, symbol);
		Informative informative = new Informative(Actor.create(actor), sym);
		return new InsertQuery(informative, new TemplateAttribute(match));
	}


}
