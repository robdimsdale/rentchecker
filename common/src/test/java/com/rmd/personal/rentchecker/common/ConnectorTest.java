package com.rmd.personal.rentchecker.common;

import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorTest {

    private static final float FLOAT_DELTA = 0.001f;

    @InjectMocks
    private Connector connector;

    private Connector getConnector() {
        return connector;
    }

    @Test
    public void noArgConstructorReturnsOk() {
        // Act & Assert
        assertNotNull(new Connector());
    }

    @Test
    public void createInitialHttpEntityRequiresNonNullUsername() {
        // Arrange
        final String username = null;
        final String password = "somePassword";

        // Act & Assert
        try {
            this.getConnector().createInitialHttpEntity(username, password);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage().toLowerCase(), containsString("username"));
        }
    }

    @Test
    public void createInitialHttpEntityRequiresNonEmptyUsername() {
        // Arrange
        final String username = "";
        final String password = "somePassword";

        // Act & Assert
        try {
            this.getConnector().createInitialHttpEntity(username, password);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage().toLowerCase(), containsString("username"));
        }
    }

    @Test
    public void createInitialHttpEntityRequiresNonNullPassword() {
        // Arrange
        final String username = "someUsername";
        final String password = null;

        // Act & Assert
        try {
            this.getConnector().createInitialHttpEntity(username, password);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage().toLowerCase(), containsString("password"));
        }
    }

    @Test
    public void createInitialHttpEntityRequiresNonEmptyPassword() {
        // Arrange
        final String username = "someUsername";
        final String password = "";

        // Act & Assert
        try {
            this.getConnector().createInitialHttpEntity(username, password);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage().toLowerCase(), containsString("password"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createInitialHttpEntityAddsUsernameAndPasswordToBody() {
        // Arrange
        final String username = "someUsername";
        final String password = "somePassword";

        // Act
        final HttpEntity<?> result = this.getConnector().createInitialHttpEntity(username, password);

        // Assert
        assertThat(result.getBody(), is(notNullValue()));
        MultiValueMap<String, String> bodyAsMap = (LinkedMultiValueMap<String, String>) result.getBody();
        Map<String, String> singleValueBody = bodyAsMap.toSingleValueMap();
        assertTrue(singleValueBody.containsValue(username));
        assertTrue(singleValueBody.containsValue(password));
    }

    @Test
    public void performLoginReturnsCorrectlyIfRestTemplateReturnsFirstTime() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        ResponseEntity<String> expectedReturn = new ResponseEntity<>(HttpStatus.OK);
        Connector anotherConnector = new Connector(mockRestTemplate);
        doReturn(expectedReturn)
                .when(mockRestTemplate).postForEntity(
                anyString(),
                eq(entity),
                eq(String.class));

        // Act
        final ResponseEntity<String> returned = anotherConnector.performLogin(entity);

        // Assert
        assertThat(returned, is(expectedReturn));
    }

    @Test
    public void performLoginReturnsCorrectlyIfRestTemplateThrowsResourceAccessExceptionOnce() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        ResponseEntity<String> expectedReturn = new ResponseEntity<>(HttpStatus.OK);
        Connector anotherConnector = new Connector(mockRestTemplate);
        doThrow(new ResourceAccessException("myResouceFailed")).doReturn(expectedReturn)
                .when(mockRestTemplate).postForEntity(
                anyString(),
                eq(entity),
                eq(String.class));

        // Act
        final ResponseEntity<String> returned = anotherConnector.performLogin(entity);

        // Assert
        assertThat(returned, is(expectedReturn));
    }

    @Test
    public void performLoginReturnsCorrectlyIfRestTemplateThrowsResourceAccessExceptionTwice() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        Connector anotherConnector = new Connector(mockRestTemplate);
        doThrow(new ResourceAccessException("myResouceFailed1"))
                .doThrow(new ResourceAccessException("myResouceFailed2"))
                .when(mockRestTemplate).postForEntity(
                anyString(),
                eq(entity),
                eq(String.class));

        // Act
        try {
            anotherConnector.performLogin(entity);
            fail();
        } catch (ResourceAccessException e) {
            assertThat(e.getMessage(), StringContains.containsString("myResouceFailed2"));
        }
    }

    @Test
    public void addCookiesToPageEntityWithCopyOfHttpHeadersAddsExistingHeaders() {
        // Arrange
        MultiValueMap<String, String> dummyEntityHeaders = new LinkedMultiValueMap<>();
        dummyEntityHeaders.put("Set-Cookie", new ArrayList<String>());
        ResponseEntity<String> dummyPageEntity = new ResponseEntity<>(dummyEntityHeaders, HttpStatus.OK);

        HttpHeaders dummyHeaders = new HttpHeaders();
        List<String> dummyHeader1Values = new ArrayList<>();
        dummyHeader1Values.add("dummyHeader1Value");
        dummyHeaders.put("dummyHeader1", dummyHeader1Values);

        // Act
        final HttpEntity<?> returned
                = this.getConnector().addCookiesToPageEntityWithCopyOfHttpHeaders(dummyPageEntity, dummyHeaders);

        // Assert
        assertThat(returned.getHeaders().get("dummyHeader1"), is(dummyHeader1Values));
    }

    @Test
    public void addCookiesToPageEntityWithCopyOfHttpHeadersAddsSingleCookie() {
        // Arrange
        MultiValueMap<String, String> dummyEntityHeaders = new LinkedMultiValueMap<>();
        List<String> dummyEntityHeader1Values = new ArrayList<>();
        dummyEntityHeader1Values.add("cookieValue");
        dummyEntityHeaders.put("Set-Cookie", dummyEntityHeader1Values);
        ResponseEntity<String> dummyPageEntity = new ResponseEntity<>(dummyEntityHeaders, HttpStatus.OK);

        HttpHeaders dummyHeaders = new HttpHeaders();

        // Act
        final HttpEntity<?> returned
                = this.getConnector().addCookiesToPageEntityWithCopyOfHttpHeaders(dummyPageEntity, dummyHeaders);

        // Assert
        assertThat(returned.getHeaders().get("Cookie"), is(dummyEntityHeader1Values));
    }

    @Test
    public void addCookiesToPageEntityWithCopyOfHttpHeadersAddsMultipleCookies() {
        // Arrange
        MultiValueMap<String, String> dummyEntityHeaders = new LinkedMultiValueMap<>();
        List<String> dummyEntityHeader1Values = new ArrayList<>();
        dummyEntityHeader1Values.add("cookieValue1");
        dummyEntityHeader1Values.add("cookieValue2");
        dummyEntityHeaders.put("Set-Cookie", dummyEntityHeader1Values);
        ResponseEntity<String> dummyPageEntity = new ResponseEntity<>(dummyEntityHeaders, HttpStatus.OK);

        HttpHeaders dummyHeaders = new HttpHeaders();

        // Act
        final HttpEntity<?> returned
                = this.getConnector().addCookiesToPageEntityWithCopyOfHttpHeaders(dummyPageEntity, dummyHeaders);

        // Assert
        final List<String> expectedCookies = new ArrayList<>();
        expectedCookies.add("cookieValue1; cookieValue2");
        assertThat(returned.getHeaders().get("Cookie"), is(expectedCookies));
    }

    @Test
    public void addRefererToCopyOfHttpHeadersRetainsOriginal() {
        // Arrange
        HttpHeaders dummyHeaders = new HttpHeaders();
        List<String> dummyHeader1Values = new ArrayList<>();
        dummyHeader1Values.add("dummyHeader1Value");
        dummyHeaders.put("dummyHeader1", dummyHeader1Values);

        // Act
        final HttpHeaders returned = this.getConnector().addRefererToCopyOfHttpHeaders(dummyHeaders);

        // Assert
        assertThat(returned.get("dummyHeader1"), is(dummyHeader1Values));
        assertTrue(returned.containsKey("Referer"));
    }

    @Test
    public void getHomepageReturnsCorrectlyIfRestTemplateReturnsFirstTime() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        ResponseEntity<String> expectedReturn = new ResponseEntity<>(HttpStatus.OK);
        Connector anotherConnector = new Connector(mockRestTemplate);
        doReturn(expectedReturn)
                .when(mockRestTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(entity),
                eq(String.class));

        // Act
        final ResponseEntity<String> returned = anotherConnector.getHomepage(entity);

        // Assert
        assertThat(returned, is(expectedReturn));
    }

    @Test
    public void getHomepageReturnsCorrectlyIfRestTemplateThrowsResourceAccessExceptionOnce() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        ResponseEntity<String> expectedReturn = new ResponseEntity<>(HttpStatus.OK);
        Connector anotherConnector = new Connector(mockRestTemplate);
        doThrow(new ResourceAccessException("myResouceFailed")).doReturn(expectedReturn)
                .when(mockRestTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(entity),
                eq(String.class));

        // Act
        final ResponseEntity<String> returned = anotherConnector.getHomepage(entity);

        // Assert
        assertThat(returned, is(expectedReturn));
    }

    @Test
    public void getHomepageReturnsCorrectlyIfRestTemplateThrowsResourceAccessExceptionTwice() {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        HttpEntity<?> entity = new HttpEntity<>("body");
        Connector anotherConnector = new Connector(mockRestTemplate);
        doThrow(new ResourceAccessException("myResouceFailed1"))
                .doThrow(new ResourceAccessException("myResouceFailed2"))
                .when(mockRestTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(entity),
                eq(String.class));

        // Act
        try {
            anotherConnector.getHomepage(entity);
            fail();
        } catch (ResourceAccessException e) {
            assertThat(e.getMessage(), StringContains.containsString("myResouceFailed2"));
        }
    }

    @Test
    public void scrapePageEntityForRentThrowsExceptionIfHtmlNotFound() {
        // Arrange
        final String body = "";
        ResponseEntity<String> dummyResponse = new ResponseEntity<>(body, HttpStatus.OK);

        // Act & Assert
        try {
            this.getConnector().scrapePageEntityForRent(dummyResponse);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage().toLowerCase(), StringContains.containsString("html"));
        }
    }

    @Test
    public void scrapePageEntityForRentReturnsFoundNegativeValue() {
        // Arrange
        final String body = ">-$123.45<";
        ResponseEntity<String> dummyResponse = new ResponseEntity<>(body, HttpStatus.OK);

        // Act
        float returned = this.getConnector().scrapePageEntityForRent(dummyResponse);

        // Assert
        final float expected = -123.45f;
        assertEquals(expected, returned, FLOAT_DELTA);
    }

    @Test
    public void scrapePageEntityForRentReturnsFoundPositiveValue() {
        // Arrange
        final String body = ">$123.45<";
        ResponseEntity<String> dummyResponse = new ResponseEntity<>(body, HttpStatus.OK);

        // Act
        float returned = this.getConnector().scrapePageEntityForRent(dummyResponse);

        // Assert
        final float expected = 123.45f;
        assertEquals(expected, returned, FLOAT_DELTA);
    }

    @Test
    public void scrapePageEntityHandlesCommas() {
        // Arrange
        final String body = ">$9,123.45<";
        ResponseEntity<String> dummyResponse = new ResponseEntity<>(body, HttpStatus.OK);

        // Act
        float returned = this.getConnector().scrapePageEntityForRent(dummyResponse);

        // Assert
        final float expected = 9123.45f;
        assertEquals(expected, returned, FLOAT_DELTA);
    }
}
