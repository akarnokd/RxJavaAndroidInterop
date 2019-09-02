/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hu.akarnokd.rxjava3.android;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public final class AndroidInteropPluginsTest {
    @Before @After
    public void setUpAndTearDown() {
        AndroidInteropPlugins.reset();
    }

    @Test
    public void mainThreadHandlerCalled() {
        final AtomicReference<Scheduler> schedulerRef = new AtomicReference<>();
        final Scheduler newScheduler = new EmptyScheduler();
        AndroidInteropPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override public Scheduler apply(Scheduler scheduler) {
                schedulerRef.set(scheduler);
                return newScheduler;
            }
        });

        Scheduler scheduler = new EmptyScheduler();
        Scheduler actual = AndroidInteropPlugins.onMainThreadScheduler(scheduler);
        assertSame(newScheduler, actual);
        assertSame(scheduler, schedulerRef.get());
    }

    @Test
    public void resetClearsMainThreadHandler() {
        AndroidInteropPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override public Scheduler apply(Scheduler scheduler) {
                throw new AssertionError();
            }
        });
        AndroidInteropPlugins.reset();

        Scheduler scheduler = new EmptyScheduler();
        Scheduler actual = AndroidInteropPlugins.onMainThreadScheduler(scheduler);
        assertSame(scheduler, actual);
    }

    @Test
    public void initMainThreadHandlerCalled() {
        final AtomicReference<Callable<Scheduler>> schedulerRef = new AtomicReference<>();
        final Scheduler newScheduler = new EmptyScheduler();
        AndroidInteropPlugins
                .setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
                    @Override public Scheduler apply(Callable<Scheduler> scheduler) {
                        schedulerRef.set(scheduler);
                        return newScheduler;
                    }
                });

        Callable<Scheduler> scheduler = new Callable<Scheduler>() {
            @Override public Scheduler call() throws Exception {
                throw new AssertionError();
            }
        };
        Scheduler actual = AndroidInteropPlugins.initMainThreadScheduler(scheduler);
        assertSame(newScheduler, actual);
        assertSame(scheduler, schedulerRef.get());
    }

    @Test
    public void resetClearsInitMainThreadHandler() throws Exception {
        AndroidInteropPlugins
                .setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
                    @Override public Scheduler apply(Callable<Scheduler> scheduler) {
                        throw new AssertionError();
                    }
                });

        final Scheduler scheduler = new EmptyScheduler();
        Callable<Scheduler> schedulerCallable = new Callable<Scheduler>() {
            @Override public Scheduler call() throws Exception {
                return scheduler;
            }
        };

        AndroidInteropPlugins.reset();

        Scheduler actual = AndroidInteropPlugins.initMainThreadScheduler(schedulerCallable);
        assertSame(schedulerCallable.call(), actual);
    }

    @Test
    public void defaultMainThreadSchedulerIsInitializedLazily() {
        Function<Callable<Scheduler>, Scheduler> safeOverride =
                new Function<Callable<Scheduler>, Scheduler>() {
            @Override public Scheduler apply(Callable<Scheduler> scheduler) {
                return new EmptyScheduler();
            }
        };
        Callable<Scheduler> unsafeDefault = new Callable<Scheduler>() {
            @Override public Scheduler call() throws Exception {
                throw new AssertionError();
            }
        };

       AndroidInteropPlugins.setInitMainThreadSchedulerHandler(safeOverride);
       AndroidInteropPlugins.initMainThreadScheduler(unsafeDefault);
    }

    @Test
    public void overrideInitMainSchedulerThrowsWhenSchedulerCallableIsNull() {
        try {
            AndroidInteropPlugins.initMainThreadScheduler(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("scheduler == null", e.getMessage());
        }
    }

    @Test
    public void overrideInitMainSchedulerThrowsWhenSchedulerCallableReturnsNull() {
        Callable<Scheduler> nullResultCallable = new Callable<Scheduler>() {
            @Override public Scheduler call() throws Exception {
                return null;
            }
        };

        try {
            AndroidInteropPlugins.initMainThreadScheduler(nullResultCallable);
            fail();
        } catch (NullPointerException e) {
            assertEquals("Scheduler Callable returned null", e.getMessage());
        }
    }

    @Test
    public void getInitMainThreadSchedulerHandlerReturnsHandler() {
        Function<Callable<Scheduler>, Scheduler> handler = new Function<Callable<Scheduler>, Scheduler>() {
            @Override public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                return Schedulers.trampoline();
            }
        };
        AndroidInteropPlugins.setInitMainThreadSchedulerHandler(handler);
        assertSame(handler, AndroidInteropPlugins.getInitMainThreadSchedulerHandler());
    }

    @Test
    public void getMainThreadSchedulerHandlerReturnsHandler() {
        Function<Scheduler, Scheduler> handler = new Function<Scheduler, Scheduler>() {
            @Override public Scheduler apply(Scheduler scheduler) {
                return Schedulers.trampoline();
            }
        };
        AndroidInteropPlugins.setMainThreadSchedulerHandler(handler);
        assertSame(handler, AndroidInteropPlugins.getOnMainThreadSchedulerHandler());
    }

    @Test
    public void getInitMainThreadSchedulerHandlerReturnsNullIfNotSet() {
        AndroidInteropPlugins.reset();
        assertNull(AndroidInteropPlugins.getInitMainThreadSchedulerHandler());
    }

    @Test
    public void getMainThreadSchedulerHandlerReturnsNullIfNotSet() {
        AndroidInteropPlugins.reset();
        assertNull(AndroidInteropPlugins.getOnMainThreadSchedulerHandler());
    }
}
