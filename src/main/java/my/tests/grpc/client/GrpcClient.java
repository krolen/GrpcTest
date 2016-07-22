package my.tests.grpc.client;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import my.tests.grpc.proto.SimpleRequest;
import my.tests.grpc.proto.SimpleResponse;
import my.tests.grpc.proto.TestServiceGrpc;
import my.tests.grpc.server.GrpcServer;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 5000).usePlaintext(true).build();
//        blockingCall(channel);
        asyncCall(channel);
    }

    private static void blockingCall(ManagedChannel channel) {
        TestServiceGrpc.TestServiceBlockingStub blockingStub = TestServiceGrpc.newBlockingStub(channel);
        AtomicInteger count = new AtomicInteger(0);
        Iterator<SimpleResponse> iterator = blockingStub.mySyncCall(Empty.getDefaultInstance());
        while (iterator.hasNext()) {
            SimpleResponse next = iterator.next();
            int i = count.incrementAndGet();
            if (i % 5000 == 0) {
                System.out.println("Consumed " + i + " waiting...");
                Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            }
        }

        System.out.println("Total consumed: " + count.get());
    }

    private static void asyncCall(ManagedChannel channel) {
        TestServiceGrpc.TestServiceStub asyncStub = TestServiceGrpc.newStub(channel);
        AtomicInteger count = new AtomicInteger(0);
        final SettableFuture<Void> finishFuture = SettableFuture.create();

        StreamObserver<SimpleResponse> responseObserver = new StreamObserver<SimpleResponse>() {
            @Override
            public void onNext(SimpleResponse value) {
                count.incrementAndGet();
                GrpcServer.printMsg("Got response " + value.getCount());
                Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            }

            @Override
            public void onError(Throwable t) {
                finishFuture.setException(t);
            }

            @Override
            public void onCompleted() {
                finishFuture.set(null);
            }
        };

        StreamObserver<SimpleRequest> requestObserver = asyncStub.myAsyncCall(responseObserver);

//        requestObserver.onNext(SimpleRequest.newBuilder().setCount(count.get()).build());

        AtomicBoolean continueProcess = new AtomicBoolean(true);
        while (continueProcess.get()) {
            try {
                finishFuture.get(4, TimeUnit.SECONDS);
                continueProcess.set(false);
                GrpcServer.printMsg("Client action completed");
                channel.shutdown();
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Throwables.propagate(e);
            } catch (ExecutionException e) {
                Throwables.propagate(e);
            } catch (TimeoutException ignored) {
                int value = count.get();
                GrpcServer.printMsg("Emitting request entry " + value);
                requestObserver.onNext(SimpleRequest.newBuilder().setCount(value).build());
            }
        }

        System.out.println("Total consumed: " + count.get());
    }
}
