package com.autonomy.abc.selenium.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.autonomy.frontend.selenium.users.UserConfigParser;

// this only exists to avoid unchecked casts
// TODO: do we care?
public interface JsonUserConfigParser extends UserConfigParser<JsonNode> {
}
