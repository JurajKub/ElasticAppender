package com.jkubinyi.elasticappender.search.query;

import java.util.Optional;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLeafLevelGenerator;

public class Range extends AbstractLeafLevelGenerator {

	private final static RangeValue EMPTY_RANGEVALUE = new RangeValue(null, null);
	
	public static class RangeValue {

		private final Object from;
		private final Object to;

		private RangeValue(Object from, Object to) {
			this.from = from;
			this.to = to;
		}
		
		public static RangeValue of(Object from, Object to) {
			return new RangeValue(from, to);
		}

		public Object getFrom() {
			return from;
		}

		public Object getTo() {
			return to;
		}
	}
	
	private Range(Field field, RangeValue rangeValue, ComparisonOperator operator) {
		super(field, rangeValue, operator);
	}

	public static Range of(Field field, RangeValue rangeValue, ComparisonOperator operator) {
		return new Range(field, rangeValue, operator);
	}
	
	public static Range of(Field field, RangeValue rangeValue) {
		return new Range(field, rangeValue, ComparisonOperator.equals);
	}

	public RangeValue getRangeValue() {
		Optional<Object> optionalValue = this.getValue();
		if(optionalValue.isPresent())
			return (RangeValue) optionalValue.get();
		else
			return Range.EMPTY_RANGEVALUE;
	}

	@Override
	protected QueryBuilder buildLeafQuery() {
		RangeValue rangeValue = this.getRangeValue();
		return QueryBuilders.rangeQuery(this.getField().toString()).from(rangeValue.getFrom())
				.to(rangeValue.getTo());
	}
}