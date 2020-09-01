package xyz.tsumugu2626.app.fami;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("xyz.tsumugu2626.app.fami", appContext.getPackageName());

        for (int i=1;i<=8;i++) {
            Log.d("testcode", i+String.valueOf(MyTopArrangementManager.get_top_arrangement(i)));
        }
    }
}
