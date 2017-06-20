package org.salve.drools;

import com.google.common.collect.Sets;
import io.atlassian.fugue.Either;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Functions that have no place.
 *
 * basically me extending the standard java library
 */
@Immutable
public class Functions {
	/**
	 * Does the dumb equals check and cast to the type you're checking against.
	 * @param current usually the "this" keyword in an equals function
	 * @param two the object to check equality against
	 * @param <T> The type cassed to
	 * @return Either a boolean, which means you have a type mismatch or an
	 * identity match, or the object now cast to the right type
	 */
	@SuppressWarnings("unchecked")
	public static <T> Either<Boolean, T> equalsAs(T current, Object two){
		if(two == null){
			return Either.left(false);
		}
		if(current == two){
			return Either.left(true);
		}
		Class<?> clas = current.getClass();
		if(!clas.isInstance(two)){
			return Either.left(false);
		}
		return Either.right((T) clas.cast(two));
	}

	/**
	 * Assumes ou want to use Function.identity for the first for the boolean
	 * in the either type. makes the code just a little better readable.
	 * @param current
	 * @param two
	 * @param func
	 * @param <T>
	 * @return
	 */
	public static <T> boolean  equalsWith(T current, Object two, Function<T, Boolean> func){
		return Functions.equalsAs(current, two).fold(Function.identity(),
			func
		);
	}

	/**
	 * Much faster than the modulo thingy,
	 * only works with ints (I guess the modulos thingy too)
	 * source: https://stackoverflow.com/questions/7342237/check-whether-number-is-even-or-odd
	 * @param integer
	 * @return
	 */
	public static boolean isEven(int integer){
		 return (integer & 1) == 0;
	}

	/**
	 * stream api preffering longs... i dunno I guess this still works
	 * @param integer
	 * @return
	 */
	public static boolean isEven(long integer){
		return (integer & 1) == 0;
	}

	/**
	 * ... why can't a string array do this anyway
	 * @param strings
	 * @return
	 */
	public static String concat(String... strings){
		StringBuilder strBuilder = new StringBuilder();
		for(String str : strings){
			strBuilder.append(str);
		}
		return strBuilder.toString();
	}

	public static <T, K, V> Map<K, Set<V>> streamToHashMapSet(
		Stream<T> input,
		Function<T, K> keyfunc,
		Function<T, V> valuefunc
	){
		return input.collect(
			Collectors.toMap(
				keyfunc,
				// java generics are invariant, we need to cast to make the
				// compiler understand covarience which it does automatically
				// for regular types (ie inherentance)
				val -> (Set<V>) Sets.newHashSet(valuefunc.apply(val)),
				// if duplicates are found do this
				Sets::union
			)
		);
	}
}
