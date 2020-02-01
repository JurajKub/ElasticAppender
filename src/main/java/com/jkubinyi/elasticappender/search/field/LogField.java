package com.jkubinyi.elasticappender.search.field;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.EASearch.SearchQuery;
import com.jkubinyi.elasticappender.search.Field;

/**
 * Abstract class providing basic leaf query building a nesting capabilities for
 * extending classes and API for external calls. Please refer to the concrete
 * implementation for more information.
 * 
 * @author jurajkubinyi
 */
public abstract class LogField {
	private final Field field;
	private final String value;
	private final ValueType valueType;
	private final ConditionType conditionType;
	private Set<LogField> subFields = new LinkedHashSet<>();
	private Operator conditionOperator = Operator.AND;

	/**
	 * Interface which is implemented by all leaf query builders.
	 * 
	 * @author jurajkubinyi
	 *
	 */
	public interface FieldBuilder {
		LogField make();
	}

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

	/**
	 * Used internally to mark whether query uses a meaningful value or will be
	 * evaluated by implementation specific logic.
	 * 
	 * @author jurajkubinyi
	 */
	public enum ValueType {
		/** Field's value will be compared against the value. */
		value,

		/** Extending class providing an additional functionality should use this. */
		specific
	}

	protected abstract QueryBuilder buildQuery();

	/**
	 * Creates QueryBuilder instance and thus the query by traversing and calling on
	 * each element {@link #buildQTree()} from this level down. Method should not be
	 * called directly and rather it is strongly encouraged to use
	 * {@link SearchQuery} implementation to prepare the query depending on the
	 * goal.
	 * 
	 * @return Elasticsearch's {@link QueryBuilder} with all subfield traversed and
	 *         added to the output.
	 */
	public QueryBuilder buildQTree() {
		QueryBuilder currentFieldQuery = this.buildQuery();
		if (this.subFields.size() > 0) {
			BoolQueryBuilder rootQuery = QueryBuilders.boolQuery();
			this.addQueryToTheCondition(rootQuery, this, currentFieldQuery);
			for (LogField field : this.subFields) {
				this.addQTreeToTheCondition(rootQuery, field);
			}
			return rootQuery;
		} else
			return currentFieldQuery;
	}

	private QueryBuilder addQueryToTheCondition(BoolQueryBuilder rootQuery, LogField field, QueryBuilder fieldQuery) {
		if (field.getConditionType() == ConditionType.must)
			rootQuery.must(fieldQuery);
		else if (field.getConditionType() == ConditionType.mustNot)
			rootQuery.mustNot(fieldQuery);
		else if (field.getConditionType() == ConditionType.should)
			rootQuery.should(fieldQuery);
		else if (field.getConditionType() == ConditionType.filter)
			rootQuery.filter(fieldQuery);
		return rootQuery;
	}

	private QueryBuilder addQTreeToTheCondition(BoolQueryBuilder rootQuery, LogField field) {
		QueryBuilder fieldQuery = field.buildQTree();
		return this.addQueryToTheCondition(rootQuery, field, fieldQuery);
	}

	/**
	 * 
	 * @param parameter
	 * @param value
	 * @param valueType
	 * @param conditionType Can be ignored in some queries where it does nothing
	 *                      (example root match query)
	 */
	protected LogField(Field field, String value, ValueType valueType, ConditionType conditionType) {
		this.field = field;
		this.value = value;
		this.valueType = valueType;
		this.conditionType = conditionType;
	}

	/**
	 * Used to get preferred operator for the Elasticsearch leaff query generation
	 * properties.
	 * 
	 * @return Elasticsearch's {@link Operator}.
	 */
	public Operator getOperator() {
		return this.conditionOperator;
	}

	/**
	 * Used to get preferred operator for the Elasticsearch leaff query generation
	 * properties.
	 * 
	 * @return Elasticsearch's {@link Operator}.
	 */
	public LogField operator(Operator operator) {
		this.conditionOperator = operator;
		return this;
	}

	/**
	 * Used to create a one level lower (vertically) leaf query.
	 * 
	 * @param field Leaf query to be added.
	 * @return Current leaf query instance.
	 */
	public LogField add(LogField field) {
		this.subFields.add(field);
		return this;
	}

	/**
	 * @return Used to get field which value will be evaluated by the current leaf
	 *         query.
	 */
	public Field getField() {
		return this.field;
	}

	/**
	 * @return Used to get requested value which will be evaluated by the current
	 *         leaf query.
	 */
	public Optional<String> getValue() {
		return Optional.ofNullable(this.value);
	}

	/**
	 * @return Gets the type of the value. Must be checked before value usage.
	 */
	public ValueType getValueType() {
		return this.valueType;
	}

	/**
	 * @return Gets the type of the condition by which are other leaf queries at the
	 *         same level grouped together.
	 */
	public ConditionType getConditionType() {
		return this.conditionType;
	}
}
