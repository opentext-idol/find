package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.config.UserConfigParser;
import com.fasterxml.jackson.databind.JsonNode;

// this only exists to avoid unchecked casts
// TODO: do we care?
public interface JsonUserConfigParser extends UserConfigParser<JsonNode> {
}
