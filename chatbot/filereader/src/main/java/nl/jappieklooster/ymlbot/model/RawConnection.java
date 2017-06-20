package nl.jappieklooster.ymlbot.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.javatuples.Pair;
import org.salve.drools.model.*;
import org.salve.drools.model.template.db.QueryDatabase;
import org.salve.drools.model.db.SymbolDatabase;
import org.salve.drools.model.template.TemplateAttribute;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.values.PerlocutionaryValueSet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The only difference between raw connection and connection is that connection
 * is immutable.
 */
public class RawConnection {
	public static RawConnection createDefaults(){
		return new RawConnection();
	}
	public RawBefore before = RawBefore.none;
	/**
	 * Name of where the connection goes to
	 */
	public String symbol;

	public static final String same_scene = "";
	/**
	 * If it changes scene, to which
	 */
	public String scene = same_scene;
	/**
	 * Sometimes certain connections don't make sense,
	 * For example a patient asking a doctor what the
	 * problem is isn't desirable.
	 *
	 * this field allows the user to specify such nonsensities by restricting
	 * it to an actor name
	 * by default we have no restrictions
	 */
	public String restricted_to = Actor.anyName;

	/**
	 * What expected emotions or mental effects does this connection have on
	 * the other party.
	 */
	public Set<String> values = Sets.newHashSet();

	public Map<String, RawInsertQuery> utterances = Maps.newHashMap();

	/**
	 * use reflection to figure out if we should overwrite values or not
	 * @param runtimeDefaultsValues
	 * @return
	 */
	public RawConnection overwriteIfDefault(RawConnection runtimeDefaultsValues){
		RawConnection result = RawConnection.createDefaults(); // with compile time defaults

		// we check and then set them to the runtime defaults (by the script)
		for(Field field : RawConnection.class.getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				// don't care about static fields, only instance fields
				continue;
			}
			try {
				Object thisValue = field.get(this);
				Object compileTimeValue = field.get(result);
				if(thisValue == compileTimeValue){
					// case of null
					field.set(result, field.get(runtimeDefaultsValues));
					continue;
				}
				// if we equal the compile time default, set runtime default
				if(thisValue.equals(compileTimeValue)){
					field.set(result, field.get(runtimeDefaultsValues));
				}else{
					// we have a custom value, use that
					field.set(result, thisValue);
				}
			} catch (IllegalAccessException e) {
				// this shouldn't even happen since everything is public and not final
			}
		}
		return result;
	}

	public final Connection create(SymbolDatabase db) throws TemplateAttributeNotFoundException {
		final Symbol symbol = RawSymbol.get(db, this.scene, this.symbol);
		if(same_scene.equals(before.scene)){
			before.scene = scene;
		}
		QueryDatabase queries = new QueryDatabase(utterances
			.entrySet()
			.stream()
			.map(entry -> new Pair<>(
				new TemplateAttribute(entry.getKey()),
				entry.getValue().create(db, this.scene)
			))
			.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1)));

		Set<TemplateAttribute> required = symbol.getRequiredTemplateVars().collect(Collectors.toSet());
		for(TemplateAttribute require : required){
			if(!queries.get(require).isPresent()){
				// the rare case where exceptions are useful, in this function we
				// don't have all info for a proper error message
				// since we *want* to crash at this point, might as wel throw
				// an exception
				throw new TemplateAttributeNotFoundException(require, queries, symbol);
			}
		}


		return new Connection(
			before.create(db),
			symbol,
			Actor.create(this.restricted_to),
			PerlocutionaryValueSet.create(this.values.stream().map(PerlocutionaryValue::create).collect(Collectors.toSet())),
			queries
		);
	}

	public class TemplateAttributeNotFoundException extends Exception{
		public final TemplateAttribute require;
		public final QueryDatabase queries;
		public final Symbol symbol;

		TemplateAttributeNotFoundException(TemplateAttribute require, QueryDatabase queries, Symbol symbol) {
			this.require = require;
			this.queries = queries;
			this.symbol = symbol;
		}

		@Override
		public String getMessage(){
			return "Could not find " + require + " in queries " + queries +
				" of connection to symbol " + symbol +
				". Please make sure to define a query in yaml, " +
				"for example by specifying which utterance " + require +
				" can be extracted from. " +
				"Crashing because in the current setup this connection will " +
				"never be used.";
		}
	}
}
