package my.tests.grpc.server;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import my.tests.grpc.proto.SimpleResponse;
import my.tests.grpc.proto.TestServiceGrpc;

import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class TestServiceImpl implements TestServiceGrpc.TestService {

    @Override
    public void mySyncCall(Empty request, StreamObserver<SimpleResponse> responseObserver) {
        for (int i = 0; i < 100; i++) {
            SimpleResponse response = SimpleResponse.newBuilder().
                setStatusMessage(SimpleResponse.StatusMessage.newBuilder().setStatus(SimpleResponse.StatusMessage.StatusEnum.COMMENT).setComment("This is status " + i)).
                setData(SimpleResponse.Data.newBuilder().setIntData(i).setTextData("This is data " + i)).build();
            responseObserver.onNext(response);
            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void myAsyncCall(Empty request, StreamObserver<SimpleResponse> responseObserver) {
//        for (int i = 0; i < 100; i++) {
//            SimpleResponse response = SimpleResponse.newBuilder().setMsg("Async " + i).setStatus(MyEnums.Status.ERROR).build();
//            responseObserver.onNext(response);
//            Uninterruptibles.sleepUninterruptibly(7, TimeUnit.SECONDS);
//        }
        responseObserver.onCompleted();

    }
}
