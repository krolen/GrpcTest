package my.tests.grpc.server;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import my.tests.grpc.proto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class TestServiceImpl extends TestServiceGrpc.TestServiceImplBase {

    @Override
    public void mySyncCall(Empty request, StreamObserver<SimpleResponse> responseObserver) {
        SynchronizedObserverDelegate<SimpleResponse> observer = new SynchronizedObserverDelegate<>(responseObserver);
//        StreamObserver<SimpleResponse> observer = responseObserver;
        ExecutorService service = Executors.newFixedThreadPool(5);
        AtomicInteger count = new AtomicInteger(0);
        List<Future> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 1000_000; i++) {
                final int finalI = i;
                Future<Integer> future = service.submit(() -> {
                    SimpleResponse response = SimpleResponse.newBuilder().
                        setStatusMessage(StatusMessage.newBuilder().setStatus(StatusEnum.COMMENT).setComment("This is status " + finalI)).
                        setData(Data.newBuilder().setIntData(finalI).setTextData("This is data " + finalI)).build();
                    observer.onNext(response);
                    if (finalI % 1000 == 0) {
                        System.out.println("Emitted " + finalI);
                    }
                    return finalI;
                });
                count.incrementAndGet();
                futures.add(future);
            }

            for (Future future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    Throwables.propagate(e);
                }
            }
        } finally {
            System.out.println("Completing job");
            responseObserver.onCompleted();
            System.out.println("Job completed");
        }

        System.out.println("Total emitted:" + count.get());
    }

    @Override
    public StreamObserver<SimpleRequest> myAsyncCall(StreamObserver<SimpleResponse> responseObserver) {
//        ExecutorService service = Executors.newFixedThreadPool(5);
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean continueProcess = new AtomicBoolean(true);

        ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
        service.submit(() -> {
            while (count.get() < 32) {
                if (continueProcess.get()) {
                    int i = count.incrementAndGet();
                    System.out.println("Sending server msg " + i);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    responseObserver.onNext(SimpleResponse.newBuilder().setCount(i).setStatusMessage(StatusMessage.newBuilder().setStatus(StatusEnum.COMMENT)).build());
                }
            }
            System.out.println("Server action completed");
            responseObserver.onCompleted();
        });

        return new StreamObserver<SimpleRequest>() {
            @Override
            public void onNext(SimpleRequest simpleRequest) {
                int sent = count.get();
                int received = simpleRequest.getCount();
                if (sent - received > 10) {
                    System.out.println("Pausing emitting");
                    continueProcess.compareAndSet(true, false);
                } else {
                    if (continueProcess.compareAndSet(false, true)) {
                        System.out.println("Unpausing emitting");
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error on server: " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

    }

}
