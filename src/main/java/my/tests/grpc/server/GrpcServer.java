package my.tests.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import my.tests.grpc.proto.TestServiceGrpc;

import java.io.IOException;

/**
 * Created by kkulagin on 7/13/2016.
 */
public class GrpcServer {

    public static void main(String[] args) throws IOException {
        Server server = ServerBuilder.forPort(5000).addService(TestServiceGrpc.bindService(new TestServiceImpl())).build();
        server.start();
        System.out.println("Server started");
    }

}
