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

import android.os.Build;
import android.os.Looper;
import android.os.Message;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowMessageQueue;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public final class AndroidInteropSchedulersTest {

    @Before @After
    public void setUpAndTearDown() {
        AndroidInteropPlugins.reset();
    }

    @Test
    public void mainThreadCallsThroughToHook() {
        final AtomicInteger called = new AtomicInteger();
        final Scheduler newScheduler = new EmptyScheduler();
        AndroidInteropPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override public Scheduler apply(Scheduler scheduler) {
                called.getAndIncrement();
                return newScheduler;
            }
        });

        assertSame(newScheduler, AndroidInteropSchedulers.mainThread());
        assertEquals(1, called.get());

        assertSame(newScheduler, AndroidInteropSchedulers.mainThread());
        assertEquals(2, called.get());
    }

    @Test
    public void fromNullThrows() {
        try {
            AndroidInteropSchedulers.from(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("looper == null", e.getMessage());
        }
    }

    @Test
    public void fromNullThrowsTwoArg() {
        try {
            AndroidInteropSchedulers.from(null, false);
            fail();
        } catch (NullPointerException e) {
            assertEquals("looper == null", e.getMessage());
        }
    }

    @Test
    public void fromReturnsUsableScheduler() {
        assertNotNull(AndroidInteropSchedulers.from(Looper.getMainLooper()));
    }

    @Test
    public void asyncIgnoredPre16() {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 14);

        ShadowLooper mainLooper = ShadowLooper.getShadowMainLooper();
        mainLooper.pause();
        ShadowMessageQueue mainMessageQueue = shadowOf(Looper.getMainLooper().getQueue());

        Scheduler main = AndroidInteropSchedulers.from(Looper.getMainLooper(), true);
        main.scheduleDirect(new Runnable() {
            @Override public void run() {
            }
        });

        Message message = mainMessageQueue.getHead();
        assertFalse(message.isAsynchronous());
    }
}
