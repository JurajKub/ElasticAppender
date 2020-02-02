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
"class",
"method",
"file",
"line",
"classLoaderName"
})
public class Source {

@JsonProperty("class")
private String _class;
@JsonProperty("method")
private String method;
@JsonProperty("file")
private String file;
@JsonProperty("line")
private Integer line;
@JsonProperty("classLoaderName")
private String classLoaderName;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("class")
public String getClass_() {
return _class;
}

@JsonProperty("class")
public void setClass_(String _class) {
this._class = _class;
}

@JsonProperty("method")
public String getMethod() {
return method;
}

@JsonProperty("method")
public void setMethod(String method) {
this.method = method;
}

@JsonProperty("file")
public String getFile() {
return file;
}

@JsonProperty("file")
public void setFile(String file) {
this.file = file;
}

@JsonProperty("line")
public Integer getLine() {
return line;
}

@JsonProperty("line")
public void setLine(Integer line) {
this.line = line;
}

@JsonProperty("classLoaderName")
public String getClassLoaderName() {
return classLoaderName;
}

@JsonProperty("classLoaderName")
public void setClassLoaderName(String classLoaderName) {
this.classLoaderName = classLoaderName;
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