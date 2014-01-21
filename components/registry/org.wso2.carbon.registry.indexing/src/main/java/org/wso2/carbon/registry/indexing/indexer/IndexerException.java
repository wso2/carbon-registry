package org.wso2.carbon.registry.indexing.indexer;

public class IndexerException extends Exception {
	
    private static final long serialVersionUID = 1L;

	public IndexerException(String message) {
		super(message);
	}
	
	public IndexerException(String message, Throwable cause) {
		super(message, cause);
	}

}
