package com.example.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.grpcdemo.grpc.HelloResponse;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@SpringBootApplication
@RestController
public class Demo2Application {

	@PostMapping(path = "/hello")
	public RestResponse hello(@RequestBody RestRequest request) {
		String greeting = new StringBuilder()
				.append("Hello, ")
				.append(request.firstName)
				.append(" ")
				.append(request.lastName)
				.toString();

		RestResponse response = new RestResponse();
		response.greeting = greeting ;
		return response ;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(Demo2Application.class, args);
		Server server = ServerBuilder
		          .forPort(8081)
		          .addService(new HelloServiceImpl()).build();

		        server.start();
		        server.awaitTermination();
	}

}