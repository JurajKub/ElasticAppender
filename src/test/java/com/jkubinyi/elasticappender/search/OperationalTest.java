package com.jkubinyi.elasticappender.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkubinyi.elasticappender.record.LogRecord;
import com.jkubinyi.elasticappender.search.EASearch.BooleanQuery;
import com.jkubinyi.elasticappender.search.EASearch.LogIndex;
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
	public void searchLogByMessage() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		BooleanQuery generator = this.search.fulltextSearch();
		Group generator2 = generator
		.term(Field.message, OperationalTest.TEST_TERM_MESSAGE);
		
		SearchResponse response = generator.execute();
		System.out.println(response.getHits().getHits().length);
		List<LogRecord> logs = Arrays.stream(response.getHits().getHits())
    		    .filter(hit->hit.hasSource()) // For some reason source could be null.
    		    .map(hit -> {
					try {
						return mapper.readValue(BytesReference.toBytes(hit.getSourceRef()), LogRecord.class);
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				})
    		    .filter(Objects::nonNull)
    		    .collect(Collectors.toList());
		System.out.println(generator.toString());
		
		for(LogRecord log : logs) {
			System.out.println(log.getMessage());
		}
		
		assertEquals(generator, generator2);
	}
}
