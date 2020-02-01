package com.jkubinyi.elasticappender.search;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.jkubinyi.elasticappender.L4JElasticAppender;
import com.jkubinyi.elasticappender.search.field.LogField;
import com.jkubinyi.elasticappender.search.field.LogField.ConditionType;
import com.jkubinyi.elasticappender.search.field.LogField.FieldBuilder;
import com.jkubinyi.elasticappender.search.field.SimpleQueryStringFields;

/**
 * <p>EASearch is a short name for ElasticAppender Search. Provides a set of utilities which
 * enables easier fulltext search on most fields from {@link L4JElasticAppender}.</p>
 * <p>Includes the same robin round connection feature as in {@link L4JElasticAppender}. All
 * exposed fields from the Log4J to the Elasticsearch are mapped in the {@link Field} enum.<br>
 * Class requires at least one node connection and same index name with date format used in
 * ElasticAppender configuration.</p>
 * <p>Class includes a {@link FulltextSearchQuery} which together with {@link LogField}
 * handles query generation. Elasticsearch uses a concept of leaf queries and compound queries.<br>
 * Leaf queries are looking at particular field's value and matches against it. They can be nested.<br>
 * Compound queries are aggregation clauses used to merge scores of leaf queries nested inside.<br>
 * 
 * Leaf Query types supported: <ul>
 * <li>Match query - a fulltext query which analyzes text before matching with support of fuzzy search.</li>
 * <li>Range query - a query which matches documents with fields values within defined range.</li>
 * <li>Query String query - a fulltext SQL like query mini language which matches fields. It is not safe to
 * run against unverified queries or user input because it can throw error. Please see {@link SimpleQueryStringFields}
 * for safe version.</li>
 * <li>Simple Query String query - a more restricted safe version of Query String with limited language but
 * it does not throw errors. Instead, it ignores any invalid part.</li>
 * </ul>
 * <br>
 * Compound Query types supported: <ul>
 * <li>Bool query - it sums all of the wrapped inner query scores together and returns result with it as a whole.</li>
 * <li>Disjunction Max query - it returns the highest score from any wrapped inner matching query.</li>
 * </ul>
 * <br>
 * <br>
 * Class uses a Bool wrapper query by default, but it can be changed in the {@link FulltextSearchQuery}.
 * </p>
 * 
 * @author jurajkubinyi
 *
 */
public class EASearch {
	
	private final HttpHost[] hosts;
	private final CredentialsProvider credentialsProvider;
	private final RestHighLevelClient restClient;
	private final LogIndex index;
	
	/**
	 * Class storing information about index.
	 * @author jurajkubinyi
	 *
	 */
	public static class LogIndex {
		private final String prefix;
		private final String name;
		private final DateFormat format;
		private String computedFormat;
		
		/**
		 * Creates a standard index reference in conjunction with {@link L4JElasticAppender}. This should
		 * be used by most projects.
		 * 
		 * @param name Name of the index from Log4J configuration.
		 * @param format Format of the index from Log4J configuration.
		 * @return Instance holding information about the index including calculated name.
		 */
		public static LogIndex ofStandard(String name, String format) {
			return new LogIndex(L4JElasticAppender.getPrefix(), name, format);
		}

		/**
		 * Creates a custom index reference using provided prefix.
		 * 
		 * @param prefix Prefix of the index.
		 * @param name Name of the index from Log4J configuration.
		 * @param format Format of the index from Log4J configuration.
		 * @return Instance holding information about the index including calculated name.
		 */
		public static LogIndex of(String prefix, String name, String format) {
			return new LogIndex(prefix, name, format);
		}
		
		private LogIndex(String prefix, String name, String format) {
			this.prefix = prefix;
			this.name = name;
			this.format = new SimpleDateFormat(format);
			this.recalculateIndex();
		}
		
		/**
		 * Recalculates the index. There should be no need to invoke it manually.
		 * @return Instance holding index information.
		 */
		public LogIndex recalculateIndex() {
			String computedDate = this.format.format(new Date());
			this.computedFormat = new StringBuilder()
					.append(this.prefix)
					.append(this.name).append("_")
					.append(computedDate)
					.toString();
			return this;
		}
		
