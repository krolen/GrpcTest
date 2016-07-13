package my.tests.grpc.client;

import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import my.tests.grpc.proto.SimpleResponse;
import my.tests.grpc.proto.TestServiceGrpc;

import java.util.Iterator;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000).build();
        TestServiceGrpc.TestServiceBlockingStub blockingStub = TestServiceGrpc.newBlockingStub(channel);
        TestServiceGrpc.TestServiceStub asyncStub = TestServiceGrpc.newStub(channel);

        final SettableFuture<Void> finishFuture = SettableFuture.create();
        StreamObserver<SimpleResponse> responseObserver = new StreamObserver<SimpleResponse>() {
            @Override
            public void onNext(SimpleResponse value) {
                System.out.println(value.toString());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Exception " + t.getMessage());
                finishFuture.setException(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed");
                finishFuture.set(null);
            }
        };

        asyncStub.myAsyncCall(Empty.getDefaultInstance(), responseObserver);

        Iterator<SimpleResponse> iterator = blockingStub.mySyncCall(Empty.getDefaultInstance());
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString());
        }

    }
}
