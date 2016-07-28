package my.tests.grpc.client;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import my.tests.grpc.proto.SimpleRequest;
import my.tests.grpc.proto.SimpleResponse;
import my.tests.grpc.proto.TestServiceGrpc;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class QueueGrpcClient {

    private final ManagedChannel channel;
    private final ChronicleQueue queue;

    public QueueGrpcClient() {
        channel = ManagedChannelBuilder.forAddress("127.0.0.1", 5000).usePlaintext(true).build();
        queue = SingleChronicleQueueBuilder.binary("/trades").build();
    }

    public static void main(String[] args) {
        QueueGrpcClient client = new QueueGrpcClient();
        try {
            client.asyncCall();
        } finally {
            client.close();
        }
    }

    private void close() {
        Optional.ofNullable(queue).ifPresent(ChronicleQueue::close);
        channel.shutdown();
        try {
            boolean terminated = channel.awaitTermination(20, TimeUnit.SECONDS);
            if (terminated) {
                System.out.println("Client terminated normally");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            channel.shutdownNow();
        }
    }

    private void asyncCall() {
        TestServiceGrpc.TestServiceStub asyncStub = TestServiceGrpc.newStub(channel);
//        .withInterceptors(new ClientInterceptor() {
//            @Override
//            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
//                                                                       CallOptions callOptions, Channel next) {
//                return null;
//            }
//        });

        AtomicBoolean continueProcess = new AtomicBoolean(true);
//        final SettableFuture<Object> finishFuture = SettableFuture.create();

        StreamObserver<SimpleResponse> responseObserver = new StreamObserver<SimpleResponse>() {
            @Override
            public void onNext(SimpleResponse value) {
                ExcerptAppender appender = queue.acquireAppender();
                appender.writeBytes(b -> {
                    byte[] bytes = value.toByteArray();
                    b.write(bytes);
                });
            }

            @Override
            public void onError(Throwable t) {
//                finishFuture.setException(t);
                continueProcess.compareAndSet(true, false);
            }

            @Override
            public void onCompleted() {
//                finishFuture.set(null);
                System.out.println("Server streaming completed");
                continueProcess.compareAndSet(true, false);
            }
        };

        StreamObserver<SimpleRequest> requestObserver = asyncStub.myAsyncCall(responseObserver);
        requestObserver.onNext(SimpleRequest.newBuilder().setCount(1).build());

        ExcerptTailer tailer = queue.createTailer();
        boolean readSmth = false;
        while (continueProcess.get() || readSmth) {
            readSmth = tailer.readBytes(bytes -> {
                byte[] byteArray = bytes.bytesForRead().toByteArray();
                try {
                    SimpleResponse response = SimpleResponse.parseFrom(byteArray);
                    doSmth(response);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void doSmth(SimpleResponse simpleResponse) {
        System.out.println("Read: " + simpleResponse.getCount());
    }

}
