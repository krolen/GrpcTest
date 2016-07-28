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
public class SimpleTestService extends TestServiceGrpc.TestServiceImplBase {

    @Override
    public void mySyncCall(Empty request, StreamObserver<SimpleResponse> responseObserver) {
    }

    @Override
    public StreamObserver<SimpleRequest> myAsyncCall(StreamObserver<SimpleResponse> responseObserver) {

        return new StreamObserver<SimpleRequest>() {
            @Override
            public void onNext(SimpleRequest simpleRequest) {
                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
                System.out.println("yoyoyo" + simpleRequest.getCount());
                SimpleResponse.Builder builder = SimpleResponse.newBuilder();
                for (int i = 1; i < 15; i++) {
                    responseObserver.onNext(builder.setCount(i).build());
                    builder.clear();
                    if(i == 1) {
                        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
                    }
                }
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                GrpcServer.printMsg("Error on client: " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                GrpcServer.printMsg("Client completed execution");
                responseObserver.onCompleted();
            }
        };

    }

}
