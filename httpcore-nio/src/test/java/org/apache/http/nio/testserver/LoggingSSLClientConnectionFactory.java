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
package org.apache.http.nio.testserver;

import javax.net.ssl.SSLContext;

import se.tink.org.apache.http.impl.nio.DefaultNHttpClientConnection;
import se.tink.org.apache.http.nio.NHttpConnectionFactory;
import se.tink.org.apache.http.nio.reactor.IOSession;
import se.tink.org.apache.http.nio.reactor.ssl.SSLIOSession;
import se.tink.org.apache.http.nio.reactor.ssl.SSLMode;
import se.tink.org.apache.http.nio.reactor.ssl.SSLSetupHandler;

public class LoggingSSLClientConnectionFactory implements NHttpConnectionFactory<DefaultNHttpClientConnection> {

    private final SSLContext sslcontext;
    private final SSLSetupHandler setupHandler;

    public LoggingSSLClientConnectionFactory(
            final SSLContext sslcontext, final SSLSetupHandler setupHandler) {
        super();
        this.sslcontext = sslcontext;
        this.setupHandler = setupHandler;
    }

    public LoggingSSLClientConnectionFactory(final SSLContext sslcontext) {
        this(sslcontext, null);
    }

    public DefaultNHttpClientConnection createConnection(final IOSession iosession) {
        final SSLIOSession ssliosession = new SSLIOSession(
                iosession, SSLMode.CLIENT, this.sslcontext, this.setupHandler);
        iosession.setAttribute(SSLIOSession.SESSION_KEY, ssliosession);
        return new LoggingNHttpClientConnection(ssliosession);
    }

}
