package com.jkubinyi.elasticappender.search.common;

/**
 * ConditionType is used to group leaf queries on the same level together making
 * a logical conditions.
 * 
 * @author jurajkubinyi
 */
public enum ConditionType {
	/** Equivalent to the AND. */
	must,

	/** Equivalent of the NOT. */
	mustNot,

	/** Equivalent of the OR. */
	should,

	/** Part of the query which results won't be used in wrap query. */
	filter,

	/** Used in specific use cases where none of the above makes sense. */
	none
}