package com.jkubinyi.elasticappender.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jkubinyi.elasticappender.common.LogIndex;
import com.jkubinyi.elasticappender.search.EASearch.BooleanQueryBuilder;
import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.query.Group;

public class OperationalTest {
	
	private EASearch search;
	private Logger log = LogManager.getLogger(OperationalTest.class);
	private static final String TEST_TERM_MESSAGE = "Testing TERM search.";
	
	@Before()
	public void preInit() {
		this.search = EASearch.ofAnonymous(
			LogIndex.ofStandard("log", "yyyyMMdd"),
			new HttpHost[] { new HttpHost("URL", 9200, "http") }
		);
		
		log.error(OperationalTest.TEST_TERM_MESSAGE);
	}
	
	@Test
	public void queryTreeReturnsSuperParent() throws IOException {
		BooleanQueryBuilder generator = this.search.booleanQuery();
		Group generator2 = generator
		.term(Field.message, OperationalTest.TEST_TERM_MESSAGE);
		
		assertEquals(generator, generator2);
	}
	
	@Test
	public void queryReturnsAtLeastOne() throws IOException {
		BooleanQueryBuilder generator = this.search.booleanQuery();
		generator.term(Field.message, OperationalTest.TEST_TERM_MESSAGE);
		
		assertNotEquals(0, generator.execute().size());
	}
}