		/**
		 * Returns precomputed index name. Calling the method does not invoke
		 * calculation thus can be called whenever needed without caching the result.
		 * 
		 * @return Index name from Elasticsearch.
		 */
		public String getIndexName() {
			return this.computedFormat;
		}
	}
	
	/**
	 * Creates an authenticated connection to the Elasticsearch cluster using defined index.
	 * @param index Index to be used in queries
	 * @param hosts Address(es) of the node(s)
	 * @param user Username used during authentication.
	 * @param password Password used during authentication.
	 * @return
	 */
	public static EASearch of(LogIndex index, HttpHost[] hosts, String user, String password) {
		return new EASearch(index, hosts, user, password);
	}

	/**
	 * Creates an anonymous connection to the Elasticsearch cluster using defined index.
	 * @param index Index to be used in queries
	 * @param hosts Address(es) of the node(s)
	 * @return
	 */
	public static EASearch ofAnonymous(LogIndex index, HttpHost[] hosts) {
		return new EASearch(index, hosts, null, null);
	}
	
	protected EASearch(LogIndex index, HttpHost[] hosts, String user, String password) {
		this.index = index;
		this.hosts = hosts;
		RestClientBuilder restClientBuilder = RestClient.builder(this.hosts);
		
		if(user != null && !user.isEmpty()) {
			this.credentialsProvider = new BasicCredentialsProvider();
			this.credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
			restClientBuilder.setHttpClientConfigCallback(callback -> {
				return callback.setDefaultCredentialsProvider(this.credentialsProvider);
			});
		} else {
			this.credentialsProvider = null;
		}
		
		this.restClient = new RestHighLevelClient(restClientBuilder);
	}
	
	/**
	 * @return Gets currently used index in queries.
	 */
	public LogIndex getIndex() {
		return this.index;
	}
	
	/**
	 * @return Returns instance of FulltextSearchQuery responsible for entire communication and query generation.
	 */
	public FulltextSearchQuery fulltextSearch() {
		return new FulltextSearchQuery();
	}
	
	/**
	 * Class abstracting creation of the query structure in a more developer friendly manner with minimum-to-no
	 * Elasticsearch required knowledge. Supports several types of leaf query types and compound queries.
	 * Compound (wrapper) queries can be set using {@link #setWrapType(WrapType)}. Together with {@link LogField}
	 * enables easy horizontal and vertical condition generation.
	 * 
	 * @author jurajkubinyi
	 *
	 */
	public class FulltextSearchQuery extends SearchQuery {
		
		public WrapType wrapQueryType = WrapType.sum;
		
		/**
		 * Sets the type of the compound (wrapper) class used. Depending on the setting the Elasticsearch
		 * will return results scores.
		 * @param type
		 * @return
		 */
		public FulltextSearchQuery setWrapType(WrapType type) {
			this.wrapQueryType = type;
			return this;
		}
		
		@Override
		protected QueryBuilder buildQuery() {
			Set<LogField> fields = this.getAllFields();
			if(fields.size() > 1) { // More than field
				if(this.wrapQueryType == WrapType.sum) {
					BoolQueryBuilder rootQuery = QueryBuilders.boolQuery();
					
					for(LogField field : fields) {
						QueryBuilder fieldQuery = field.buildQTree();
						if(field.getConditionType() == ConditionType.must)
							rootQuery.must(fieldQuery);
						else if(field.getConditionType() == ConditionType.mustNot)
							rootQuery.mustNot(fieldQuery);
						else if(field.getConditionType() == ConditionType.should)
							rootQuery.should(fieldQuery);
						else if(field.getConditionType() == ConditionType.filter)
							rootQuery.filter(fieldQuery);
					}
					return rootQuery;
				} else {
					DisMaxQueryBuilder rootQuery = QueryBuilders.disMaxQuery();
					
					for(LogField field : fields) {
						QueryBuilder fieldQuery = field.buildQTree();
						rootQuery.add(fieldQuery);
					}
					return rootQuery;
				}
			} else {
				LogField field = fields.stream().findFirst().get();
				return field.buildQTree();
			}
		}
	}
	
	public abstract class SearchQuery {
		
