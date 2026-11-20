package com.largespecialattack;

import com.google.inject.Provides;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Experimental native-widget resizing, not an overlay or macro.
 *
 * The client retains all native click listeners and actions. We only change
 * widget layout properties. Never call invokeMenuAction, run client scripts
 * to perform an action, or generate mouse/keyboard input.
 */
@PluginDescriptor(
    name = "Large Special Attack",
    description = "Enlarge the actual special attack orb and combat-tab bar",
    tags = {"special attack", "orb", "combat", "accessibility", "ui"}
)
public class LargeSpecialAttackPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private LargeSpecialAttackConfig config;

    // Widget objects can be replaced whenever the game rebuilds the interface.
    // Identity-based snapshots avoid scaling a widget a second time.
    private final Map<Widget, WidgetState> saved = new IdentityHashMap<>();

    @Provides
    LargeSpecialAttackConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(LargeSpecialAttackConfig.class);
    }

    @Override
    protected void startUp()
    {
        clientThread.invoke(this::apply);
    }

    @Override
    protected void shutDown()
    {
        clientThread.invoke(this::restoreAll);
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        // The game routinely recalculates interface layout and may overwrite
        // properties. Reapply only where dimensions/position have changed.
        apply();
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        if (event.getScriptId() == ScriptID.COMBAT_INTERFACE_SETUP
            || event.getScriptId() == ScriptID.COMBAT_INTERFACE_SP
            || event.getScriptId() == ScriptID.TOPLEVEL_REDRAW)
        {
            // Do not interfere while a game script is updating widgets.
            clientThread.invokeLater(this::apply);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (LargeSpecialAttackConfig.GROUP.equals(event.getGroup()))
        {
            clientThread.invokeLater(() ->
            {
                restoreAll();
                apply();
            });
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            // Widgets were invalidated by the client; don't restore stale ones.
            saved.clear();
        }
    }

    private void apply()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        if (config.orbEnabled() && config.orbScale() > 100)
        {
            Widget orb = client.getWidget(InterfaceID.Orbs.ORB_SPECENERGY);
            scaleWidgetTree(orb, config.orbScale(), config.orbOffsetX(), config.orbOffsetY());
        }

        if (config.barEnabled() && config.barScale() > 100)
        {
            // The combat interface has different spec layouts for ordinary,
            // main-hand and off-hand weapon setups. Each root is distinct.
            scaleWidgetTree(client.getWidget(InterfaceID.CombatInterface.SP_ATTACKBAR),
                config.barScale(), 0, 0);
            scaleWidgetTree(client.getWidget(InterfaceID.CombatInterface.SP_ATTACKBAR_MAINHAND),
                config.barScale(), 0, 0);
            scaleWidgetTree(client.getWidget(InterfaceID.CombatInterface.SP_ATTACKBAR_OFFHAND),
                config.barScale(), 0, 0);
        }
    }

    private void scaleWidgetTree(Widget root, int percent, int offsetX, int offsetY)
    {
        if (root == null || root.isHidden())
        {
            return;
        }

        final double factor = Math.max(1.0, Math.min(3.0, percent / 100.0));
        resizeTree(root, factor, true, offsetX, offsetY);
    }

    private void resizeTree(Widget widget, double factor, boolean root, int offsetX, int offsetY)
    {
        if (widget == null)
        {
            return;
        }

        // Capture original position and size before any adjustments.
        WidgetState original = saved.computeIfAbsent(widget, WidgetState::new);
        int width = Math.max(1, (int) Math.round(original.width * factor));
        int height = Math.max(1, (int) Math.round(original.height * factor));

        // Center the root on its original location; scale child positions
        // within their parent so the actual interactive widget scales too.
        int x = root
            ? original.relativeX - (width - original.width) / 2 + offsetX
            : (int) Math.round(original.relativeX * factor);
        int y = root
            ? original.relativeY - (height - original.height) / 2 + offsetY
            : (int) Math.round(original.relativeY * factor);

        if (widget.getOriginalX() != x || widget.getOriginalY() != y
            || widget.getOriginalWidth() != width || widget.getOriginalHeight() != height
            || widget.getXPositionMode() != WidgetPositionMode.ABSOLUTE_LEFT
            || widget.getYPositionMode() != WidgetPositionMode.ABSOLUTE_TOP
            || widget.getWidthMode() != WidgetSizeMode.ABSOLUTE
            || widget.getHeightMode() != WidgetSizeMode.ABSOLUTE)
        {
            widget.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_TOP);
            widget.setSize(width, height, WidgetSizeMode.ABSOLUTE, WidgetSizeMode.ABSOLUTE);
            widget.revalidate();
        }

        Widget[] children = widget.getChildren();
        if (children != null)
        {
            for (Widget child : children)
            {
                resizeTree(child, factor, false, 0, 0);
            }
        }
    }

    private void restoreAll()
    {
        for (Map.Entry<Widget, WidgetState> entry : saved.entrySet())
        {
            Widget widget = entry.getKey();
            WidgetState original = entry.getValue();
            widget.setPos(original.originalX, original.originalY, original.xMode, original.yMode);
            widget.setSize(original.originalWidth, original.originalHeight,
                original.widthMode, original.heightMode);
            widget.revalidate();
        }
        saved.clear();
    }

    /** All four layout modes are restored, not just pixel dimensions. */
    private static final class WidgetState
    {
        final int originalX, originalY, originalWidth, originalHeight;
        final int xMode, yMode, widthMode, heightMode;
        final int relativeX, relativeY, width, height;

        WidgetState(Widget w)
        {
            originalX = w.getOriginalX();
            originalY = w.getOriginalY();
            originalWidth = w.getOriginalWidth();
            originalHeight = w.getOriginalHeight();
            xMode = w.getXPositionMode();
            yMode = w.getYPositionMode();
            widthMode = w.getWidthMode();
            heightMode = w.getHeightMode();
            relativeX = w.getRelativeX();
            relativeY = w.getRelativeY();
            width = w.getWidth();
            height = w.getHeight();
        }
    }
}
