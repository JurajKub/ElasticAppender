package com.jkubinyi.elasticappender.record;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"timeMillis",
"thread",
"level",
"loggerName",
"message",
"endOfBatch",
"loggerFqcn",
"contextMap",
"threadId",
"threadPriority",
"source"
})
public class LogRecord {

@JsonProperty("timeMillis")
private Long timeMillis;
@JsonProperty("thread")
private String thread;
@JsonProperty("level")
private String level;
@JsonProperty("loggerName")
private String loggerName;
@JsonProperty("message")
private String message;
@JsonProperty("endOfBatch")
private Boolean endOfBatch;
@JsonProperty("loggerFqcn")
private String loggerFqcn;
@JsonProperty("contextMap")
private ContextMap contextMap;
@JsonProperty("threadId")
private Integer threadId;
@JsonProperty("threadPriority")
private Integer threadPriority;
@JsonProperty("source")
private Source source;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("timeMillis")
public Long getTimeMillis() {
return timeMillis;
}

@JsonProperty("timeMillis")
public void setTimeMillis(Long timeMillis) {
this.timeMillis = timeMillis;
}

@JsonProperty("thread")
public String getThread() {
return thread;
}

@JsonProperty("thread")
public void setThread(String thread) {
this.thread = thread;
}

@JsonProperty("level")
public String getLevel() {
return level;
}

@JsonProperty("level")
public void setLevel(String level) {
this.level = level;
}

@JsonProperty("loggerName")
public String getLoggerName() {
return loggerName;
}

@JsonProperty("loggerName")
public void setLoggerName(String loggerName) {
this.loggerName = loggerName;
}

@JsonProperty("message")
public String getMessage() {
return message;
}

@JsonProperty("message")
public void setMessage(String message) {
this.message = message;
}

@JsonProperty("endOfBatch")
public Boolean getEndOfBatch() {
return endOfBatch;
}

@JsonProperty("endOfBatch")
public void setEndOfBatch(Boolean endOfBatch) {
this.endOfBatch = endOfBatch;
}

@JsonProperty("loggerFqcn")
public String getLoggerFqcn() {
return loggerFqcn;
}

@JsonProperty("loggerFqcn")
public void setLoggerFqcn(String loggerFqcn) {
this.loggerFqcn = loggerFqcn;
}

@JsonProperty("contextMap")
public ContextMap getContextMap() {
return contextMap;
}

@JsonProperty("contextMap")
public void setContextMap(ContextMap contextMap) {
this.contextMap = contextMap;
}

@JsonProperty("threadId")
public Integer getThreadId() {
return threadId;
}

@JsonProperty("threadId")
public void setThreadId(Integer threadId) {
this.threadId = threadId;
}

@JsonProperty("threadPriority")
public Integer getThreadPriority() {
return threadPriority;
}

@JsonProperty("threadPriority")
public void setThreadPriority(Integer threadPriority) {
this.threadPriority = threadPriority;
}

@JsonProperty("source")
public Source getSource() {
return source;
}

@JsonProperty("source")
public void setSource(Source source) {
this.source = source;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}