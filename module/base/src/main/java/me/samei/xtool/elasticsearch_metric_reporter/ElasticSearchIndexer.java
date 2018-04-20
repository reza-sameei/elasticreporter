package me.samei.xtool.elasticsearch_metric_reporter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchIndexer {

    private final String host;
    private final Logger logger;

    public ElasticSearchIndexer(String host) throws MalformedURLException {

        if (host == null) throw new IllegalArgumentException("'host' can't be null");
        if (host.isEmpty()) throw new IllegalArgumentException("'host' can't be empty");

        logger = LoggerFactory.getLogger(getClass());

        new URL(host);

        this.host = host;
    }

    public void put(String index, long time, String json) throws IOException {

        String url = genURL(index, time);

        if (logger.isTraceEnabled()) logger.trace("PUT, Index: {}, Time: {}, GeneratedURL: {}", index, time, url);

        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();

        http.setUseCaches(false);
        http.setDoOutput(true);
        http.setRequestMethod("PUT");
        http.setRequestProperty("Content-Type", "application/json");

        OutputStream output = new DataOutputStream(http.getOutputStream());
        byte[] bytes = json.getBytes();

        if (logger.isTraceEnabled()) logger.trace("PUT, RequestBodyLen: {}, RequestBody: {}", bytes.length, json);

        output.write(bytes);

        output.flush();
        output.close();

        http.connect();

        if (logger.isTraceEnabled()) logger.trace(
                "PUT, Response({}): {}, ContentLen: {}, ContentType: {}",
                http.getResponseCode(), http.getResponseMessage(),
                http.getContentLength(), http.getContentType()
        );

        int statusCode = http.getResponseCode();
        String body = null;
        boolean successful = false;

        if (statusCode == 200 || statusCode == 201) {
            body = readStream(http.getInputStream());
            successful = true;
        } else {
            body = readStream(http.getErrorStream());
        }

        if (logger.isTraceEnabled()) logger.trace("PUT, ResponseBody: {}", body);

        if (successful) {
            if (logger.isDebugEnabled()) logger.debug(
                    "PUT, Index: {}, Time: {}, URL: {}, RequestBodyLen: {}, Response({}): {}, ResponseBodyLen: {}",
                    index, time, url,
                    bytes.length,
                    http.getResponseCode(), http.getResponseMessage(),
                    body.getBytes().length
            );
        } else {
            if (logger.isWarnEnabled()) logger.warn(
                    "PUT, Index: {}, Time: {}, URL: {}, RequestBodyLen: {}, Response({}): {}, ResponseBodyLen: {}, ResponseBody: {}",
                    index, time, url,
                    bytes.length,
                    http.getResponseCode(), http.getResponseMessage(),
                    body.getBytes().length,
                    body
            );
        }
    }

    private String genURL(String index, long time) {
        return new StringBuilder()
                .append(this.host)
                .append("/")
                .append(index)
                .append("/doc/")
                .append(Long.toString(time))
                .toString();
    }

    private String readStream(InputStream input) throws IOException {
        BufferedReader buffer =
                new BufferedReader(new InputStreamReader(input));

        StringBuilder str = new StringBuilder();
        String line = null;
        while ((line = buffer.readLine()) != null) {
            str.append(line);
        }
        return str.toString();
    }

    private String _toString;

    @Override
    public String toString() {
        if (_toString == null) {
            _toString = new StringBuilder()
                    .append(getClass().getName())
                    .append("(host: '")
                    .append(host)
                    .append("')")
                    .toString();

        }
        return _toString;
    }
}

