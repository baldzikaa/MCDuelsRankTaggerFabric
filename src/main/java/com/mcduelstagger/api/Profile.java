package com.mcduelstagger.api;

import java.util.Map;
import java.util.UUID;

public record Profile(UUID uuid, String username, Map<String, KitEntry> kits) {}
