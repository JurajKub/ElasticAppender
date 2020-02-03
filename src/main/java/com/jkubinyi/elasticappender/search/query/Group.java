package com.jkubinyi.elasticappender.search.query;

import java.util.Arrays;
import java.util.List;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.query.Range.RangeValue;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLevelGenerator;

public class Group extends AbstractLevelGenerator {

	protected Group(List<AbstractLevelGenerator> nodes, LogicalOperator operator) {
		super(nodes, operator);
	}
	
	protected Group(List<AbstractLevelGenerator> nodes) {
		super(nodes);
	}
	
	protected Group() {
		super();
	}
	
	public static Group newGroup() {
		return new Group();
	}
	
	public static Group newGroup(AbstractLevelGenerator... nodes) {
		Group group = new Group(Arrays.asList(nodes));
		return group;
	}

	public static Group newGroup(LogicalOperator operator, AbstractLevelGenerator... nodes) {
		Group group = new Group(Arrays.asList(nodes), operator);
		return group;
	}
	
	public static Group newGroup(List<AbstractLevelGenerator> nodes) {
		Group group = new Group(nodes);
		return group;
	}

	public static Group newGroup(List<AbstractLevelGenerator> nodes, LogicalOperator operator) {
		Group group = new Group(nodes, operator);
		return group;
	}
	
	public Group group(AbstractLevelGenerator... nodes) {
		Group group = new Group(Arrays.asList(nodes));
		this.add(group);
		return this;
	}

	public Group group(LogicalOperator operator, AbstractLevelGenerator... nodes) {
		Group group = new Group(Arrays.asList(nodes), operator);
		this.add(group);
		return this;
	}
	
	public Group group(List<AbstractLevelGenerator> nodes) {
		Group group = new Group(nodes);
		this.add(group);
		return this;
	}

	public Group group(List<AbstractLevelGenerator> nodes, LogicalOperator operator) {
		Group group = new Group(nodes, operator);
		this.add(group);
		return this;
	}
	
	public Group queryString(Field field, String query, ComparisonOperator operator) {
		SimpleQueryString q = SimpleQueryString.of(field, query, comparisonOperator);
		this.add(q);
		return this;
	}

	public Group queryString(Field field, String query) {
		SimpleQueryString q = SimpleQueryString.of(field, query);
		this.add(q);
		return this;
	}

	public Group simpleQueryString(List<Field> fields, String query, ComparisonOperator operator) {
		SimpleQueryString q = SimpleQueryString.of(fields, query, comparisonOperator);
		this.add(q);
		return this;
	}

	public Group simpleQueryString(List<Field> fields, String query) {
		SimpleQueryString q = SimpleQueryString.of(fields, query);
		this.add(q);
		return this;
	}
	
	public Group simpleQueryString(Field field, String query, ComparisonOperator operator) {
		SimpleQueryString q = SimpleQueryString.of(field, query, comparisonOperator);
		this.add(q);
		return this;
	}

	public Group simpleQueryString(Field field, String query) {
		SimpleQueryString q = SimpleQueryString.of(field, query);
		this.add(q);
		return this;
	}

	public Group queryString(List<Field> fields, String query, ComparisonOperator operator) {
		QueryString q = QueryString.of(fields, query, comparisonOperator);
		this.add(q);
		return this;
	}

	public Group queryString(List<Field> fields, String query) {
		QueryString q = QueryString.of(fields, query);
		this.add(q);
		return this;
	}
	
	public Group range(Field field, RangeValue value, ComparisonOperator operator) {
		Range range = Range.of(field, value, comparisonOperator);
		this.add(range);
		return this;
	}
	
	public Group range(Field field, RangeValue value) {
		Range range = Range.of(field, value, this.comparisonOperator); // Inherit it from the group
		this.add(range);
		return this;
	}

	public Group term(Field field, Object value, ComparisonOperator operator) {
		Term term = Term.of(field, value, operator);
		this.add(term);
		return this;
	}

	public Group term(Field field, Object value) {
		Term term = Term.of(field, value, this.comparisonOperator);
		this.add(term);
		return this;
	}
	
	public Group match(Field field, Object value, ComparisonOperator operator) {
		Match match = Match.of(field, value, operator);
		this.add(match);
		return this;
	}
	
	public Group match(Field field, Object value) {
		Match match = Match.of(field, value, this.comparisonOperator);
		this.add(match);
		return this;
	}
	
	public Group add(AbstractLevelGenerator node, LogicalOperator operator) {
		this.allNodes.put(operator, node);
		return this;
	}
	
	public Group add(AbstractLevelGenerator node) {
		this.allNodes.put(LogicalOperator.and, node);
		return this;
	}
}
