package com.yandex.tracker.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration duration, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(duration.toMillis());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return Duration.ofMillis(json.getAsLong());
    }
}
