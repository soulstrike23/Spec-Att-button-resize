
package com.largespecialattack;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class LargeSpecialAttackPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(LargeSpecialAttackPlugin.class);
        RuneLite.main(args);
    }
}
