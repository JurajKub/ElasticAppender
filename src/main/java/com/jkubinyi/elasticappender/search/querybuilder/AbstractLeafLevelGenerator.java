package com.jkubinyi.elasticappender.search.querybuilder;

import java.util.Optional;

import org.elasticsearch.index.query.QueryBuilder;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.query.Group;

/**
 * <p>
 * Abstract generator of the leaf query fragment. The resulting branch
 * of the query will end with this fragment. If you want to expand the
 * query you should look at horizontal or vertical usage of {@link Group}.
 * </p>
 * The class is used as a base for {@link Group} and abstract leaf query
 * fragments as well. (See {@link AbstractLeafLevelGenerator}) <u>Should not be
 * extended directly if not absolutely necessary. Please see more concrete
 * abstract classes or implementations for more information.</u>
 * 
 * @author jurajkubinyi
 *
 */
public abstract class AbstractLeafLevelGenerator extends AbstractLevelGenerator {
	private final Field field;
	private final Object value;
	private float boost = 1;
	
	protected AbstractLeafLevelGenerator(Field field, Object value, ComparisonOperator operator) {
		super(operator);
		this.field = field;
		this.value = value;
	}

	protected AbstractLeafLevelGenerator(Field field, Object value) {
		super();
		this.field = field;
		this.value = value;
	}

	/**
	 * @return Floating point number used to decrease or increase the relevance scores of a query. Defaults to 1.0.
	 */
	public float getBoost() {
		return boost;
	}

	/**
	 * Sets the number used to decrease or increase the relevance scores of a query. Defaults to 1.0.
	 * @param boost Boost values are relative to the default value of 1.0. A boost value between 0 and 1.0
	 * decreases the relevance score. A vvalue greater than 1.0 increases the relevance score.
	 */
	public void setBoost(float boost) {
		this.boost = boost;
	}

	/**
	 * @return Gets the field which will be used during the fragment evaluation.
	 */
	public Field getField() {
		return this.field;
	}

	/**
	 * @return Gets the value which will be used during the fragment evaluation.
	 */
	public Optional<Object> getValue() {
		return Optional.ofNullable(this.value);
	}

	/**
	 * @return Gets the unsafe value which will be used during the fragment evaluation.
	 * Can be null so should be treated as such.
	 */
	public Object getUnsafeValue() {
		return this.value;
	}
	
	/**
	 * Provides a way to add common leaf settings to each built query by its
	 * implementing class.
	 * 
	 * @return Reevaluated fragment query.
	 */
	@Override
	protected QueryBuilder buildQuery() {
		QueryBuilder leafQuery = this.buildLeafQuery();
		leafQuery.boost(this.getBoost());
		return leafQuery;
	}
	
	/**
	 * Should be implemented by all leaf query fragments to provide customized query
	 * generation. The resulting {@link QueryBuilder} instance will be evaluated and
	 * may be modified by this class before returning it up the tree.
	 * 
	 * @return Customized query built for the concrete leaf fragment.
	 */
	protected abstract QueryBuilder buildLeafQuery();
}
