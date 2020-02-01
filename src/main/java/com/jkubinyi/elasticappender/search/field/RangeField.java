package com.jkubinyi.elasticappender.search.field;

import java.util.Objects;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.Field;

/**
 * A term leaf query used to compare ranges of the document's fields. Can be
 * used to compare one field against the range.<br>
 * Alternative queries can be used in conjunction with this to construct a
 * query.
 * 
 * @author jurajkubinyi
 */
public class RangeField extends LogField {

	private static class RangeValue {

		private final Object from;
		private final Object to;

		private RangeValue(Object from, Object to) {
			this.from = from;
			this.to = to;
		}

		public Object getFrom() {
			return from;
		}

		public Object getTo() {
			return to;
		}
	}

	public static class RangeFieldBuilder implements FieldBuilder {

		private Object from;
		private Object to;
		private final Field field;
		private final ConditionType conditionType;

		private RangeFieldBuilder(Field field, ConditionType conditionType) {
			this.field = field;
			this.conditionType = conditionType;
		}

		public RangeFieldBuilder from(Object from) {
			this.from = from;
			return this;
		}

		public RangeFieldBuilder to(Object to) {
			this.to = to;
			return this;
		}

		public RangeField make() {
			Objects.requireNonNull(this.from, "From cannot be null.");
			Objects.requireNonNull(this.to, "To cannot be null.");
			return RangeField.ofRange(this.field, new RangeValue(this.from, this.to), this.conditionType);
		}
	}

	private final RangeValue rangeValue;

	private RangeField(Field field, RangeValue rangeValue, ConditionType conditionType) {
		super(field, null, ValueType.specific, conditionType);
		this.rangeValue = rangeValue;
	}

	public static RangeField ofRange(Field field, RangeValue rangeValue, ConditionType conditionType) {
		return new RangeField(field, rangeValue, conditionType);
	}

	public static RangeFieldBuilder of(Field field, ConditionType conditionType) {
		return new RangeFieldBuilder(field, conditionType);
	}

	public static RangeFieldBuilder of(Field field) {
		return new RangeFieldBuilder(field, ConditionType.none);
	}

	public RangeValue getRangeValue() {
		return this.rangeValue;
	}

	@Override
	protected QueryBuilder buildQuery() {
		return QueryBuilders.rangeQuery(this.getField().toString()).from(this.rangeValue.getFrom())
				.to(this.rangeValue.getTo());
	}
}
