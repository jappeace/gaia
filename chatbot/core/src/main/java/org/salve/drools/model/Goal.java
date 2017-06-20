package org.salve.drools.model;

import org.salve.drools.Functions;

import javax.annotation.concurrent.Immutable;
import java.util.function.Function;


/**
 * The only purpose of this class is to make goals comparable to each other.
 * We need to know which are preffered for the T functions to do their job
 */
@Immutable
public class Goal implements Comparable<Goal>{
	public final int utility;
	// perhaps utterance isn't the right datatype
	// since we just plainly ignore perlocutionary values
	public final Informative toSay;
	private final int hash_value;

	public Goal(int utility, Informative toSay) {
		this.utility = utility;
		this.toSay = toSay;
		this.hash_value = toSay.hashCode() - utility * 71;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Goal other) {
		return this.utility-other.utility;
	}

	@Override
	public int hashCode(){
		return hash_value;
	}

	@Override
	public boolean equals(Object obj){
		return Functions.equalsAs(this, obj).fold(Function.identity(),
			other -> this.utility == other.utility &&
				this.toSay.equals(other.toSay)
		);
	}

	public boolean isGoal(Utterance utt){
		return this.toSay.equals(utt.informative);
	}
	public String toString(){
		return "Goal(utility:"+utility+", toAchieve:" + toSay +")";
	}
}
