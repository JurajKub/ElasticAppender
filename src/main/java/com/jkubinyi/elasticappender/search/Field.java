package com.jkubinyi.elasticappender.search;

public enum Field {
	contextMap("contextMap"),
	endOfBatch("endOfBatch"),
	level("level"),
	loggerFQCN("loggerFqcn"),
	loggerName("loggerName"),
	message("message"),
	creationTimeMs("timeMillis"),
	thread("thread"),
	threadId("threadId"),
	threadPriority("threadPriority"),
	sourceClass("source.class"),
	sourceMethod("source.method"),
	sourceFile("source.file"),
	sourceLine("source.line"),
	sourceClassLoaderName("source.classLoaderName");
	
	
	private final String treePosition;       

    private Field(String treePosition) {
        this.treePosition = treePosition;
    }

    public boolean equalsPosition(String position) {
        return this.treePosition.equals(position);
    }

    public String toString() {
       return this.treePosition;
    }
}
