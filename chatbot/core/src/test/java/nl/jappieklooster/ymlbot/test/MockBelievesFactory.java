package nl.jappieklooster.ymlbot.test;

import com.google.common.collect.Sets;
import io.atlassian.fugue.Either;
import org.salve.drools.model.*;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.db.SymbolDatabase;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.values.PersonalValues;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.salve.personality.model.Believes;
import org.salve.personality.model.Personality;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The point of this class is to create an agent believe base without having to
 * depend on the yml infrastructure (which is complex in its own right
 * but straigtforward)
 */
public class MockBelievesFactory {

	public static final Actor actor_any = Actor.any;
	public static final Actor actor_doctor = Actor.create("doctor");
	public static final Actor actor_patient = Actor.create("patient");

	public static final Scene scene_intro = new Scene("introdction");
	public static final Scene scene_diagnoses = new Scene("diagnoses");
	public static final Scene scene_finish = new Scene("finish");

	public final Map<String, Symbol> symbols;
	public final Map<Symbol, Set<Connection>> symbolListMap = new HashMap<>();
	private final SymbolDatabase  symboldb;

	public MockBelievesFactory(){
		SymbolData data = new SymbolData();
		data.put(scene_intro, hellos, "Hello", "Hi!", "Ni houw");
		data.put(scene_intro, whyhere, "why are you here?");
		data.put(scene_intro, maybeimsick, "Perhaps I'm just a sick guy!");
		data.put(scene_intro, ilikevistingyou, "I enjoy seeing you once in a while doctor");
		data.put(scene_intro, imthedoctor, "I'm the doctor");
		data.put(scene_diagnoses, needmedicine, "I need some medicine");
		data.put(scene_diagnoses, areyousick, "Are you sick?");
		data.put(scene_diagnoses, yesnomaybe, "Yes", "No", "Maybe");
		data.put(scene_finish, havemedicine, "Have some medicine");
		data.put(scene_finish, okthanks, "Thanks");
		data.put(scene_finish, goodbyes, "Bye,", "see you later, spacecoweboy");
		data.put(scene_finish, badbyes, "hate you", "go home");
		data.put(scene_finish, showdoor, "The door is over there");
		symbols = data.symbols
			.entrySet()
			.stream()
			.map(x -> new Symbol(x.getKey().getValue1(), x.getValue(), x.getKey().getValue0(), Sets.newHashSet()))
			.collect(Collectors.toMap(x -> x.name, x -> x));
		symboldb = new SymbolDatabase(symbols);
	}

	public static final String hellos = "hellos";
	public static final String whyhere = "whyhere";
	public static final String maybeimsick = "maybeimsick";
	public static final String ilikevistingyou = "likevisitingyou";

	public static final String needmedicine= "needmedicine";
	public static final String imthedoctor= "imthedoctor";

	public static final String areyousick= "areyousick?";
	public static final String yesnomaybe= "yesnomaybe";
	public static final String havemedicine= "havesomemedicine";
	public static final String okthanks= "thanks";

	public static final String goodbyes = "goodbyes";
	public static final String badbyes = "badbyes";
	public static final String showdoor = "showdoor";

