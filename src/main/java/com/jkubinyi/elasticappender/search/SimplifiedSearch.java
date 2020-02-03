package com.jkubinyi.elasticappender.search;

import com.jkubinyi.elasticappender.record.LogRecord;
import com.jkubinyi.elasticappender.search.EASearch.BooleanQueryBuilder;
import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.query.SimpleQueryString;
import com.jkubinyi.elasticappender.search.query.QueryString;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Class providing simplified Fulltext / Exact search capabilities
 * for evaluating the ElasticAppender saved logs from Elasticsearch
 * cluster. It is just a simplifying proxy class for {@link EASearch}
 * and provides a limited subset of features targeted at easy and fast
 * log searching.
 * 
 * @author jurajkubinyi
 */
public class SimplifiedSearch {
	private int maxResults = 10;
	private int from = 0;
	private boolean paginated = false;
	private final EASearch search;
	private BooleanQueryBuilder queryBuilder;
	
	public SimplifiedSearch(EASearch search) {
		this.search = search;
	}
	
	/**
	 * Creates a fulltext search using field looking for the value. Under the hood
	 * analyzes the value and returns results similar, as measured by a Levenshtein
	 * edit distance.
	 * 
	 * @param field Field of the log which will be evaluated.
	 * @param value Value which will be analyzed.
	 * @return {@link SimplifiedSearch} instance which is building the query.
	 */
	public SimplifiedSearch fulltext(Field field, Object value) {
		if(this.queryBuilder == null) {
			BooleanQueryBuilder queryBuilder = this.search.booleanQuery();
			this.queryBuilder = queryBuilder;
		}
		
		this.queryBuilder.match(field, value);
		return this;
	}

	/**
	 * Creates a fulltext search using query. Under the hood analyzes the value
	 * and returns results similar, as measured by a Levenshtein edit distance.
	 * 
	 * @param field Field of the log which will be evaluated.
	 * @param query Elasticsearch's query language depending on the safe parameter.
	 * @param safe If {@code true} will use {@link SimpleQueryString} else will use
	 * {@link QueryString}
	 * @return {@link SimplifiedSearch} instance which is building the query.
	 */
	public SimplifiedSearch fulltextQuery(Field field, String query, boolean safe) {
		if(this.queryBuilder == null) {
			BooleanQueryBuilder queryBuilder = this.search.booleanQuery();
			this.queryBuilder = queryBuilder;
		}
		
		if(safe)
			this.queryBuilder.simpleQueryString(field, query);
		else
			this.queryBuilder.queryString(field, query);
		return this;
	}
	
	/**
	 * 
	 * @param field Field of the log which will be evaluated.
	 * @param value Value which could be analyzed depending on the exact value.
	 * @param exact If {@code true} will not use any analyzes and tries to find
	 * only direct, exact matches.
	 * @return {@link SimplifiedSearch} instance which is building the query.
	 */
	public SimplifiedSearch matches(Field field, Object value, boolean exact) {
		if(this.queryBuilder == null) {
			BooleanQueryBuilder queryBuilder = this.search.booleanQuery();
			this.queryBuilder = queryBuilder;
		}
		
		if(exact)
			this.queryBuilder.term(field, value);
		else
			this.queryBuilder.match(field, value);
		return this;
	}
	
	/**
	 * Sets pagination.
	 * 
	 * @param from Show results from n-th result.
	 * @param maxResults Show maximum of n results.
	 * @return {@link SimplifiedSearch} instance which is building the query.
	 */
	public SimplifiedSearch paginated(int from, int maxResults) {
		this.paginated = true;
		this.from = from;
		this.maxResults = maxResults;
		return this;
	}
	
	/**
	 * Executes the built query and return a {@link Set} of results.
	 * 
	 * @return {@link Set} of results.
	 * @throws IOException In case of connection issues.
	 */
	public Set<LogRecord> execute() throws IOException {
		Objects.requireNonNull(this.queryBuilder, "Cannot execute on empty query.");
		
		if(this.paginated)
			return this.queryBuilder.executePaginated(this.from, this.maxResults);
		else
			return this.queryBuilder.execute();
	}
	
	/**
	 * Resets current building query and creates a new one ready to be built.
	 * 
	 * @return {@link SimplifiedSearch} instance which is building the query.
	 */
	public SimplifiedSearch reset() {
		this.queryBuilder = this.search.booleanQuery();
		return this;
	}
	
	/**
	 * @return Generated JSON query by traversing all childs in the tree till the end
	 * and generating logical conditions. The result is not cached and each call
	 * regenerates the query.
	 */
	public String queryString() {
		if(this.queryBuilder == null) return "";
		return this.queryBuilder.toString();
	}
}
