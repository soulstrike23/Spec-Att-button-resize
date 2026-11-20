package com.largespecialattack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/**
 * Resize controls only. This plugin does not create hotkeys or dispatch clicks.
 */
@ConfigGroup(LargeSpecialAttackConfig.GROUP)
public interface LargeSpecialAttackConfig extends Config
{
    String GROUP = "largespecialattack";

    @ConfigItem(
        keyName = "orbEnabled",
        name = "Enlarge special attack orb",
        description = "Resize the native special attack orb and its clickable widget",
        position = 0
    )
    default boolean orbEnabled()
    {
        return true;
    }

    @Range(min = 100, max = 300)
    @ConfigItem(
        keyName = "orbScale",
        name = "Orb size (%)",
        description = "100 is original size. Larger values may overlap nearby UI widgets.",
        position = 1
    )
    default int orbScale()
    {
        return 175;
    }

    @Range(min = -200, max = 200)
    @ConfigItem(
        keyName = "orbOffsetX",
        name = "Orb horizontal offset",
        description = "Move the enlarged orb horizontally in client pixels",
        position = 2
    )
    default int orbOffsetX()
    {
        return 0;
    }

    @Range(min = -200, max = 200)
    @ConfigItem(
        keyName = "orbOffsetY",
        name = "Orb vertical offset",
        description = "Move the enlarged orb vertically in client pixels",
        position = 3
    )
    default int orbOffsetY()
    {
        return 0;
    }

    @ConfigItem(
        keyName = "barEnabled",
        name = "Enlarge combat-tab spec bar",
        description = "Experimental: resize the native clickable bar, including dual-wield bar layouts",
        position = 4
    )
    default boolean barEnabled()
    {
        return false;
    }

    @Range(min = 100, max = 300)
    @ConfigItem(
        keyName = "barScale",
        name = "Spec bar size (%)",
        description = "Scales width and height; 100 is original size",
        position = 5
    )
    default int barScale()
    {
        return 150;
    }
}
