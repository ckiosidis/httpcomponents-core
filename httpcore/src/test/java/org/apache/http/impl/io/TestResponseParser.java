/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.io;

import java.io.InterruptedIOException;

import tink.org.apache.http.Consts;
import tink.org.apache.http.Header;
import tink.org.apache.http.HttpResponse;
import tink.org.apache.http.HttpVersion;
import tink.org.apache.http.NoHttpResponseException;
import tink.org.apache.http.StatusLine;
import org.apache.http.impl.SessionInputBufferMock;
import tink.org.apache.http.impl.io.DefaultHttpResponseParser;import tink.org.apache.http.io.SessionInputBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link DefaultHttpResponseParser}.
 */
public class TestResponseParser {

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidConstructorInput() throws Exception {
        new DefaultHttpResponseParser(null);
    }

    @Test
    public void testBasicMessageParsing() throws Exception {
        final String s =
            "HTTP/1.1 200 OK\r\n" +
            "Server: whatever\r\n" +
            "Date: some date\r\n" +
            "Set-Cookie: c1=stuff\r\n" +
            "\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(s, Consts.ASCII);

        final DefaultHttpResponseParser parser = new DefaultHttpResponseParser(inbuffer);
        final HttpResponse httpresponse = parser.parse();

        final StatusLine statusline = httpresponse.getStatusLine();
        Assert.assertNotNull(statusline);
        Assert.assertEquals(200, statusline.getStatusCode());
        Assert.assertEquals("OK", statusline.getReasonPhrase());
        Assert.assertEquals(HttpVersion.HTTP_1_1, statusline.getProtocolVersion());
        final Header[] headers = httpresponse.getAllHeaders();
        Assert.assertEquals(3, headers.length);
    }

    @Test
    public void testConnectionClosedException() throws Exception {
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(new byte[] {});

        final DefaultHttpResponseParser parser = new DefaultHttpResponseParser(inbuffer);
        try {
            parser.parse();
            Assert.fail("NoHttpResponseException should have been thrown");
        } catch (final NoHttpResponseException expected) {
        }
    }

    @Test
    public void testMessageParsingTimeout() throws Exception {
        final String s =
            "HTTP\000/1.1 200\000 OK\r\n" +
            "Server: wha\000tever\r\n" +
            "Date: some date\r\n" +
            "Set-Coo\000kie: c1=stuff\r\n" +
            "\000\r\n";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(
                new TimeoutByteArrayInputStream(s.getBytes(Consts.ASCII)), 16);

        final DefaultHttpResponseParser parser = new DefaultHttpResponseParser(inbuffer);

        int timeoutCount = 0;

        HttpResponse httpresponse = null;
        for (int i = 0; i < 10; i++) {
            try {
                httpresponse = parser.parse();
                break;
            } catch (final InterruptedIOException ex) {
                timeoutCount++;
            }

        }
        Assert.assertNotNull(httpresponse);
        Assert.assertEquals(5, timeoutCount);

        @SuppressWarnings("null") // httpresponse cannot be null here
        final
        StatusLine statusline = httpresponse.getStatusLine();
        Assert.assertNotNull(statusline);
        Assert.assertEquals(200, statusline.getStatusCode());
        Assert.assertEquals("OK", statusline.getReasonPhrase());
        Assert.assertEquals(HttpVersion.HTTP_1_1, statusline.getProtocolVersion());
        final Header[] headers = httpresponse.getAllHeaders();
        Assert.assertEquals(3, headers.length);
    }

}

