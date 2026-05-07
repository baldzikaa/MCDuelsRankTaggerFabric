package com.mcduelstagger.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {
    private static final String JSON = """
        {
          "uuid": "b876ec32-e396-476b-a115-8438d83c67d4",
          "username": "Technoblade",
          "kits": {
            "crystal": { "rank": "high_dueler", "elo": 2488, "position": 1 },
            "uhc":     { "rank": "high_dueler", "elo": 2393, "position": 1 }
          }
        }""";

    @Test void deserializesViaGson() {
        Profile p = new Gson().fromJson(JSON, Profile.class);
        assertEquals(UUID.fromString("b876ec32-e396-476b-a115-8438d83c67d4"), p.uuid());
        assertEquals("Technoblade", p.username());
        Map<String, KitEntry> kits = p.kits();
        assertEquals(2, kits.size());
        assertEquals("high_dueler", kits.get("crystal").rank());
        assertEquals(2488, kits.get("crystal").elo());
    }
    @Test void absentKitsMapBecomesEmptyMap() {
        Profile p = new Gson().fromJson(
            "{\"uuid\":\"b876ec32-e396-476b-a115-8438d83c67d4\",\"username\":\"x\"}",
            Profile.class);
        assertNotNull(p);
        assertNull(p.kits());
    }
}
