package com.jkubinyi.elasticappender.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.jkubinyi.elasticappender.batch.Batcher.BatchProcessor;

/**
 * Batching class used to create batches of objects with help of
 * blocking queue. Concrete queue implementation backing the batcher
 * will vary depending on the constructor parameter 'maxUnprocessed'.
 * 
 * When there are enough elements it will create a new batch of the
 * elements and process it using defined {@link BatchProcessor} implementation.
 * 
 * @author jurajkubinyi
 * @param <Q> Type of object which will be batched.
 */
public class BlockingQueueBatcher<Q> implements Batcher<Q> {

	private final int batchSize;
	private final BlockingQueue<Q> queue;
	private final BatchProcessor<Q> batchProcessor;
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * @param batchProcessor Instance of processor class having logic to process batches of elements.
	 * @param batchSize Count of the elements in one group (batch) processed by the processor.
	 */
	public BlockingQueueBatcher(BatchProcessor<Q> batchProcessor, int batchSize) {
		this(batchProcessor, batchSize, 0);
	}
	
	/**
	 * @param batchProcessor Instance of processor class having logic to process batches of elements.
	 * @param batchSize Count of the elements in one group (batch) processed by the processor.
	 * @param maxUnprocessed Maximum number of unprocessed elements which could be present at the same time in the batcher. Setting it to 0 means unlimited number.
	 */
	public BlockingQueueBatcher(BatchProcessor<Q> batchProcessor, int batchSize, int maxUnprocessed) {
		this.batchProcessor = batchProcessor;
		this.batchSize = batchSize;
		if(maxUnprocessed > 0)
			this.queue = new ArrayBlockingQueue<>(maxUnprocessed);
		else
			this.queue = new LinkedBlockingQueue<>();
	}
	
	public boolean addAll(Collection<Q> collection) {
		synchronized(this.lock) {
			return this.queue.addAll(collection);
		}
	}
	
	public boolean offer(Q obj, long timeout, TimeUnit unit) throws InterruptedException {
		if(this.lock.tryLock(timeout, unit)) {
			try {
				if(this.queue.offer(obj)) {
					if (this.queue.size() >= this.batchSize) this.batchAll();
					return true;
				}
			} finally {
				this.lock.unlock();
			}
		}
		return false;
	}
	
	public boolean add(Q obj) throws InterruptedException {
		this.lock.lockInterruptibly();
		try {
			if(this.queue.offer(obj)) {
				if(this.queue.size() >= this.batchSize) this.batchAll();
				return true;
			}
		} finally {
			this.lock.unlock();
		}
		return false;
	}
	
	/**
	 * Will drain entire queue into the collection and run the processor on the same thread.
	 */
	private void batchAll() {
		List<Q> array = new ArrayList<>();
		this.queue.drainTo(array);
		this.batchProcessor.process(array);
	}
}