	public final Believes createTestBelieves(){
		connect(hellos,
			any(whyhere, "Angry"),
			any(hellos, "Happy"),
			any(needmedicine, "Persuading", "Scary")
		);
		connect(whyhere,
			restricted(needmedicine, actor_patient, "Enlightening"),
			restricted(imthedoctor, actor_doctor, "Angry"),
			restricted(maybeimsick, actor_patient, "Angry"),
			restricted(ilikevistingyou, actor_patient, "Happy")
		);
		connect(imthedoctor, any(okthanks));
		connect(needmedicine,
			restricted(areyousick, actor_doctor, "Scary"),
			restricted(havemedicine, actor_doctor, "Happy")
		);
		connect(areyousick, any(yesnomaybe));
		connect(yesnomaybe, any(havemedicine));
		connect(havemedicine, any(okthanks));
		connect(okthanks,
			any(goodbyes, "Enlightening"),
            any(badbyes, "Sad"),
            any(areyousick)
		);
		connect(goodbyes, any(showdoor, "Enlightening"));
		connect(badbyes, any(showdoor, "Sad"));

		final ConnectionDatabase database = new ConnectionDatabase(symbolListMap);

		PersonalValues values = PersonalValues.empty;
		values = values.set(PerlocutionaryValue.create("Happy"), 10);
		values = values.set(PerlocutionaryValue.create("Persuading"), 2);
		values = values.set(PerlocutionaryValue.create("Scary"), -5);
		values = values.set(PerlocutionaryValue.create("Angry"), -10);
		values = values.set(PerlocutionaryValue.create("Enlightening"), 4);
		values = values.set(PerlocutionaryValue.create("Sad"), -100);

		final Set<Actor> actors =Sets.newHashSet(actor_doctor, actor_patient);

		Set<Goal> goals = Sets.newHashSet(
			new Goal(300,
				new Informative(actor_any, symboldb.getOrThrow(showdoor))
			),
			new Goal(50, new Informative(actor_doctor, symboldb.getOrThrow(havemedicine))),
			new Goal(10, new Informative(actor_patient, symboldb.getOrThrow(needmedicine))),
			new Goal(4, new Informative(actor_doctor, symboldb.getOrThrow(whyhere)))
		);
		final Believes believes = Believes.create(
			database,
			goals,
			values,
			actors,
			actor_patient,
			Personality.empty
		);
		return believes;
	}
	static class SymbolData{
		public final Map<Pair<Scene, String>, List<String>> symbols = new HashMap<>();
		public void put(Scene scene, String key, String... values){
			this.symbols.put(new Pair<>(scene, key),Arrays.asList(values));
		}
	}
	public Connection createConnection(String symbol, Actor actor, PerlocutionaryValueSet values){
		return Connection.create(
			symboldb.getOrThrow(symbol),
			actor,
			values
		);
	}

	public void setconnect(String one, Set<Connection> two){
		Set<Connection> connections = Sets.newHashSet(two);
		Symbol symbol = symboldb.getOrThrow(one);
		if (symbolListMap.containsKey(symbol)){
			connections.addAll(symbolListMap.get(symbol));
		}
		symbolListMap.put(symbol, connections);
	}
	/**
	 * convencie function for connect
	 * @param one
	 * @param values use pair or triplle
	 */
	@SafeVarargs
	public final void connect(
		String one,
		Either<Pair<String, PerlocutionaryValueSet>, Triplet<String, Actor, PerlocutionaryValueSet>>...values
	){
		Set<Connection> connections = createConnections(values);
		setconnect(one, connections);
	}
	@SafeVarargs
	public final Set<Connection> createConnections(
			Either<Pair<String, PerlocutionaryValueSet>, Triplet<String, Actor, PerlocutionaryValueSet>>...values
		){
		return Arrays.asList(values).stream().map(tupple ->
			tupple.fold(pair ->
					createConnection(pair.getValue0(), actor_any, pair.getValue1()),
					tripple ->
					createConnection(tripple.getValue0(), tripple.getValue1(), tripple.getValue2())
				)
		).collect(Collectors.toSet());
	}
	/**
	 * wraps arguments in a pair and puts it into an either
	 * @param symbol
	 * @param values
	 * @return
	 */
	public static Either<Pair<String, PerlocutionaryValueSet>, Triplet<String, Actor, PerlocutionaryValueSet>>
	any(String symbol, String... values){
		return Either.left(new Pair<>(symbol, PerlocutionaryValueSet.create(values)));
	}

	/**
	 * wraps arguments in a triplet and puts it into an either
	 * @param symbol
	 * @param actor
	 * @param values
	 * @return
	 */
	public static Either<Pair<String, PerlocutionaryValueSet>, Triplet<String, Actor, PerlocutionaryValueSet>>
	restricted(String symbol, Actor actor, String... values){
		return Either.right(new Triplet<>(symbol, actor, PerlocutionaryValueSet.create(values) ));
	}
}
