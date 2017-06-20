package org.salve.personality.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.salve.drools.Functions;
import org.salve.drools.model.*;
import org.salve.drools.model.db.ConnectionDatabase;
import org.salve.drools.model.values.PerlocutionaryValueSet;
import org.salve.drools.model.values.PersonalValues;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable believes
 *
 * The entire knowledge base of the agent
 */
@Immutable
public class Believes {
	// TODO record achieved goals?
	// TODO perhaps we should start grouping fields
	// for example rather than having programmedconnections & learned
	// connections as 2 field, make one connections field with as ubfield programmed & learned
	public final ConnectionDatabase programmedConnections;
	/**
	 * Si uses this for example to store previously used replies in
	 */
	public final ConnectionDatabase learnedConnections;
	public final Set<Goal> goals;
	public final PersonalValues values;
	public final PersonalValues learnedValues;
	private final Set<Actor> actors;
	public final Actor self;
	public final Personality personality;

	/**
	 * List of utterances made
	 *
	 * First item in the pair is the utterer, second item is what he said.
	 */
	private final List<Utterance> previousUtterances;

	/**
	 * private so that we don't need to make shallow coppies from collections
	 * the create function should be used (which makes the shallow coppies)
	 * @param database
	 * @param learnedConnections
	 * @param goals
	 * @param values
	 * @param learned
	 * @param previousUtterances
	 * @param actors
	 * @param self
	 * @param personality
	 */
	private Believes(
		ConnectionDatabase database,
		ConnectionDatabase learnedConnections,
		Set<Goal> goals,
		PersonalValues values,
		PersonalValues learned,
		List<Utterance> previousUtterances,
		Set<Actor> actors,
		Actor self,
		Personality personality) {
		this.programmedConnections = database;
		this.learnedConnections = learnedConnections;
		this.goals = goals;
		this.values = values;
		this.learnedValues = learned;
		this.previousUtterances = previousUtterances;
		this.actors = actors;
		this.self = self;
		this.personality = personality;
	}

	/**
	 * Create believes, sets fields that have defaults to their defaults.
	 * Often these defaults would have 'setter' operations.
	 * @param database
	 * @param goals
	 * @param values
	 * @param actors
	 * @return
	 */
	public static Believes create(
		ConnectionDatabase database,
		Set<Goal> goals,
		PersonalValues values,
		Set<Actor> actors,
		Actor self,
		Personality personality
	){
		return Believes.create(
			database,
			ConnectionDatabase.empty,
			goals,
			values,
			PersonalValues.empty,
			Lists.newArrayList(),
			actors,
			self,
			personality);
	}

	/**
	 * Allows creation of believes the hard way. This allows you to specify
	 * fields that don't have setters, such as personality
	 *
	 * Makes shallow copies of mutable collections
	 * @param database
	 * @param learnedConnections
	 * @param goals
	 * @param values
	 * @param learned
	 * @param previousUtterances
	 * @param actors
	 * @param self
	 * @param personality
	 * @return
	 */
	public static Believes create(
		ConnectionDatabase database,
		ConnectionDatabase learnedConnections,
		Set<Goal> goals,
		PersonalValues values,
		PersonalValues learned,
		List<Utterance> previousUtterances,
		Set<Actor> actors,
		Actor self,
		Personality personality
	){
		return new Believes(
			database,
			learnedConnections,
			Sets.newHashSet(goals),
			values,
			learned,
			Lists.newArrayList(previousUtterances),
			Sets.newHashSet(actors),
			self,
			personality
		);
	}
	public static Believes copy(Believes from){
		return Believes.create(
			from.programmedConnections,
			from.learnedConnections,
			from.goals,
			from.values,
			from.learnedValues,
			from.previousUtterances,
			from.actors,
			from.self,
			from.personality
		);
	}
	@Override
	public boolean equals(Object obj){
		return Functions.equalsWith(this, obj,
			other -> this.goals.equals(other.goals) &&
				this.programmedConnections.equals(other.programmedConnections) &&
				this.learnedConnections.equals(other.learnedConnections) &&
				this.goals.equals(other.goals) &&
				this.values.equals(other.values) &&
				this.learnedValues.equals(other.learnedValues) &&
				this.previousUtterances.equals(other.previousUtterances) &&
				this.actors.equals(other.actors) &&
				this.self.equals(other.self)
		);
	}
	/**
	 * Returns a copy of this instance with the utterance appended to the
	 * previous utterances
	 * @param utterance
	 * @return
	 */
	public Believes addUtterance(Utterance utterance){
		Believes result = copy(this);
		result.previousUtterances.add(utterance);
		return result;
	}

