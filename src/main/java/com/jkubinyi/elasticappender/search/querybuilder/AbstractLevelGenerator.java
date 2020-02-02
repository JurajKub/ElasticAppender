package com.jkubinyi.elasticappender.search.querybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.query.Group;

/**
 * <p>
 * Abstract generator of the query fragment with additional support for defining
 * child nodes and generating accurate query depending on the settings.
 * </p>
 * The class is used as a base for {@link Group} and abstract leaf query
 * fragments as well. (See {@link AbstractLeafLevelGenerator}) <u>Should not be
 * extended directly if not absolutely necessary. Please see more concrete
 * abstract classes or implementations for more information.</u>
 * 
 * @author jurajkubinyi
 *
 */
public abstract class AbstractLevelGenerator {

	protected ArrayListValuedHashMap<LogicalOperator, AbstractLevelGenerator> allNodes = new ArrayListValuedHashMap<>();
	protected final ComparisonOperator comparisonOperator;

	interface AbstractLevelBuilder {
		public AbstractLevelGenerator make();
	}

	public enum ComparisonOperator {
		equals, notEquals
	}

	public enum LogicalOperator {
		and, or
	}

	/**
	 * Used to create an intermediate classes which does <b>not have value</b> like
	 * groups with starting child nodes.
	 * 
	 * @param nodes List of nodes down the tree which acts like a child.
	 */
	protected AbstractLevelGenerator(List<AbstractLevelGenerator> nodes) {
		for (AbstractLevelGenerator node : nodes) {
			this.allNodes.put(LogicalOperator.and, node);
		}
		this.comparisonOperator = ComparisonOperator.equals;
	}

	/**
	 * Used to create an intermediate classes which does <b>not have value</b> like
	 * groups with starting child node.
	 * 
	 * @param nodes One node down the tree which acts like a child.
	 */
	protected AbstractLevelGenerator(AbstractLevelGenerator node) {
		this.allNodes.put(LogicalOperator.and, node);
		this.comparisonOperator = ComparisonOperator.equals;
	}

	/**
	 * Used to create an intermediate classes which does <b>not have value</b> like
	 * groups.
	 * 
	 * @param nodes           One node down the tree which acts like a child.
	 * @param logicalOperator LogicalOperator by which this intermediate classes
	 *                        childs will be grouped together. (AND/OR)
	 */
	protected AbstractLevelGenerator(List<AbstractLevelGenerator> nodes, LogicalOperator logicalOperator) {
		this();
		this.allNodes.putAll(logicalOperator, nodes);
	}

	/**
	 * Used by the extending intermediate classes which does <b>not have value</b>
	 * like groups or by leaf queries which does not have user defined comparison.
	 * Please see {@link Group} for usage example.
	 * 
	 * @return AbstractLevelGenerator instance which acts as man-in-the-middle
	 *         without having a value itself.
	 */
	protected AbstractLevelGenerator() {
		this.comparisonOperator = ComparisonOperator.equals;
	}

	/**
	 * Used by the extending leaf classes to add class-specific features. Please see
	 * {@link AbstractLeafLevelGenerator} for usage example.
	 * 
	 * @param operator If value should be equal or not equal.
	 * @return AbstractLevelGenerator instance which acts as last leaf with having a
	 *         value comparison or range logic inside.
	 */
	protected AbstractLevelGenerator(ComparisonOperator operator) {
		this.comparisonOperator = operator;
	}

	protected QueryBuilder buildQuery() {
		if (this.allNodes.size() > 0) {
			BoolQueryBuilder root = QueryBuilders.boolQuery();
			// BoolQueryBuilder activeRoot = root;
			Map<LogicalOperator, Collection<AbstractLevelGenerator>> nodesMap = this.allNodes.asMap();
			// LogicalOperator firstOperator = nodesMap.keySet().stream().findFirst().get();

			for (Entry<LogicalOperator, Collection<AbstractLevelGenerator>> entry : nodesMap.entrySet()) {
				LogicalOperator logicOperator = entry.getKey();
				Collection<AbstractLevelGenerator> nodes = entry.getValue();
				BoolQueryBuilder currentRoot = QueryBuilders.boolQuery();

				if (logicOperator == LogicalOperator.and) {

					if (nodes.size() > 1) {
						ArrayList<AbstractLevelGenerator> equalsNodes = new ArrayList<>();
						ArrayList<AbstractLevelGenerator> notEqualsNodes = new ArrayList<>();
						nodes.forEach(concreteNode -> {
							if (concreteNode.comparisonOperator == ComparisonOperator.equals)
								equalsNodes.add(concreteNode);
							else
								notEqualsNodes.add(concreteNode);
						});
						boolean allSameComparison = (equalsNodes.size() == 0 || notEqualsNodes.size() == 0);

						if (allSameComparison) {
							if (equalsNodes.size() != 0) {
								equalsNodes.forEach(concreteNode -> {
									root.must(concreteNode.buildQuery());
								});
							} else {
								notEqualsNodes.forEach(concreteNode -> {
									currentRoot.mustNot(concreteNode.buildQuery());
								});
								root.must(currentRoot);
							}
						}
					} else {
						AbstractLevelGenerator concreteNode = nodes.stream().findFirst().get();
						if (concreteNode.comparisonOperator == ComparisonOperator.equals)
							root.must(concreteNode.buildQuery());
						else
							root.mustNot(concreteNode.buildQuery());
					}
				} else {
					nodes.forEach(concreteNode -> currentRoot.should(concreteNode.buildQuery()));
					root.must(currentRoot);
				}
			}
			return root;
		} else {
			return QueryBuilders.matchAllQuery(); // Should not happen but as a fallback
		}
	}

	/**
	 * <p>
	 * Returns a generated prettified JSON of the query from this node till the last
	 * leaf. The exact format is unspecified and subject to change. Each subsequent
	 * call will generate the result again and can thus result in different output.
	 * It is up to the developer to cache the result if necessary.
	 * </p>
	 * Calling the method subsequently without caching can cause unnecessary load
	 * due to the calculated nature of the result.
	 * 
	 * @return Calculated result of the current query tree up from this node till
	 *         the last referenced leaf in the entire structure.
	 */
	@Override
	public String toString() {
		return this.buildQuery().toString();
	}

	/**
	 * Returns all immediate child nodes. If there is requirement for full tree
	 * traversal, please consider running the method on all returned elements as
	 * well.
	 * 
	 * @return Collection containing all immediate child nodes.
	 */
	public Collection<AbstractLevelGenerator> getAllNodes() {
		List<AbstractLevelGenerator> nodes = new ArrayList<>();
		for (Entry<LogicalOperator, Collection<AbstractLevelGenerator>> entry : this.allNodes.asMap().entrySet()) {
			nodes.addAll(entry.getValue());
		}
		return nodes;
	}
}
