package com.jkubinyi.elasticappender.search.common;

/**
 * Mapped all of the default ElasticAppender fields stored in Elasticsearch.
 * Provides an additional information on each mapping.
 * 
 * @author jurajkubinyi
 */
public enum Field {
	contextMap("contextMap", false),
	endOfBatch("endOfBatch", false),
	level("level", true),
	loggerFQCN("loggerFqcn", true),
	loggerName("loggerName", true),
	message("message", true),
	creationTimeMs("timeMillis", false),
	thread("thread", true),
	threadId("threadId", false),
	threadPriority("threadPriority", false),
	sourceClass("source.class", true),
	sourceMethod("source.method", true),
	sourceFile("source.file", true),
	sourceLine("source.line", false),
	sourceClassLoaderName("source.classLoaderName", true);
	
	
	private final String treePosition;     
	private final boolean hasKeyword;

    private Field(String treePosition, boolean hasKeyword) {
        this.treePosition = treePosition;
        this.hasKeyword = hasKeyword;
    }
    
    /**
     * Returns whether the field has a keyword which can be used to make direct, unanalyzed
     * comparisons against String value.
     * @return If {@code true} the code can use keyword when appropriate.
     */
    public boolean hasKeyword() {
		return this.hasKeyword;
	}
    
    /**
     * @return Returns a field's keyword if {@link #hasKeyword} is {@code true} else
     * returns regular {@link #toString()}
     */
    public String toKeywordString() {
    	if(this.hasKeyword) 
    		return this.toString() + ".keyword";
    	else
    		return this.toString();
    }
    
    /**
     * Whether the enum equals checking not by the name but by the tree location.
     * 
     * @param position Position in JSON tree with levels delimited by dot.<br>
     * <b>Example:</b> parent.child.grandchild
     * @return
     */
	public boolean equalsPosition(String position) {
        return this.treePosition.equals(position);
    }

	/**
	 * Returns a tree position of the field. The exact details
	 * of the representation are unspecified and subject to change.
	 * The following may be regarded as typical:<br>
     * parent.child.grandchild
     * 
     * @return The field's tree position.
	 */
    public String toString() {
       return this.treePosition;
    }
}