	public Believes setGoals(Set<Goal> to){
		return new Believes(
			this.programmedConnections,
			this.learnedConnections,
			Sets.newHashSet(to),
			this.values,
			this.learnedValues,
			this.previousUtterances,
			this.actors,
			this.self,
			personality);
	}

	public Believes setLearnedConnections(ConnectionDatabase to){
		return new Believes(
			this.programmedConnections,
			to,
			this.goals,
			this.values,
			this.learnedValues,
			this.previousUtterances,
			this.actors,
			this.self,
			personality);
	}
	public Believes setLearnedValues(PersonalValues to){
		return new Believes(
			this.programmedConnections,
			this.learnedConnections,
			this.goals,
			this.values,
			to,
			this.previousUtterances,
			this.actors,
			this.self,
			personality);
	}

	public Stream<Utterance> getUtterances(){
		return previousUtterances.stream();
	}
	public Optional<Utterance> lastUtterance(){
		return getUtterances().reduce((a, b) -> b);
	}

	/**
	 * Tries to find a conection from the last utterance to the given infromative
	 * @param infor
	 * @return
	 */
	public Optional<Connection> findToFromLastUttTo(Informative infor){
		final Optional<Symbol> last = lastUtterance().map(Utterance::getWhat);
		return last
			// db returns an option and last already was an option, flatmap makes
			// it into a single layered option
			.flatMap(lastval -> programmedConnections
				.get(lastval)
				// flattens the result from the get and the result of find any
				// (both are options)
				.flatMap(con ->
					con.stream().filter( x -> x.to.equals(infor.what)
						&& x.restricted_to.equals(infor.who)
					)
					.findAny()
				)
			);
	}
	public Stream<Utterance> getUtterancesReversed(){
		return Lists.reverse(previousUtterances).stream();
	}
	public Stream<Actor> getActors(){
		return this.actors.stream();
	}

	public Actor getOtherActer(Utterance utt){
		return getActors()
			.filter(x -> !x.equals(utt.getByWhom()))
			.findAny()
			 // used to return an option, but all cases directly or it with this
			.orElse(Actor.any);
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Believes(");
		for(Field field : this.getClass().getFields()){
			builder.append(field.getName());
			builder.append(":");
			try {
				builder.append(field.get(this).toString());
			} catch (IllegalAccessException e) {
				throw new RuntimeException("impossible", e);
			}
			builder.append(",");
		}
		builder.append(")");
		return  builder.toString();
	}
	public Optional<Actor> getAnotherActor(){
		return actors.stream()
			.filter(x->!x.equals(self))
			.findAny();
	}

	public Stream<Connection> findProgrammedConnections(Utterance utt){
		return programmedConnections
			.getConnectionsExpand(utt.getWhat(), this::getActors)
			.filter(connection ->
				connection.before.map(x->x.isBefore(getUtterancesReversed())).orElse(true)
			)
			.filter(connection -> connection.queries.findMatch(getUtterances()).isPresent());
	}
	public Stream<DialogueTree> findProgrammedOptions(Utterance utt){
		return findProgrammedConnections(utt)
			.map(
				DialogueTree::createFromConnection
			);
	}
}
