package com.jkubinyi.elasticappender.search.common;

import org.elasticsearch.common.unit.Fuzziness;

/**
 * Fuzzy search configuration for the leaf query.
 * 
 * @author jurajkubinyi
 */
public class Fuzzy {

	private final Fuzziness fuzziness;

	/** (Optional, integer) Maximum number of variations created. Defaults to 50. */
	private final int maxExpansions;

	/**
	 * (Optional, integer) Number of beginning characters left unchanged when
	 * creating expansions. Defaults to 0.
	 */
	private final int prefixLength;

	/**
	 * (Optional, boolean) Indicates whether edits include transpositions of two
	 * adjacent characters (ab → ba). Defaults to true.
	 */
	private final boolean transpositions;

	/**
	 * <p>
	 * Generates fuzzy search configuration.
	 * </p>
	 * For default fuzzy configuration which should fit most applications please see
	 * {@link Fuzzies}.
	 * 
	 * @param fuzziness      Maximum edit distance allowed for matching.
	 * @param maxExpansions  Maximum number of variations created.
	 * @param prefixLength   Number of beginning characters left unchanged when
	 *                       creating expansions.
	 * @param transpositions Indicates whether edits include transpositions of two
	 *                       adjacent characters (ab → ba).
	 */
	public Fuzzy(Fuzziness fuzziness, int maxExpansions, int prefixLength, boolean transpositions) {
		this.fuzziness = fuzziness;
		this.maxExpansions = maxExpansions;
		this.prefixLength = prefixLength;
		this.transpositions = transpositions;
	}

	protected Fuzzy() {
		this.fuzziness = Fuzziness.AUTO;
		this.maxExpansions = 50;
		this.prefixLength = 0;
		this.transpositions = true;
	}

	/**
	 * @return Gets the fuzziness used by the leaf query.
	 */
	public Fuzziness getFuzziness() {
		return fuzziness;
	}

	/**
	 * @return Gets the fuzzy max expansion used by the leaf query.
	 */
	public int getMaxExpansions() {
		return maxExpansions;
	}

	/**
	 * @return Gets the fuzzy prefix length used by the leaf query.
	 */
	public int getPrefixLength() {
		return prefixLength;
	}

	/**
	 * @return Checks if fuzzy transpositions should be used by the leaf query.
	 */
	public boolean hasTranspositions() {
		return transpositions;
	}
}