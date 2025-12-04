package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;        // только путь без query, напр. "/messages"
    private final String query;       // строка query, напр. "last=10&user=vasya" (может быть null)
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, String query) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.queryParams = query != null
                ? URLEncodedUtils.parse(query, StandardCharsets.UTF_8)
                : List.of();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.stream()
                .filter(param -> param.getName().equals(name))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    public List<String> getQueryParams(String name) {
        return queryParams.stream()
                .filter(param -> param.getName().equals(name))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", query='" + query + '\'' +
                ", queryParams=" + queryParams +
                '}';
    }
}