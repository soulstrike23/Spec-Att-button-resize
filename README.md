# Large Special Attack (experimental RuneLite plugin)

The goal is deliberately narrow: enlarge OSRS's **special attack orb** and,
optionally, the **combat-tab special attack bar**, without scaling your entire
UI. A larger clickable target is the point; this is not a cosmetic overlay.

## Current implementation

- The plugin resizes the *native* special attack orb widget tree, including
  the `SPECBUTTON` child, by 100–300%.
- Optional experimental resizing of the native combat-tab special bar for
  ordinary, mainhand and offhand layouts.
- Horizontal/vertical offsets for the orb, -200 to +200 client pixels.
- Reapplies layout when game UI scripts change it, and restores original
  widget dimensions and alignment modes when disabled.
- No new input actions: **no keybind, macro, mouse movement, custom menu
  dispatch, client-script invocation to trigger special attack, or auto-click**.

## Important limitations

**Prototype: builds against the current RuneLite dependency, but has NOT been
verified in a running, logged-in game client.** GitHub Actions successfully ran
`./gradlew clean build` on 2026-11-20. This verifies Java compilation; it does
not prove that the enlarged widget is clickable or that the implementation is
compatible with live OSRS interfaces.

In particular, the game's layout scripts may redraw children, resize the
clickable target differently than expected, or clip parts of a larger orb.
The bar is disabled by default because the new dual-special-bar layouts need
in-game validation. At high sizes the spec orb may overlap prayer/run orbs.
Do **not** install an unreviewed copy on an account you care about. Submit for
RuneLite Plugin Hub review first; approval is not guaranteed.

## Developer testing (a volunteer/reviewer can do this)

Open in IntelliJ with JDK 11 (the version RuneLite's Plugin Hub README
recommends), import as a Gradle project, and run the `run` Gradle task.

Check in fixed, resizable classic and resizable modern modes:
1. Equip a weapon with a special attack and move the pointer around the
   enlarged orb; test clicks *outside the old area but inside the enlarged one*.
2. Check that the right-click menu, toggle state and energy display are still
   correct, and other nearby buttons can still be clicked.
3. Enable bar resizing and switch weapons, including weapons with unusual
   special interfaces and dual/offhand bar layouts.
4. Resize the client, change UI scaling, logout/login, and disable the plugin.
   Ensure original widget sizes return and no UI is permanently displaced.

If the enlarged graphic appears but the new area is not clickable, this
implementation does **not** yet meet the core requirement and needs changes
before submission.

## Publishing through Plugin Hub

1. Get preliminary confirmation from RuneLite maintainers that expanding
   these native hitboxes is acceptable.
2. The public source repository is already available at
   https://github.com/soulstrike23/Spec-Att-button-resize .
3. Have a trusted developer test the enlarged **clickable area** in a development
   client and address any issues before submitting.
4. Once functionality and compliance are verified, fork
   `https://github.com/runelite/plugin-hub` and create a file
   `plugins/large-special-attack` containing the repository URL and the full
   40-character commit hash of your uploaded source. There is a template in
   `plugin-hub-submission/`.
5. Open a pull request from that fork to the official Plugin Hub repository.

Official instructions: https://github.com/runelite/plugin-hub#submitting-a-plugin

## Notes for developers

This uses the named IDs from `net.runelite.api.gameval.InterfaceID` instead of
hard-coded numeric child IDs, and the public RuneLite Widget API (`setPos`,
`setSize`, `revalidate`). It saves original modes and dimensions per widget
identity rather than multiplying sizes every frame.

Potential follow-up work after actual game testing: use native layout offsets
rather than scaling every child if the game uses sprite sizing inconsistently;
check clipping and overlap with orb parent; test combat interfaces for single,
mainhand and offhand variants; and ensure the enlarged target always retains
native click listeners.

Released under the BSD 2-Clause license.
