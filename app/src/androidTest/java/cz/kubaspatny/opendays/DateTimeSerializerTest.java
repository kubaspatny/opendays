package cz.kubaspatny.opendays;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import cz.kubaspatny.opendays.json.DateTimeSerializer;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DateTimeSerializerTest {

    private DateTimeSerializer dateTimeSerializer;

    @Before
    public void setUp() throws Exception {
        dateTimeSerializer = new DateTimeSerializer();
    }

    @Test
    public void testSerialize() throws Exception {
        DateTime time = new DateTime(DateTimeZone.UTC).withDate(2014,10,20).withTime(22,0,0,0);
        JsonElement serialized = dateTimeSerializer.serialize(time, null, null);
        Assert.assertEquals("2014-10-20T22:00:00.000Z", serialized.getAsString());
    }

    @Test
    public void testDeserialize() throws Exception {
        JsonElement e = new JsonParser().parse("\"2014-10-20T20:00:00.000Z\"").getAsJsonPrimitive();
        DateTime deserialized = dateTimeSerializer.deserialize(e, null, null);

        Assert.assertEquals(2014,   deserialized.getYear());
        Assert.assertEquals(10,     deserialized.getMonthOfYear());
        Assert.assertEquals(20,     deserialized.getDayOfMonth());

        Assert.assertEquals(20,     deserialized.getHourOfDay());
        Assert.assertEquals(0,      deserialized.getMinuteOfHour());
        Assert.assertEquals(0,      deserialized.getSecondOfMinute());
    }
}
