package com.jkubinyi.elasticappender.search.field;

import java.util.LinkedHashSet;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import com.jkubinyi.elasticappender.search.Field;
import com.jkubinyi.elasticappender.search.field.MatchField.Fuzzy;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Brings a new SQL-like mini language with support of regexes. Can be
 * used to match one or more fields against one query (value) with the help of
 * fuzziness. <br>
 * The query should not be used to process unverified or user queries due its
 * nature throwing errors in case of any syntax issue.</b> If you need some of
 * the features try {@link SimpleQueryStringFields} for safer alternative.
 * 
 * @author jurajkubinyi
 */
public class QueryStringFields extends LogField {

	private boolean moreFields = false;

	private Set<Field> fieldsSet = new LinkedHashSet<>();

	private Fuzzy fuzzy = Fuzzies.DISABLED;

	private QueryStringFields(Field field, String value, ConditionType conditionType) {
		super(field, value, ValueType.value, conditionType);
	}

	private QueryStringFields(Set<Field> fields, String value, ConditionType conditionType) {
		super(null, value, ValueType.specific, conditionType);
		this.fieldsSet.addAll(fields);
		this.moreFields = true;
	}

	private QueryStringFields(String value, ConditionType conditionType) {
		super(null, null, ValueType.specific, conditionType);
		this.moreFields = false;
	}

	/**
	 * Creates single field query for the condition.
	 * 
	 * @param field         Field which will be evaluated if not stated otherwise
	 *                      inside the query.
	 * @param value         The query which will be used during evaluation.
	 * @param conditionType The condition by which it will be grouped together with
	 *                      other leaf queries on the same level.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(Field field, String value, ConditionType conditionType) {
		return new QueryStringFields(field, value, conditionType);
	}

	/**
	 * Creates single field query without condition. Can be only used for the root
	 * query.
	 * 
	 * @param field Field which will be evaluated if not stated otherwise inside the
	 *              query.
	 * @param value The query which will be used during evaluation.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(Field field, String value) {
		return new QueryStringFields(field, value, ConditionType.none);
	}

	/**
	 * Creates multi field query for the condition.
	 * 
	 * @param fields        Fields which will be evaluated if not stated otherwise
	 *                      inside the query.
	 * @param value         The query which will be used during evaluation.
	 * @param conditionType The condition by which it will be grouped together with
	 *                      other leaf queries on the same level.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(Set<Field> fields, String value, ConditionType conditionType) {
		return new QueryStringFields(fields, value, conditionType);
	}

	/**
	 * Creates multi field query without condition. Can be only used for the root
	 * query.
	 * 
	 * @param fields Fields which will be evaluated if not stated otherwise inside
	 *               the query.
	 * @param value  The query which will be used during evaluation.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(Set<Field> fields, String value) {
		return new QueryStringFields(fields, value, ConditionType.none);
	}

	/**
	 * Creates query without defined field. All eligible fields will be evaluated if
	 * not stated otherwise inside the query.
	 * 
	 * @param value         The query which will be used during evaluation.
	 * @param conditionType The condition by which it will be grouped together with
	 *                      other leaf queries on the same level.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(String value, ConditionType conditionType) {
		return new QueryStringFields(value, conditionType);
	}

	/**
	 * Creates query without dined field and condition. All eligible fields will be
	 * evaluated if not stated otherwise inside the query. Can be only used for the
	 * root query.
	 * 
	 * @param value The query which will be used during evaluation.
	 * @return Leaf query instance.
	 */
	public static QueryStringFields ofValue(String value) {
		return new QueryStringFields(value, ConditionType.none);
	}

	/**
	 * 
	 * @param fuzzy
	 * @return Leaf query instance.
	 */
	public QueryStringFields fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildQuery() {
		String query = this.getValue()
				.orElseThrow(() -> new NullPointerException("Value cannot be null. (Used as a query)"));
		QueryStringQueryBuilder qsb = QueryBuilders.queryStringQuery(query);
		if (this.moreFields) {
			this.fieldsSet.forEach(field -> {
				qsb.field(field.toString());
			});
		} else {
			Field field = this.getField();
			if (field != null) {
				qsb.defaultField(field.toString());
			}
		}

		if (this.fuzzy != Fuzzies.DISABLED) {
			qsb.fuzziness(this.fuzzy.getFuzziness()).fuzzyMaxExpansions(this.fuzzy.getMaxExpansions())
					.fuzzyPrefixLength(this.fuzzy.getPrefixLength()).fuzzyTranspositions(this.fuzzy.isTranspositions());
		}

		return qsb;
	}
}
