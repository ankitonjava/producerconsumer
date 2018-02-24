import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerProblem
{

    public static void main(String args[]) throws InterruptedException
    {
        Lock lock = new ReentrantLock();
        Condition producerMaxQueueCondition = lock.newCondition();
        Condition consumerQueueEmptyCondition = lock.newCondition();
        int maxQueuSize = 2;
        Queue<Integer> queue = new LinkedList<>();

        Thread producer = new Thread(new RecordProducer(lock, producerMaxQueueCondition, consumerQueueEmptyCondition, queue, maxQueuSize), "Producer");
        Thread consumer = new Thread(new RecordConsumer(lock, maxQueuSize, producerMaxQueueCondition, consumerQueueEmptyCondition, queue), "Consumer");
        producer.start();
        consumer.start();

        Thread.sleep(20000);
        producer.interrupt();
        consumer.interrupt();
    }

}


/**
 * Record producer
 */
class RecordProducer implements Runnable
{
    private Lock lock;

    private int maxQueuSize;

    private Condition producerMaxQueueCondition;

    private Queue<Integer> queue;

    private Condition consumerQueueEmptyCondition;

    private int counter;

    public RecordProducer(Lock lock, Condition producerMaxQueueCondition, Condition consumerQueueEmptyCondition, Queue<Integer> queue, int maxQueuSize)
    {
        this.lock = lock;
        this.producerMaxQueueCondition = producerMaxQueueCondition;
        this.queue = queue;
        this.maxQueuSize = maxQueuSize;
        this.consumerQueueEmptyCondition = consumerQueueEmptyCondition;
    }

    private void produceRecords() throws InterruptedException
    {
        while (queue.size() != maxQueuSize)
        {
            queue.add(counter);
            counter++;
        }
        this.consumerQueueEmptyCondition.signalAll();
        producerMaxQueueCondition.await();
        System.out.println(Thread.currentThread().getName() + " - Done with producing records..");
    }

    @Override
    public void run()
    {
        lock.lock();
        try
        {
            while (true)
            {
                produceRecords();
                Thread.sleep(2000);
            }

        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            lock.unlock();
        }

    }
}


/**
 * Record consumer thread
 */
class RecordConsumer implements Runnable
{
    private Lock lock;

    private Condition producerMaxQueueCondition;

    private Condition consumerQueueEmptyCondition;

    private Queue<Integer> queue;

    public RecordConsumer(Lock lock, int maxQueuSize, Condition producerMaxQueueCondition, Condition consumerQueueEmptyCondition, Queue<Integer> queue)
    {
        this.lock = lock;
        this.producerMaxQueueCondition = producerMaxQueueCondition;
        this.consumerQueueEmptyCondition = consumerQueueEmptyCondition;
        this.queue = queue;
    }

    private void consumeRecords()
    {
        while (queue.size() > 0)
        {
            System.out.println(queue.poll());
        }
        System.out.println(Thread.currentThread().getName() + " - Done with consuming records..");

    }

    @Override
    public void run()
    {
        lock.lock();
        try
        {
            while (true)
            {
                consumeRecords();
                producerMaxQueueCondition.signalAll();
                consumerQueueEmptyCondition.await();
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            lock.unlock();
        }
    }
}