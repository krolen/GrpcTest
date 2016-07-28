package my.tests.grpc.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 7/28/2016.
 */
public class SemaphoreTest {

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1, true);

        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build()).submit(() -> {
            while (true) {
                semaphore.acquireUninterruptibly();
                try {
                    for (int i = 0; i < 3; i++) {
                        System.out.println("Thread one " + i);
                    }
                } finally {
                    semaphore.release();
                }
            }
        });


        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build()).submit(() -> {
            while (true) {
                semaphore.acquireUninterruptibly();
                try {
                    for (int i = 0; i < 3; i++) {
                        System.out.println("Thread two " + i);
                    }
                } finally {
                    semaphore.release();
                }
            }
        });

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
}
