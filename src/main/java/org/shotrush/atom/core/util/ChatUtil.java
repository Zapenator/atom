package org.shotrush.atom.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public static Component color(String message) {
        if (message == null) return Component.empty();

        if (message.contains("<") && message.contains(">")) {
            try {
                return miniMessage.deserialize(message);
            } catch (Exception e) {

                return legacySerializer.deserialize(message);
            }
        }
        
        // Legacy
        return legacySerializer.deserialize(message);
    }
}
