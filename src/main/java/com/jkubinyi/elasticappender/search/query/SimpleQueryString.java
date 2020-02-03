package com.jkubinyi.elasticappender.search.query;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.common.Fuzzy;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLeafLevelGenerator;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Brings a new SQL-like mini language which is more restricted but more
 * benevolent than {@link QueryString}. Can be used to match one or more fields
 * against one query (value) with the help of fuzziness. <br>
 * The query can be used to process unverified or user queries. It does not
 * throw error when presented with invalid query.</b> Instead it will try to
 * silently skip the wrong part and move on with the query.
 * 
 * @author jurajkubinyi
 */
public class SimpleQueryString extends AbstractLeafLevelGenerator {

	private Fuzzy fuzzy;
	private boolean moreFields = false;
	private List<Field> fieldsList = new ArrayList<>();

	private SimpleQueryString(Field field, String value) {
		super(field, value);
	}

	private SimpleQueryString(Field field, String value, ComparisonOperator operator) {
		super(field, value, operator);
	}

	private SimpleQueryString(List<Field> fields, String value, ComparisonOperator operator) {
		super(null, value, operator);
		this.moreFields = true;
	}

	private SimpleQueryString(List<Field> fields, String value) {
		super(null, value);
		this.moreFields = true;
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param field    Field to be used in leaf query.
	 * @param value    Value which will be document's values evaluated against.
	 * @param operator Condition against which will be the field's value evaluated.
	 * @return Leaf query instance.
	 */
	public static SimpleQueryString of(Field field, String value, ComparisonOperator operator) {
		return new SimpleQueryString(field, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static SimpleQueryString of(Field field, String value) {
		return new SimpleQueryString(field, value);
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param field    Field to be used in leaf query.
	 * @param value    Value which will be document's values evaluated against.
	 * @param operator Condition against which will be the field's value evaluated.
	 * @return Leaf query instance.
	 */
	public static SimpleQueryString of(List<Field> fields, String value, ComparisonOperator operator) {
		return new SimpleQueryString(fields, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static SimpleQueryString of(List<Field> fields, String value) {
		return new SimpleQueryString(fields, value);
	}

	/**
	 * @param fuzzy Sets the fuzziness used by the leaf query.
	 * @return Leaf query instance.
	 */
	public SimpleQueryString fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildLeafQuery() {
		Object query = this.getValue()
				.orElseThrow(() -> new NullPointerException("Value cannot be null. (Used as a query)"));
		SimpleQueryStringBuilder sqsb = QueryBuilders.simpleQueryStringQuery((String) query);
		if (this.moreFields) {
			this.fieldsList.forEach(field -> {
				sqsb.field(field.toString());
			});
		} else {
			Field field = this.getField();
			if (field != null)
				sqsb.field(field.toString());
		}

		if (this.fuzzy != null)
			sqsb.fuzzyMaxExpansions(this.fuzzy.getMaxExpansions()).fuzzyPrefixLength(this.fuzzy.getPrefixLength())
				.fuzzyTranspositions(this.fuzzy.hasTranspositions());
		return sqsb;
	}
}