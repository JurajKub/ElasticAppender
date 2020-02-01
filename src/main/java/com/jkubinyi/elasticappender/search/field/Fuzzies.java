package com.jkubinyi.elasticappender.search.field;

import com.jkubinyi.elasticappender.search.field.MatchField.Fuzzy;

/**
 * Collection of most used {@link Fuzzy} configurations.
 * 
 * @author jurajkubinyi
 */
public class Fuzzies {
	/**
	 * <p>
	 * Default Fuzzy configuration used by the Elasticsearch.
	 * </p>
	 * Configuration is as stated:
	 * <ul>
	 * <li><b>Fuzziness:</b> AUTO</li>
	 * <li><b>Max expansions:</b> 50</li>
	 * <li><b>Prefix length:</b> 0</li>
	 * <li><b>Transpositions:</b> true</li>
	 * </ul>
	 */
	public static final Fuzzy DEFAULT_AUTO = new Fuzzy();

	/**
	 * Used to mark a disabled fuzzy search in {@link LogField} implementation.
	 */
	public static final Fuzzy DISABLED = new Fuzzy();
}
