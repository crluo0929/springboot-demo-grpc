package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.grpcdemo.grpc.HelloRequest;
import com.example.grpcdemo.grpc.HelloResponse;
import com.example.grpcdemo.grpc.HelloServiceGrpc.HelloServiceImplBase;

import io.grpc.stub.StreamObserver;

@Component
public class HelloServiceImpl extends HelloServiceImplBase{

	@Autowired MyService myService ;
	
	@Override
    public void hello(
      HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		
		System.out.println(myService);
		
        String greeting = new StringBuilder()
          .append("Hello, ")
          .append(request.getFirstName())
          .append(" ")
          .append(request.getLastName())
          .toString();

        HelloResponse response = HelloResponse.newBuilder()
          .setGreeting(greeting)
          .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
	
}
