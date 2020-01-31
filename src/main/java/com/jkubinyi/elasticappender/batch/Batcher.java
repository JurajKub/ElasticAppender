package com.jkubinyi.elasticappender.batch;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Used to "prettify" and abstract away underlying batching algorithm.
 * In future there can be several different algorithms implementing the
 * interface possible each having goal to solve different set of tasks.
 * 
 * @author jurajkubinyi
 * @param <Q> Type of object which will be batched.
 */
public interface Batcher<Q> {

	/**
	 * Functional interface used to define a business logic
	 * processing batch of the elements.
	 * 
	 * @author jurajkubinyi
	 * @param <O> Type of the elements inside the {@link Batcher}.
	 */
	@FunctionalInterface
	public interface BatchProcessor<O> {
		public void process(Collection<O> work);
	}
	
	/**
	 * Adds entire content of Collection back to the {@link Batcher}. 
	 * Resulting order can vary by implementation but it is strongly suggested
	 * in each implementation result should be the same as calling
	 * {@link #add(Object)} for each individual object in the Collection.
	 * 
	 * @param collection Collection to be added back to the {@link Batcher}
	 * @return if returns {@code false} the collection may have at least part
	 * of the elements added to the {@link Batcher}. It may be feasible to
	 * call the method till it returns true with retry limit.
	 */
	public boolean addAll(Collection<Q> collection);
	
	/**
	 * Add element to the batcher possible blocking infinite amount of time.
	 * The method can be interrupted by interrupting the thread it is running
	 * on.
	 * 
	 * @param obj Element to be added to the batch.
	 * @return {@code false} if the element could not be added to the batcher, possible having
	 * more elements not processed than maxUnprocessed parameter during batcher initialization.
	 * 
	 * @throws InterruptedException
	 */
	public boolean add(Q obj) throws InterruptedException;
	
	/**
	 * Try to add an element to the batcher if possible while waiting for
	 * maximum of defined amount of time.
	 * 
	 * @param obj Element to be added to the batch.
	 * @param timeout Maximum number of units to wait
	 * @param unit Unit of time to wait
	 * @return {@code false} if the element could not be added to the batcher, possible having
	 * more elements not processed than maxUnprocessed parameter during batcher initialization
	 * or timed out while trying to add element to the batcher.
	 */
	public boolean offer(Q obj, long timeout, TimeUnit unit) throws InterruptedException;
}