package org.apache.catalina.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectorTest {

    private Connector connector;
    private final int port = 8081;
    private final int acceptCount = 100;
    private final int corePoolSize = 0;
    private final long keepAliveTime = 60L;
    private final int maxThreads = 5;

    @BeforeEach
    void setUp() {
        connector = new Connector(port, acceptCount, corePoolSize, keepAliveTime, maxThreads);
        connector.start();
    }

    @AfterEach
    void tearDown() {
        connector.stop();
    }

    @Test
    void testThreadPoolCreatesAndReusesThreads() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) connector.getThreadPool();
        assertEquals(0, threadPoolExecutor.getPoolSize()); // 스레드가 없는 상태에서 시작되었는지 확인

        // 병렬로 5개의 스레드가 동시 생성 가능한지 확인
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(this::simulateClientRequest);
            threads.add(thread);
            thread.start();
        }

        // 모든 스레드가 완료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(5, threadPoolExecutor.getPoolSize()); // 최대 스레드 수 도달

        // 추가 요청이 들어오면 스레드가 재사용되는지 확인
        simulateClientRequest();
        assertEquals(5, threadPoolExecutor.getPoolSize());

        // 스레드가 유휴 상태로 돌아가면 다시 0으로 감소하는지 확인 (60초 이내에는 줄어들지 않음)
        assertEquals(5, threadPoolExecutor.getPoolSize());

        // 스레드가 유휴 상태로 충분히 오래 기다리면 스레드가 제거되는지 확인
        TimeUnit.SECONDS.sleep(keepAliveTime + 1);
        assertEquals(0, threadPoolExecutor.getPoolSize());
    }

    private void simulateClientRequest() {
        try (Socket socket = new Socket("localhost", port)) { // 클라이언트 소켓을 시뮬레이션하여 서버에 연결
            TimeUnit.SECONDS.sleep(200); // 클라이언트의 요청을 서버가 처리할 수 있도록 대기 시간 설정
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
