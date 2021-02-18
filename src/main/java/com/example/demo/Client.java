package com.example.demo;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.example.grpcdemo.grpc.HelloRequest;
import com.example.grpcdemo.grpc.HelloResponse;
import com.example.grpcdemo.grpc.HelloServiceGrpc;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {

	public static void main(String[] args) {
//		FileAppender<ILoggingEvent> myAppender = new FileAppender<ILoggingEvent>();
//		LoggerContext loggerContext=(LoggerContext)LoggerFactory.getILoggerFactory();
//		loggerContext.reset();
		
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
				.usePlaintext()
				.build();

		HelloServiceGrpc.HelloServiceBlockingStub stub 
		= HelloServiceGrpc.newBlockingStub(channel);
		HelloRequest request = HelloRequest.newBuilder().setFirstName("hihi").setLastName("world").build() ;
		long a = System.currentTimeMillis() ;
		for(int i=0;i<1;++i) {
			HelloResponse resp = stub.hello(request);
		}
		long b = System.currentTimeMillis() ;
		long grpc = b-a ;
		channel.shutdown();

		RestTemplate template = new RestTemplate(clientHttpRequestFactory()) ;
		RestRequest body = new RestRequest() ;
		body.firstName = "hihi" ;
		body.lastName = "world" ;
		HttpHeaders headers = new HttpHeaders() ;
		headers.add("Accept", "*/*");
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Connection", "Keep-Alive");
		HttpEntity<RestRequest> entity = new HttpEntity<>( body, headers) ;
		
		a = System.currentTimeMillis() ;
		for(int i=0;i<1;++i) {
			ResponseEntity<RestResponse> resp = template.exchange("http://localhost:8080/hello", HttpMethod.POST, entity , RestResponse.class) ;
		}
		b = System.currentTimeMillis() ;
		long rest = b-a ;
		
		System.out.println("grpc:" + grpc) ;
		System.out.println("rest:" + rest) ;

	}
	
	private static final int CONNECT_TIMEOUT = 30000;
    // The timeout when requesting a connection from the connection manager.
    private static final int REQUEST_TIMEOUT = 30000;
     
    // The timeout for waiting for data
    private static final int SOCKET_TIMEOUT = 60000;
 
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;
    private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS = 30;
	
	public static PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
        	e.printStackTrace();
//            LOGGER.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }
 
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
        	e.printStackTrace();
//            LOGGER.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }
 
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();
 
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        return poolingConnectionManager;
    }
	
	
	public static ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
 
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return DEFAULT_KEEP_ALIVE_TIME_MILLIS;
            }
        };
    }
	
	public static HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        CloseableHttpClient client = HttpClientBuilder.create()
        		.setConnectionManager(poolingConnectionManager())
        		.setKeepAliveStrategy(connectionKeepAliveStrategy()).build();
        clientHttpRequestFactory.setHttpClient(client);
        return clientHttpRequestFactory;
    }
	
}
