package com.rmd.personal.rentchecker.common;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Connector {

    private static final String DEFAULT_URL_WITH_RETURN_URL
            = "https://my1.equityapartments.com/Login.aspx?ReturnUrl=~/Default.aspx";
    private static final String DEFAULT_URL_WITHOUT_RETURN_URL = "https://my1.equityapartments.com/Default.aspx";

    private RestTemplate restTemplate;

    public Connector() {
        this(new RestTemplate());
    }

    protected Connector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public HttpEntity<?> createInitialHttpEntity(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username must be non-null and non-empty");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password must be non-null and non-empty");
        }

        HttpHeaders requestHeaders = new HttpHeaders();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("__LASTFOCUS", "");
        body.add("__EVENTTARGET", "");
        body.add("__EVENTARGUMENT", "");
        body.add("__VIEWSTATE", "/wEPDwUKLTY0NzI5NjE3Mg9kFgRmDxYCHgRocmVmBRRsaWJyYXJ5L0NTUy9pcmVzLmNzc2QCAw9kFgpmDxYCHgVjbGFzc2UWAmYPDxYEHghDc3NDbGFzcwUSY29udEVycm9yQ2xpZW50TmV3HgRfIVNCAgJkZAIDDxYCHgdWaXNpYmxlaBYCAgEPEGRkFgBkAgQPFgIeBXZhbHVlBQdTaWduIEluZAIFDxYCHwRoZAIGDxYCHgVzdHlsZQUXV0lEVEg6MTAwJTtIRUlHSFQ6MzBweDtkZDR7J1JBtEUkI9f4Dj4UvmwnBvtS"); // SUPPRESS CHECKSTYLE lineLength
        body.add("m_txtUsername", username);
        body.add("m_txtPassword", password);
        body.add("m_btnLogin", "Sign in");

        return new HttpEntity<>(body, requestHeaders);
    }

    public ResponseEntity<String> performLogin(HttpEntity<?> entity) {
        ResponseEntity<String> pageEntity;

        // We may get one "connection reset by peer" but never more than one.
        try {
            pageEntity = this.getRestTemplate().postForEntity(
                    DEFAULT_URL_WITH_RETURN_URL,
                    entity,
                    String.class);
        } catch (ResourceAccessException e) {
            pageEntity = this.getRestTemplate().postForEntity(
                    DEFAULT_URL_WITH_RETURN_URL,
                    entity,
                    String.class);
        }

        return pageEntity;
    }

    public HttpEntity<?> addCookiesToPageEntityWithCopyOfHttpHeaders(ResponseEntity<String> pageEntity,
                                                                     HttpHeaders httpHeaders) {
        List<String> cookies = pageEntity.getHeaders().get("Set-Cookie");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cookies.size(); i++) {
            stringBuilder.append(cookies.get(i));
            if (i < cookies.size() - 1) {
                stringBuilder.append("; ");
            }
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.putAll(httpHeaders);

        requestHeaders.add("Cookie", stringBuilder.toString());
        return new HttpEntity<Object>(requestHeaders);
    }

    public HttpHeaders addRefererToCopyOfHttpHeaders(HttpHeaders headers) {
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.putAll(headers);
        newHeaders.add("Referer", "https://my1.equityapartments.com/Login.aspx?ReturnUrl=~/Default.aspx");
        return newHeaders;
    }

    public ResponseEntity<String> getHomepage(HttpEntity<?> entity) {
        ResponseEntity<String> pageEntity;

        // We may get one "connection reset by peer" but never more than one.
        try {
            pageEntity = this.getRestTemplate().exchange(
                    DEFAULT_URL_WITHOUT_RETURN_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);
        } catch (ResourceAccessException e) {
            pageEntity = this.getRestTemplate().exchange(
                    DEFAULT_URL_WITHOUT_RETURN_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);
        }

        return pageEntity;
    }

    public float scrapePageEntityForRent(ResponseEntity<String> pageEntity) {
        String htmlBody = pageEntity.getBody();

        Pattern negativePattern = Pattern.compile(">-\\$(.*?)<");
        Matcher negativeMatcher = negativePattern.matcher(htmlBody);

        String resultVal;

        if (negativeMatcher.find()) {
            resultVal = "-" + negativeMatcher.group(1);
        } else {
            Pattern positivePattern = Pattern.compile(">\\$(.*?)<");
            Matcher positiveMatcher = positivePattern.matcher(htmlBody);
            boolean found = positiveMatcher.find();

            if (!found) {
                throw new IllegalStateException("HTML does not contain amount owed.");
            }
            resultVal = positiveMatcher.group(1);
        }

        resultVal = resultVal.replace(",", "");

        return Float.valueOf(resultVal);
    }
}
