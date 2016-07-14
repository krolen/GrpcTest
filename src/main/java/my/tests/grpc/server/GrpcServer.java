package my.tests.grpc.server;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import my.tests.grpc.proto.TestServiceGrpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(5000).addService(TestServiceGrpc.bindService(new TestServiceImpl())).build();
        server.start();
        System.out.println("Server started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may has been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                server.shutdown();
                System.err.println("*** server shut down");
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
                server.shutdownNow();
            }
        });
        server.awaitTermination();
    }

}
