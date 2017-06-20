package nl.jappieklooster.ymlbot.model;

import org.apache.commons.io.FilenameUtils;
import org.salve.drools.Functions;
import org.salve.drools.model.Actor;
import org.salve.drools.model.Before;
import org.salve.drools.model.Informative;
import org.salve.drools.model.db.SymbolDatabase;

import java.util.Optional;
import java.util.logging.Logger;

public class RawBefore {
	public String who;
	public String said;
	public String scene = RawConnection.same_scene;
	public RawBefore before = none;
	public Optional<Before> create(SymbolDatabase db){

		final String name = FilenameUtils.concat(this.scene, this.said);
		Optional<Before> result = db.get(name).map(symbol -> new Before(before.create(db), new Informative(Actor.create(who), symbol)));
		if(!result.isPresent()){
			Logger.getLogger("construction").warning("Could not construct before for " + name + ", not present, is it spelled correctly?");
		}
		return result;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj, other ->
			who.equals(other.who) &&
			said.equals(other.said) &&
			before.equals(other.before)
		);
	}

	public static final RawBefore none = new NoBefore();
	static class NoBefore extends RawBefore{
		@Override
		public Optional<Before> create(SymbolDatabase db){
			return Optional.empty();
		}
		@Override
		public boolean equals(Object obj){
			return obj instanceof NoBefore;
		}
	}
}
