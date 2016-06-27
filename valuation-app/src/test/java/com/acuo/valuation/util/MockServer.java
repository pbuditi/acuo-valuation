package com.acuo.valuation.util;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class MockServer implements Runnable {

    private final MockWebServer mockWebServer;
    private final CountDownLatch latch;

    private final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            try {
                final String body = request.getBody().readUtf8();
                if (body.contains("multipart/form-data")) {
                    return new MockResponse().setResponseCode(200).setBody("key");
                }
                if (body.contains("key=key")) {
                    return new MockResponse().setResponseCode(200).setBody(file("/reports/markit-test-01.xml"));
                }
                if (body.contains("asof")) {
                    return new MockResponse().setResponseCode(200).setBody(file("/responses/markit-test-01.xml"));
                }
                return new MockResponse().setResponseCode(404);
            } catch (Exception e) {
                return new MockResponse().setResponseCode(500);
            }
        }
    };

    private MockServer() {
        this.mockWebServer = new MockWebServer();
        this.latch = new CountDownLatch(1);
        mockWebServer.setDispatcher(dispatcher);
    }

    @Override
    public void run() {
        try {
            mockWebServer.start(8080);
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private String file(String resourceName) {
        try {
            Path path = Paths.get(getClass().getResource(resourceName).toURI());
            return new String(Files.readAllBytes(path));
        } catch (URISyntaxException | IOException e) {
            return "";
        }
    }

    public static void main(String[] args) {
        MockServer server = new MockServer();
        new Thread(server).start();
        System.out.println("Press ENTER to stop the server");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        server.stop();
    }
}
