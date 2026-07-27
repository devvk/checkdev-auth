package ru.checkdev.auth.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AccessKeyFilterTest {

    private AccessKeyFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new AccessKeyFilter();
        filterChain = mock(FilterChain.class);
        ReflectionTestUtils.setField(filter, "accessKey", "96GcWB8a");
    }

    @Test
    void whenProtectedUrlWithoutAccessKeyThenForbidden() throws Exception {
        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/profiles/tg/1");
        request.setServletPath("/profiles/tg/1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenProtectedUrlWithWrongAccessKeyThenForbidden() throws Exception {
        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/profiles/tg/1");
        request.setServletPath("/profiles/tg/1");
        request.addHeader("X-Access-Key", "wrong-key");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenProtectedUrlWithCorrectAccessKeyThenRequestContinues() throws Exception {
        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/profiles/tg/1");
        request.setServletPath("/profiles/tg/1");
        request.addHeader("X-Access-Key", "96GcWB8a");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenUnprotectedUrlWithoutAccessKeyThenRequestContinues() throws Exception {
        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/profiles/1");
        request.setServletPath("/profiles/1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }
}
