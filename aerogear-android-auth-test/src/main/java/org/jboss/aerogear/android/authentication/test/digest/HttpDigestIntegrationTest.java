/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authentication.test.digest;

import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.android.authentication.digest.HttpDigestAuthenticationModule;
import org.jboss.aerogear.android.authentication.test.MainActivity;
import org.jboss.aerogear.android.authentication.test.AuthenticationModuleTest;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfiguration;
import org.jboss.aerogear.android.authentication.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.authentication.test.util.VoidCallback;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeManager;

@Suppress
public class HttpDigestIntegrationTest extends PatchedActivityInstrumentationTestCase implements AuthenticationModuleTest {

    private static final URL CONTROLLER_URL;
    private static final RestfulPipeConfiguration AUTOBOT_CONFIG;

    protected static final String TAG = HttpDigestIntegrationTest.class.getSimpleName();

    static {
        try {
            CONTROLLER_URL = new URL("http://controller-aerogear.rhcloud.com/aerogear-controller-demo/autobots");
            AUTOBOT_CONFIG = PipeManager.config("autobots", RestfulPipeConfiguration.class);
            AUTOBOT_CONFIG.withUrl(CONTROLLER_URL);

        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpDigestIntegrationTest() {
        super(MainActivity.class);
    }

    public void testBadLogin() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTOBOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("baduser", "badpass", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTOBOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertFalse(success.get());

    }

    public void testLogin() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTOBOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("agnes", "123", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTOBOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                latch.countDown();
            }
        });

        latch.await(1000, TimeUnit.SECONDS);
        assertTrue(success.get());
    }

    public void testLogout() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTOBOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("agnes", "123", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTOBOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertTrue(success.get());

        final CountDownLatch latch2 = new CountDownLatch(1);

        final CountDownLatch logoutLatch = new CountDownLatch(1);
        basicAuthModule.logout(new VoidCallback(logoutLatch));
        logoutLatch.await(2, TimeUnit.SECONDS);
        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch2.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                success.set(false);
                latch2.countDown();
            }
        });

        latch2.await(10, TimeUnit.SECONDS);
        assertFalse(success.get());

    }

}
