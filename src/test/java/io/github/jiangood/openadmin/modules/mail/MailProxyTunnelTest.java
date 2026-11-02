package io.github.jiangood.openadmin.modules.mail;

import cn.hutool.extra.mail.JakartaMail;
import cn.hutool.extra.mail.MailAccount;
import io.github.jiangood.openadmin.framework.proxy.ProxySettings;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用本地的假代理 + 假 SMTP 验证 {@link MailProxyProperties} 与 Hutool JakartaMail
 * 真的能走 HTTP CONNECT 隧道，不依赖外网、也不依赖真实邮箱账号。
 */
class MailProxyTunnelTest {

    @Test
    void mailIsSentThroughHttpConnectProxy() throws Exception {
        try (FakeSmtp smtp = new FakeSmtp(); FakeProxy proxy = new FakeProxy()) {
            smtp.start();
            proxy.start();

            ProxySettings settings = new ProxySettings(
                    ProxySettings.Type.HTTP, "127.0.0.1", proxy.port(), "user", "pass");

            MailAccount account = new MailAccount()
                    .setHost("127.0.0.1")
                    .setPort(smtp.port())
                    .setAuth(false)
                    .setSslEnable(false)
                    .setStarttlsEnable(false)
                    .setFrom("sender@example.com")
                    .setCharset(StandardCharsets.UTF_8);
            MailProxyProperties.apply(account, settings);

            JakartaMail.create(account)
                    .setTos("receiver@example.com")
                    .setTitle("proxy test")
                    .setContent("hello through proxy", false)
                    .send();

            smtp.await();
            proxy.await();
            assertNull(proxy.error.get(), "代理处理异常：" + proxy.error.get());
            assertNull(smtp.error.get(), "SMTP 处理异常：" + smtp.error.get());

            // 走没走代理：看代理收到的 CONNECT 目标
            assertEquals("127.0.0.1:" + smtp.port(), proxy.connectTarget.get());
            // Basic 认证头：user:pass
            assertEquals("Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)),
                    proxy.authorization.get());
            // 邮件正文确实经隧道送到了 SMTP
            assertTrue(smtp.message.get().contains("Subject: proxy test"), smtp.message.get());
            assertTrue(smtp.message.get().contains("hello through proxy"), smtp.message.get());
        }
    }

    // ------------------------------------------------------------------ 假服务

    /** 只接受一次连接、只处理一次会话的假 SMTP，DATA 内容存到 {@link #message}。 */
    static final class FakeSmtp implements AutoCloseable {

        private final ServerSocket server;
        final AtomicReference<String> message = new AtomicReference<>("");
        final AtomicReference<Exception> error = new AtomicReference<>();
        private final Thread thread;

        FakeSmtp() throws IOException {
            this.server = new ServerSocket(0);
            this.thread = new Thread(this::serve, "fake-smtp");
            this.thread.setDaemon(true);
        }

        int port() {
            return server.getLocalPort();
        }

        void start() {
            thread.start();
        }

        void await() throws InterruptedException {
            thread.join(10_000);
        }

        private void serve() {
            try (ServerSocket ignored = server; Socket socket = server.accept()) {
                socket.setSoTimeout(10_000);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                writeLine(out, "220 fake.smtp ESMTP");
                String line;
                while ((line = in.readLine()) != null) {
                    String upper = line.toUpperCase(Locale.ROOT);
                    if (upper.startsWith("EHLO") || upper.startsWith("HELO")) {
                        out.write("250-fake.smtp\r\n250-8BITMIME\r\n250 OK\r\n");
                        out.flush();
                    } else if (upper.startsWith("DATA")) {
                        writeLine(out, "354 End data with <CR><LF>.<CR><LF>");
                        StringBuilder body = new StringBuilder();
                        String dataLine;
                        while ((dataLine = in.readLine()) != null && !".".equals(dataLine)) {
                            body.append(dataLine).append('\n');
                        }
                        message.set(body.toString());
                        writeLine(out, "250 OK queued");
                    } else if (upper.startsWith("QUIT")) {
                        writeLine(out, "221 Bye");
                        break;
                    } else {
                        writeLine(out, "250 OK");
                    }
                }
            } catch (Exception e) {
                error.set(e);
            }
        }

        @Override
        public void close() throws IOException {
            server.close();
        }
    }

    /** 只接受一次连接、只处理一个 CONNECT 的假 HTTP 代理，记录目标与认证头。 */
    static final class FakeProxy implements AutoCloseable {

        private final ServerSocket server;
        final AtomicReference<String> connectTarget = new AtomicReference<>();
        final AtomicReference<String> authorization = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        private final Thread thread;

        FakeProxy() throws IOException {
            this.server = new ServerSocket(0);
            this.thread = new Thread(this::serve, "fake-proxy");
            this.thread.setDaemon(true);
        }

        int port() {
            return server.getLocalPort();
        }

        void start() {
            thread.start();
        }

        void await() throws InterruptedException {
            thread.join(10_000);
        }

        private void serve() {
            try (ServerSocket ignored = server; Socket client = server.accept()) {
                client.setSoTimeout(10_000);
                InputStream in = client.getInputStream();
                String requestLine = readLine(in);
                String[] parts = requestLine.split(" ");
                assertTrue(parts[0].equals("CONNECT"), "代理收到非 CONNECT 请求：" + requestLine);
                connectTarget.set(parts[1]);

                String header;
                while (!(header = readLine(in)).isEmpty()) {
                    if (header.toLowerCase(Locale.ROOT).startsWith("proxy-authorization:")) {
                        authorization.set(header.substring(header.indexOf(':') + 1).trim());
                    }
                }

                OutputStream out = client.getOutputStream();
                out.write("HTTP/1.1 200 Connection established\r\n\r\n"
                        .getBytes(StandardCharsets.ISO_8859_1));
                out.flush();

                String target = connectTarget.get();
                int colon = target.lastIndexOf(':');
                try (Socket upstream = new Socket(target.substring(0, colon),
                        Integer.parseInt(target.substring(colon + 1)))) {
                    Thread c2s = new Thread(() -> copy(client, upstream), "proxy-c2s");
                    c2s.setDaemon(true);
                    c2s.start();
                    copy(upstream, client);
                    c2s.join(5_000);
                }
            } catch (Exception e) {
                error.set(e);
            }
        }

        @Override
        public void close() throws IOException {
            server.close();
        }
    }

    private static void copy(Socket from, Socket to) {
        byte[] buffer = new byte[4096];
        try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (IOException ignored) {
            // 任一端关闭即结束
        }
    }

    /** 按行读，只读需要的那几行（避免 BufferedReader 预读掉隧道里的字节）。 */
    private static String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                int len = line.length();
                if (len > 0 && line.charAt(len - 1) == '\r') {
                    line.setLength(len - 1);
                }
                return line.toString();
            }
            line.append((char) b);
        }
        return line.toString();
    }

    private static void writeLine(Writer out, String line) throws IOException {
        out.write(line);
        out.write("\r\n");
        out.flush();
    }
}
