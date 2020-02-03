package com.jkubinyi.elasticappender.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jkubinyi.elasticappender.L4JElasticAppender;

/**
 * Class storing information about index.
 * 
 * @author jurajkubinyi
 */
public class LogIndex {
	private final String prefix;
	private final String name;
	private final DateFormat format;
	private volatile String computedFormat;

	/**
	 * Creates a standard index reference in conjunction with
	 * {@link L4JElasticAppender}. This should be used by most projects.
	 * 
	 * @param name   Name of the index from Log4J configuration.
	 * @param format Format of the index from Log4J configuration.
	 * @return Instance holding information about the index including calculated
	 *         name.
	 */
	public static LogIndex ofStandard(String name, String format) {
		return new LogIndex(L4JElasticAppender.getPrefix(), name, format);
	}

	/**
	 * Creates a custom index reference using provided prefix.
	 * 
	 * @param prefix Prefix of the index.
	 * @param name   Name of the index from Log4J configuration.
	 * @param format Format of the index from Log4J configuration.
	 * @return Instance holding information about the index including calculated
	 *         name.
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
	 * 
	 * @return Instance holding index information.
	 */
	public LogIndex recalculateIndex() {
		String computedDate = this.format.format(new Date());
		this.computedFormat = new StringBuilder().append(this.prefix).append(this.name).append("_")
				.append(computedDate).toString();
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