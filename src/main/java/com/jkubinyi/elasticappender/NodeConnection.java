package com.jkubinyi.elasticappender;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Node;

import java.net.InetAddress;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidHost;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name = "NodeConnection", category = Node.CATEGORY, printObject = true)
public class NodeConnection {

    private HttpHost httpHost;
    private static final Logger LOGGER = StatusLogger.getLogger();

    private NodeConnection(InetAddress host, int port, String scheme) {
        this.httpHost = new HttpHost(host, port, scheme);
    }
    
    public static NodeConnection fromLocalhost() {
    	return new NodeConnection(InetAddress.getLoopbackAddress(), 9200, "http");
    }

    public HttpHost getHttpHost() {
        return this.httpHost;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
    * @return A short description of the NodeConnection. The format is
    * NOT specified and is subject to change without further notices.
    * The following example may be shown as standard:
    * "ELASTICSEARCH: NodeConnection[http://localhost:9200]"
    */
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("ELASTICSEARCH:NodeConnection[")
    	.append(httpHost.toString())
    	.append("]");
    	return sb.toString();
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<NodeConnection> {

        @PluginBuilderAttribute
        private String scheme;

        @PluginBuilderAttribute
        @ValidHost
        private InetAddress host; // Localhost

        @PluginBuilderAttribute
        @ValidPort
        private int port = 9200; // Default Elasticsearch listening REST port

        public Builder setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder setHost(InetAddress host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public NodeConnection build() {
        	if(this.scheme == null) {
        		LOGGER.warn("Scheme was not set for NodeConnection. Using http as default.");
        		this.scheme = "http";
        	}
        	
            return new NodeConnection(this.host, this.port, this.scheme);
        }
    }

}
