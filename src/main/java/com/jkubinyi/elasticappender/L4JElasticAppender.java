package com.jkubinyi.elasticappender;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.JsonLayout;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.jkubinyi.elasticappender.batch.Batcher;
import com.jkubinyi.elasticappender.batch.BlockingQueueBatcher;
import com.jkubinyi.elasticappender.common.LogIndex;
import com.jkubinyi.elasticappender.search.EASearch;
import com.jkubinyi.elasticappender.batch.Batcher.BatchProcessor;

/**
 * <p>Elasticsearch Log4J 2 appender using asynchronous bulk operations to insert the batches
 * of logs into the appropriate index. Class provides a way to customize bulk size,
 * authentication used with Elasticsearch, index name, date format, etc.</p>
 * <p>It allows to add one or more than one Elasticsearch nodes. In the latter case it uses a
 * robin round mechanism to persist the batches.</p>
 * 
 * @author jurajkubinyi
 *
 */
@Plugin(name = "ElasticAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class L4JElasticAppender extends AbstractAppender {
	
	private final NodeConnection[] nodeConnections;
	private final boolean useBulk;
	private final int bulkSize;
	private final String user;
	private final String password;
	private final int maxUnprocessedLogs;

	/** Number of logs which were not persisted. */
	private final AtomicLong swallowedLogs = new AtomicLong(0);

	/** Batcher instance having the batching logic. */
	private final Batcher<IndexRequest> requestAsyncBatch;

	/** Processor instance having the processing of the batches logic. */
	private final BatchProcessor<IndexRequest> asyncBatchProcessor = new AsyncBatchProcessor();

	/** Computed index used to distinguish log indexes. */
	private final LogIndex logIndex;
	
	/** Used for bulk sending. */
	private RestHighLevelClient restClient;
	
	/** CredentialsProvider containing potential username and password **/
	private CredentialsProvider credentialsProvider;
	
	/** Used to identify the index belongs to this appender.
	 * Example usage: 3rd party app can fetch all logs made by this appender from shared Elasticsearch cluster.
	 */
	private static final String INDEX_PREFFIX = "l4jea_";

	/**
	 * Batching logic using {@link RestHighLevelClient}'s async bulk operations.
	 * 
	 * @author jurajkubinyi
	 */
	private class AsyncBatchProcessor implements BatchProcessor<IndexRequest> {

		@Override
		public void process(Collection<IndexRequest> work) {
			BulkRequest request = new BulkRequest();
			work.forEach(request::add);
			LOGGER.debug("{}", restClient);
			restClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {

				@Override
				public void onResponse(BulkResponse response) {
					// Do nothing
				}

				@Override
				public void onFailure(Exception e) {
					LOGGER.error("Error during persisting batch. Will retry next time.", e);
					requestAsyncBatch.addAll(work);
				}
			});
		}

	}

	/**
	 * Creates Elasticsearch Appender for Log4J 2. Probably you should not initialize the class
	 * directly and let Log4J handle it's lifecycle.
	 * 
	 * @param name Name of the appender.
	 * @param filter Log4J Filter.
	 * @param layout Layout must be json.
	 * @param ignoreExceptions If you wish to ignore exceptions.
	 * @param index Base of the index name used for the logging.
	 * @param nodeConnections {@link NodeConnection}s of pointing to the Elasticsearch node.
	 * @param useBulk If {@code false} it will pretend to not divide logs into the batch and rather send them one by one.
	 * It may still use the same overhead as batching algorithm, but sends them right off the bat without waiting to fill the space.
	 * @param bulkSize Number of elements needed to wait before trying to persist them in Elasticsearch.
	 * @param user It will try to authenticate using the username and password if it is not null.
	 * @param password If username is not null the password will be used during authentication.
	 * @param maxUnprocessedLogs Maximum number of unprocessed, piled up logs in the cache. No more logs will be persisted when the cache
	 * is full till they will be cleared by the batching algorithm.
	 * @param logIndex The actual index in which will be the logs stored to.
	 */
	private L4JElasticAppender(String name, Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions,
			NodeConnection[] nodeConnections, boolean useBulk, int bulkSize,
			String user, String password, int maxUnprocessedLogs, LogIndex logIndex) {
		super(name, filter, layout, ignoreExceptions);
		this.nodeConnections = nodeConnections;
		this.useBulk = useBulk;
		this.user = user;
		this.password = password;
		this.logIndex = logIndex;
		if(!useBulk || bulkSize < 2)
			this.bulkSize = 1;
		else
			this.bulkSize = bulkSize;

		if(maxUnprocessedLogs != 0 && this.bulkSize > maxUnprocessedLogs) {
			LOGGER.warn("MaxUnprocessedLogs is smaller than bulkSize. Upsizing maxUnprocessedLogs to avoid dropping messages.");
			this.maxUnprocessedLogs = bulkSize * 2;
		} else
			this.maxUnprocessedLogs = maxUnprocessedLogs;

		this.requestAsyncBatch = new BlockingQueueBatcher<IndexRequest>(this.asyncBatchProcessor, this.bulkSize, this.maxUnprocessedLogs);

		this.validate();
		this.createRestClient();
		this.calculateCurrentDate();
	}
	
	/**
	 * @return Gets the currently used prefix for the index name.
	 */
	public static String getPrefix() {
		return L4JElasticAppender.INDEX_PREFFIX;
	}
	
	/**
	 * @return Gets the currently used Elasticsearch nodes.
	 */
	public NodeConnection[] getNodeConnections() {
		return this.nodeConnections;
	}

	/**
	 * @return Returns {@code true} if appender was set to use logs during initialization. It MAY behave as
	 * using batching algorithm despite returning {@code false} depending on the configuration.
	 */
	public boolean usesBulk() {
		return this.useBulk;
	}

	/**
	 * Used to calculate a new date and thus index name using current date/time.
	 */
	public void calculateCurrentDate() {
		this.logIndex.recalculateIndex();
	}

	/**
	 * @return Number of logs which were not persisted.
	 */
	public long getNumSwallowed() {
		return this.swallowedLogs.get();
	}

	/**
	 * @return Computed index name to persist logs into.
	 */
	public String getCurrentIndexString() {
		return this.logIndex.getIndexName();
	}
	
	/**
	 * @return Computing class instance responsible for index name generation.
	 */
	public LogIndex getCurrentIndex() {
		return this.logIndex;
	}
	
	/**
	 * Can be used by {@link EASearch} or other classes to get a <b>new</b> connection
	 * to the cluster using the original nodes informations and authorization.
	 * 
	 * @return Brand new Elasticsearch connection duplicating currently used.
	 */
	public RestHighLevelClient duplicateConnection() {
		Objects.requireNonNull(this.restClient, "RestClient is not created yet.");
		
		HttpHost[] hosts = Arrays.stream(this.nodeConnections)
				.map(node -> node.getHttpHost())
				.collect(Collectors.toList())
				.toArray(new HttpHost[this.nodeConnections.length]);
		
		RestClientBuilder restClientBuilder = RestClient.builder(hosts);
		
		if(this.credentialsProvider != null) {
			restClientBuilder.setHttpClientConfigCallback(callback -> {
				return callback.setDefaultCredentialsProvider(this.credentialsProvider);
			});
		}
		
		return new RestHighLevelClient(restClientBuilder);
	}

	/**
	 * Creates a low level and high level RestClient for the Elasticsearch using configured nodes and
	 * authentication.
	 */
	private void createRestClient() {
		HttpHost[] hosts = Arrays.stream(this.nodeConnections)
				.map(node -> node.getHttpHost())
				.collect(Collectors.toList())
				.toArray(new HttpHost[this.nodeConnections.length]);

		RestClientBuilder restClientBuilder = RestClient.builder(hosts);
		
		if(this.user != null && !this.user.isEmpty()) {
			this.credentialsProvider = new BasicCredentialsProvider();
			this.credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.user, this.password));
			restClientBuilder.setHttpClientConfigCallback(callback -> {
				return callback.setDefaultCredentialsProvider(this.credentialsProvider);
			});
		}

		this.restClient = new RestHighLevelClient(restClientBuilder);
	}

	/**
	 * For now this appender only supports JSON content type.
	 */
	private void validate() {
		if(this.getLayout() != null) {
			if(!this.getLayout().getContentType().toLowerCase().contains("application/json"))
				throw new InvalidParameterException("Layout must produce an \"application/json\" content type.");
		} else throw new InvalidParameterException("Layout does not exist.");
	}

	@PluginBuilderFactory
	public static <B extends Builder<B>> B newBuilder() {
		return new Builder<B>().asBuilder();
	}

	@Override
	public void append(LogEvent event) {
		String log = new String(this.getLayout().toByteArray(event));
		IndexRequest logRequest = new IndexRequest(this.getCurrentIndexString())
				.source(log, XContentType.JSON);
		try {
			if(!this.requestAsyncBatch.offer(logRequest, 1, TimeUnit.SECONDS)) {
				LOGGER.warn("Log swallowed due to exhausted consumer. Try exceeding maxUnprocessedLogs or setting to 0?");
				this.swallowedLogs.incrementAndGet();
			}
		} catch(Exception e) {
			LOGGER.error("Error during stashing log: ", e);
		}
	}

	/**
	 * Appender's builder used by the Log4J during starting according to the Log4J configuration.
	 * 
	 * @author jurajkubinyi
	 * @param <B> Appender
	 */
	public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B> 
	implements org.apache.logging.log4j.core.util.Builder<AbstractAppender> {

		@PluginBuilderAttribute
		@Required(message = "No 'index' configured.")
		private String index;

		@PluginElement("nodes")
		private NodeConnection[] connectionNodes;

		@PluginBuilderAttribute
		private boolean useBulk = true;

		@PluginBuilderAttribute
		private int bulkSize = 10;

		@PluginBuilderAttribute
		private String user;

		@PluginBuilderAttribute
		private String password;

		@PluginBuilderAttribute
		private int maxUnprocessedLogs = 0; // 0 means unlimited

		@PluginBuilderAttribute
		private String dateFormat = "yyyyMMdd";

		/**
		 * @param index Base of the index name used for the logging.
		 */
		public void setIndex(String index) {
			this.index = index;
		}

		/**
		 * @param connectionNodes {@link NodeConnection}s of pointing to the Elasticsearch node.
		 */
		public void setConnectionNodes(NodeConnection[] connectionNodes) {
			this.connectionNodes = connectionNodes;
		}

		/**
		 * @param useBulk If {@code false} it will pretend to not divide logs into the batch and rather send them one by one. 
		 * It may still use the same overhead as batching algorithm, but sends them right off the bat without
		 * waiting to fill the space.
		 */
		public void useBulk(boolean useBulk) {
			this.useBulk = useBulk;
		}

		/**
		 * @param bulkSize Number of elements needed to wait before trying to persist them in Elasticsearch.
		 */
		public void setBulkSize(int bulkSize) {
			this.bulkSize = bulkSize;
		}

		/**
		 * @param user It will try to authenticate using the username and password if it is not null.
		 */
		public void setUser(String user) {
			this.user = user;
		}

		/**
		 * @param password If {@link #setUser(String)} is set the password will be used during authentication.
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @param maxUnprocessedLogs Maximum number of unprocessed, piled up logs in the cache.
		 * No more logs will be persisted when the cache is full till they will be cleared by
		 * the batching algorithm.
		 */
		public void setMaxUnprocessedLogs(int maxUnprocessedLogs) {
			this.maxUnprocessedLogs = maxUnprocessedLogs;
		}

		/**
		 * @param dateFormat The actual computed date using this format will be appended to the
		 * index name in order to create a unique index name.
		 */
		public void setDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
		}

		@Override
		public Layout<? extends Serializable> getOrCreateLayout() {
			return this.getOrCreateLayout(Charset.defaultCharset());
		}

		@Override
		public Layout<? extends Serializable> getOrCreateLayout(final Charset charset) {
			if(this.getLayout() == null) {
				return JsonLayout.newBuilder()
						.setCompact(true)
						.setCharset(charset)
						.setIncludeStacktrace(true)
						.setLocationInfo(true)
						.setProperties(true)
						.build();
			}
			return this.getLayout();
		}

		@Override
		public AbstractAppender build() {
			if (this.dateFormat == null) {
				LOGGER.warn("No date format found for appender {}. Using format yyyyMMdd.", this.getName());
				this.dateFormat = "yyyyMMdd";
			}

			if (this.connectionNodes == null || this.connectionNodes.length == 0) {
				LOGGER.warn("No NodeConnections found for ElasticAppender {}. Using localhost with default port without https. (http://localhost:9200)", getName());
				this.connectionNodes = new NodeConnection[] { NodeConnection.fromLocalhost() };
			}

			return new L4JElasticAppender(this.getName(), this.getFilter(), this.getOrCreateLayout(), this.isIgnoreExceptions(), this.connectionNodes,
					this.useBulk, this.bulkSize, this.user, this.password, this.maxUnprocessedLogs, LogIndex.ofStandard(index, dateFormat));
		}
	}
}
