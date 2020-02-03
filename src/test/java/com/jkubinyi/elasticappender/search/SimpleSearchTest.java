package com.jkubinyi.elasticappender.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jkubinyi.elasticappender.record.LogRecord;
import com.jkubinyi.elasticappender.search.EASearch.LogIndex;
import com.jkubinyi.elasticappender.search.common.Field;

public class SimpleSearchTest {
	
	private EASearch eaSearch;
	private SimplifiedSearch search;
	private Logger log = LogManager.getLogger(SimpleSearchTest.class);
	private static final String TEST_SIMPLIFIED_MESSAGE = "Starting SimplifiedSearch test.";
	
	@Before()
	public void preInit() {
		this.eaSearch = EASearch.ofAnonymous(
			LogIndex.ofStandard("log", "yyyyMMdd"),
			new HttpHost[] { new HttpHost("URL", 9200, "http") }
		);
		
		this.search = new SimplifiedSearch(this.eaSearch);
		
		log.error(SimpleSearchTest.TEST_SIMPLIFIED_MESSAGE);
	}
	
	@Test
	public void queryReturnsAtLeastOneFT() throws IOException {
		Set<LogRecord> logs = this.search.fulltext(Field.message, "Starting")
		.paginated(0, 100)
		.execute();
		
		assertTrue(logs.size() > 0);
	}
	
	@Test
	public void queryReturnsAtLeastOneFTMatch() throws IOException {
		Set<LogRecord> logs = this.search.matches(Field.message, "Starting", false)
		.paginated(0, 100)
		.execute();
		
		assertTrue(logs.size() > 0);
	}

	@Test
	public void queryReturnsAtLeastOneTerm() throws IOException {
		Set<LogRecord> logs = this.search.matches(Field.message, SimpleSearchTest.TEST_SIMPLIFIED_MESSAGE, true)
		.paginated(0, 100)
		.execute();
		
		assertTrue(logs.size() > 0);
	}
}
