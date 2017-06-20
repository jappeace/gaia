package nl.jappieklooster.ymlbot.model;

import com.google.common.collect.Lists;
import org.salve.drools.model.Actor;
import org.salve.drools.model.Goal;
import org.salve.drools.model.Informative;
import org.salve.drools.model.Utterance;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.db.SymbolDatabase;
import org.salve.drools.model.values.PerlocutionaryValue;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.values.PersonalValues;
import org.salve.personality.JungianFunction;
import org.salve.personality.model.Believes;
import org.salve.personality.model.Personality;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RawBelieves {
	public List<RawGoal> goals = new ArrayList<>();
	public Map<String, Integer> values = new HashMap<>();
	public String self;
	public Set<String> actors;
	public List<JungianFunction> personality;


	public Believes create(ConnectionDatabase conss, SymbolDatabase symbols){
		return Believes.create(
			conss,
			this.createGoals(symbols),
			this.createValues(),
			this.createActors(),
			Actor.create(this.self),
			new Personality(personality)
		);
	}
	public Set<Goal> createGoals(SymbolDatabase db){
		final List<RawGoal> revgoal = Lists.reverse(goals);
		return IntStream.range(0, goals.size())
			.mapToObj(x->new Goal(x,
				new Informative(
					Actor.create(revgoal.get(x).actor),
					db.get(revgoal.get(x).scene+"/"+revgoal.get(x).symbol).get()
				)
				)).collect(Collectors.toSet());
	}

	public PersonalValues createValues(){
		PersonalValues result = PersonalValues.empty;
		for(Map.Entry<String, Integer> entry : values.entrySet()){
			result = result.set(PerlocutionaryValue.create(entry.getKey()), entry.getValue());
		}
		return result;
	}

	public Set<Actor> createActors(){
		Set<Actor> result = actors.stream().map(Actor::create).collect(Collectors.toSet());
		result.add(Actor.create(self)); // allow user to "forget".
		return result;
	}
}