		private Set<LogField> fieldsSet = new LinkedHashSet<>();
		private Field sortingField;
		private SortOrder sortOrder = SortOrder.ASC;
		private boolean shouldSort = false;
		
		protected abstract QueryBuilder buildQuery();
		
		/**
		 * @return All fields on this level which will be used to generate query.
		 * If you need to traverse entire tree you need to traverse each {@link LogField}
		 * one by one.
		 */
		public Set<LogField> getAllFields() {
			return this.fieldsSet;
		}
		
		/**
		 * Adds the field to this level in the tree.
		 * @param field Field which will be added to the tree.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery addField(LogField field) {
			this.fieldsSet.add(field);
			return this;
		}

		/**
		 * Adds the field to this level in the tree.
		 * @param field Field which will be added to the tree.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery addField(FieldBuilder field) {
			this.fieldsSet.add(field.make());
			return this;
		}
		
		/**
		 * Allows sorting on the result documents. Please see
		 * {@link #sortOrder(SortOrder)} and {@link #noSorting()}.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery useSorting() {
			this.shouldSort = true;
			return this;
		}

		/**
		 * Allows sorting on the result documents. Please see
		 * {@link #sortOrder(SortOrder)} and {@link #useSorting()}.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery noSorting() {
			this.shouldSort = false;
			return this;
		}
		
		/**
		 * Sets the preferred sorting order.
		 * @param sortOrder ASC or DESC.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery sortOrder(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
			return this;
		}
		
		/**
		 * Enables sorting of the result documents on this field.
		 * @param field Field name which will be used for filtering.
		 * @return SearchQuery query building class reference.
		 */
		public SearchQuery sortingOnField(Field field) {
			this.sortingField = field;
			return this;
		}
		
		/**
		 * Builds and executes the query from the {@link LogField}.
		 * @return Elasticsearch's SearchResponse containing result documents in format of Hits.
		 * @throws IOException Exception thrown due unrecoverable fault during build, transmission or execution of the query.
		 */
		public SearchResponse execute() throws IOException {
			SearchRequest searchRequest = this.prepareRequest();
	        SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
	        return searchResponse;
		}

		/**
		 * Builds and executes the query from the {@link LogField} with pagination support.
		 * @return Elasticsearch's SearchResponse containing result documents in format of Hits.
		 * @throws IOException Exception thrown due unrecoverable fault during build, transmission or execution of the query.
		 */
		public SearchResponse executePaginated(int from, int size) throws IOException {
			SearchRequest searchRequest = this.preparePaginatedRequest(from, size);
	        SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
	        return searchResponse;
		}
		
		/**
		 * Builds the Elasticsearch {@link SearchRequest} consisting of queries from {@link LogField}s.
		 * Used during {@link #execute()} and {@link #executePaginated(int, int)}.
		 * @return Elasticsearch {@link SearchRequest} from {@link LogField}s
		 */
		public SearchRequest prepareRequest() {
			SearchSourceBuilder searchSourceBuilder = this.createSourceBuilder();
			
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices(getIndex().getIndexName());
	        searchRequest.source(searchSourceBuilder);
	        return searchRequest;
		}

		/**
		 * Builds the Elasticsearch {@link SearchRequest} with pagination enabled consisting of
		 * queries from {@link LogField}s. Used during {@link #execute()} and
		 * {@link #executePaginated(int, int)}.
		 * @return Elasticsearch {@link SearchRequest} from {@link LogField}s
		 */
		public SearchRequest preparePaginatedRequest(int from, int size) {
			SearchSourceBuilder searchSourceBuilder = this.createSourceBuilder();
			searchSourceBuilder.from(from).size(size);
			
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices(getIndex().getIndexName());
	        searchRequest.source(searchSourceBuilder);
	        return searchRequest;
		}
		
		private SearchSourceBuilder createSourceBuilder() {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			if(this.shouldSort && this.sortingField != null)
				searchSourceBuilder.sort(new FieldSortBuilder(this.sortingField.toString()).order(this.sortOrder));
			searchSourceBuilder.query(this.buildQuery());
			System.out.println(searchSourceBuilder);
			return searchSourceBuilder;
		}
	}
}
