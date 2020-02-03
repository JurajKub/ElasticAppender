package com.jkubinyi.elasticappender.search.query;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.common.Fuzzy;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLeafLevelGenerator;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Brings a new SQL-like mini language with support of regexes. Can be
 * used to match one or more fields against one query (value) with the help of
 * fuzziness. <br>
 * The query should not be used to process unverified or user queries due its
 * nature throwing errors in case of any syntax issue.</b> If you need some of
 * the features try {@link SimpleQueryString} for safer alternative.
 * 
 * @author jurajkubinyi
 */
public class QueryString extends AbstractLeafLevelGenerator {

	private Fuzzy fuzzy;
	private boolean moreFields = false;
	private List<Field> fieldsList = new ArrayList<>();

	private QueryString(Field field, String value) {
		super(field, value);
	}

	private QueryString(Field field, String value, ComparisonOperator operator) {
		super(field, value, operator);
	}

	private QueryString(List<Field> fields, String value, ComparisonOperator operator) {
		super(null, value, operator);
		this.moreFields = true;
	}

	private QueryString(List<Field> fields, String value) {
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
	public static QueryString of(Field field, String value, ComparisonOperator operator) {
		return new QueryString(field, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static QueryString of(Field field, String value) {
		return new QueryString(field, value);
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param field    Field to be used in leaf query.
	 * @param value    Value which will be document's values evaluated against.
	 * @param operator Condition against which will be the field's value evaluated.
	 * @return Leaf query instance.
	 */
	public static QueryString of(List<Field> fields, String value, ComparisonOperator operator) {
		return new QueryString(fields, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static QueryString of(List<Field> fields, String value) {
		return new QueryString(fields, value);
	}

	/**
	 * @param fuzzy Sets the fuzziness used by the leaf query.
	 * @return Leaf query instance.
	 */
	public QueryString fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildLeafQuery() {
		Object query = this.getValue()
				.orElseThrow(() -> new NullPointerException("Value cannot be null. (Used as a query)"));
		QueryStringQueryBuilder qsb = QueryBuilders.queryStringQuery((String) query);
		if (this.moreFields) {
			this.fieldsList.forEach(field -> {
				qsb.field(field.toString());
			});
		} else {
			Field field = this.getField();
			if (field != null)
				qsb.defaultField(field.toString());
		}

		if (this.fuzzy != null)
			qsb.fuzziness(this.fuzzy.getFuzziness()).fuzzyMaxExpansions(this.fuzzy.getMaxExpansions())
					.fuzzyPrefixLength(this.fuzzy.getPrefixLength())
					.fuzzyTranspositions(this.fuzzy.hasTranspositions());
		return qsb;
	}
}