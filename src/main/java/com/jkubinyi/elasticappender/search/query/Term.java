package com.jkubinyi.elasticappender.search.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLeafLevelGenerator;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Is best used to exact match the selected value.
 * 
 * @author jurajkubinyi
 */
public class Term extends AbstractLeafLevelGenerator {

	private Term(Field field, Object value) {
		super(field, value);
	}

	private Term(Field field, Object value, ComparisonOperator operator) {
		super(field, value, operator);
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value     Value which will be document's values evaluated against.
	 * @param operator  Condition against which will be the field's value evaluated.
	 * @return Leaf query instance.
	 */
	public static Term of(Field field, Object value, ComparisonOperator operator) {
		return new Term(field, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value     Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static Term of(Field field, Object value) {
		return new Term(field, value);
	}

	@Override
	protected QueryBuilder buildLeafQuery() {
		return QueryBuilders.termQuery(this.getField().toKeywordString(), this.getValue().orElse(""));
	}
}