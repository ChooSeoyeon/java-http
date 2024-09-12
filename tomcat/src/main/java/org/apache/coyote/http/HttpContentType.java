package org.apache.coyote.http;

import java.util.stream.Stream;

public enum HttpContentType {

    HTML("text/html;charset=utf-8", ".html"), // TODO: charset 별도로 처리
    CSS("text/css;charset=utf-8", ".css"),
    JAVASCRIPT("application/javascript", ".js"),
    ICO("image/x-icon", ".ico");

    private final String mimeType;
    private final String extension;

    HttpContentType(final String mimeType, final String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public static HttpContentType findByExtension(final String extension) {
        return Stream.of(HttpContentType.values())
                .filter(type -> extension.endsWith(type.getExtension()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ContentType을 지원하지 않는 확장자입니다: " + extension));
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
