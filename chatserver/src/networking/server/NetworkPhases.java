package networking.server;

public enum NetworkPhases
{
	/**
	 * Key exchange process
	 */
	PRE0,
	
	/**
	 * Protocol check
	 */
	PRE1,
	
	/**
	 * Identifying process
	 */
	PRE2,
	
	/**
	 * Communication process
	 */
	COM,
	
	/**
	 * Post process
	 */
	POST
}
