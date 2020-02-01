package com.jkubinyi.elasticappender.search.field;

import java.util.LinkedHashSet;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;

import com.jkubinyi.elasticappender.search.Field;
import com.jkubinyi.elasticappender.search.field.MatchField.Fuzzy;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Brings a new SQL-like mini language which is more restricted but more
 * benevolent than {@link SimpleQueryStringFields}. Can be used to match one or more
 * fields against one query (value) with the help of fuzziness. <br>
 * The query can be used to process unverified or user queries. It does not
 * throw error when presented with invalid query.</b> Instead it will try to
 * silently skip the wrong part and move on with the query.
 * 
 * @author jurajkubinyi
 */
public class SimpleQueryStringFields extends LogField {

	private boolean moreFields = false;

	private Set<Field> fieldsSet = new LinkedHashSet<>();

	private Fuzzy fuzzy = Fuzzies.DISABLED;

	private SimpleQueryStringFields(Field field, String value, ConditionType conditionType) {
		super(field, value, ValueType.value, conditionType);
	}

	private SimpleQueryStringFields(Set<Field> fields, String value, ConditionType conditionType) {
		super(null, value, ValueType.specific, conditionType);
		this.fieldsSet.addAll(fields);
		this.moreFields = true;
	}

	private SimpleQueryStringFields(String value, ConditionType conditionType) {
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
	public static SimpleQueryStringFields ofValue(Field field, String value, ConditionType conditionType) {
		return new SimpleQueryStringFields(field, value, conditionType);
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
	public static SimpleQueryStringFields ofValue(Field field, String value) {
		return new SimpleQueryStringFields(field, value, ConditionType.none);
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
	public static SimpleQueryStringFields ofValue(Set<Field> fields, String value, ConditionType conditionType) {
		return new SimpleQueryStringFields(fields, value, conditionType);
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
	public static SimpleQueryStringFields ofValue(Set<Field> fields, String value) {
		return new SimpleQueryStringFields(fields, value, ConditionType.none);
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
	public static SimpleQueryStringFields ofValue(String value, ConditionType conditionType) {
		return new SimpleQueryStringFields(value, conditionType);
	}

	/**
	 * Creates query without dined field and condition. All eligible fields will be
	 * evaluated if not stated otherwise inside the query. Can be only used for the
	 * root query.
	 * 
	 * @param value The query which will be used during evaluation.
	 * @return Leaf query instance.
	 */
	public static SimpleQueryStringFields ofValue(String value) {
		return new SimpleQueryStringFields(value, ConditionType.none);
	}

	/**
	 * @param fuzzy Sets the fuzziness used by the leaf query.
	 * @return Leaf query instance.
	 */
	public SimpleQueryStringFields fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildQuery() {
		String query = this.getValue()
				.orElseThrow(() -> new NullPointerException("Value cannot be null. (Used as a query)"));
		SimpleQueryStringBuilder sqsb = QueryBuilders.simpleQueryStringQuery(query);
		if (this.moreFields) {
			this.fieldsSet.forEach(field -> {
				sqsb.field(field.toString());
			});
		} else {
			Field field = this.getField();
			if (field != null) {
				sqsb.field(field.toString());
			}
		}

		if (this.fuzzy != Fuzzies.DISABLED) {
			sqsb.fuzzyMaxExpansions(this.fuzzy.getMaxExpansions()).fuzzyPrefixLength(this.fuzzy.getPrefixLength())
					.fuzzyTranspositions(this.fuzzy.isTranspositions());
		}

		return sqsb;
	}
}
