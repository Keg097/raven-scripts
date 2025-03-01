final List<Map<String, Object>> mods = new ArrayList<>();
final Map<String, Map<String, String>> customModuleData = new HashMap<>();
final List<Map<String, Object>> customDataList = new ArrayList<>();
final Map<String, Boolean> excludedCategories = new HashMap<>();

/* Settings */
int direction;
int animationMode;
int outlineMode;
float gap = 1;
float lineGap;
float textScale;
float xOffset;
float yOffset;
float moduleHeight;
int animationDuration;
boolean lowercase;
int resetTicks = 0;

void onLoad() {
    modules.registerDescription("> Categories");
    for (String category : modules.getCategories().keySet()) {
        if (!category.equalsIgnoreCase("profiles")) {
            excludedCategories.put(category, false);
            modules.registerButton("Show " + category, true);
        }
    }
    
    modules.registerDescription("> Settings");
    modules.registerSlider("Direction", "", 1, new String[]{"Up", "Down"});
    modules.registerSlider("Animations", "", 0, new String[]{"Scale Right", "Scale Center"});
    modules.registerSlider("Animation Speed", "ms", 250, 0, 2000, 10);
    modules.registerButton("Lowercase", false);
    modules.registerSlider("Scale", "", 1, 0.5, 2, 0.1);
    modules.registerSlider("X-Offset", "", 1, 0, 50, 1);
    modules.registerSlider("Y-Offset", "", 1, 0, 50, 1);
    modules.registerSlider("Outline Mode", "", 0, new String[]{util.color("&cDisabled"), "Left (WIP)", "Right", "Full (WIP)"});
    modules.registerSlider("Suffix Addons", "", 0, new String[]{util.color("&cDisabled"), "Angle Brackets", "Brackets", "Curly Braces", "Dash", "Parentheses"});
    modules.registerSlider("Alternative Suffixes", "", 0, new String[]{"Bypass", "Server", "Raven"});
    modules.registerSlider("Suffix Color", "", 7, new String[]{util.color("&0Black"), util.color("&1Dark Blue"), util.color("&2Dark Green"), util.color("&3Dark Aqua"), util.color("&4Dark Red"), util.color("&5Dark Purple"), util.color("&6Gold"), util.color("&7Gray"), util.color("&8Dark Gray"), util.color("&9Blue"), util.color("&aGreen"), util.color("&bAqua"), util.color("&cRed"), util.color("&dLight Purple"), util.color("&eYellow"), util.color("&fWhite")});
    modules.registerSlider("Line Gap", "", 2, 0, 5, 0.1);
    modules.registerDescription("Made by @Pug");
    modules.registerDescription("Ported by @desiyn");
    modules.registerDescription("Edited by @.key97");
}

void setDataStatic(String moduleName, String alias, String overrideValue) {
    Map<String, Object> customData = new HashMap<>();
    customData.put("moduleName", moduleName);
    customData.put("alias", alias);
    customData.put("overrideValue", overrideValue);
    customData.put("type", "fixed");
    customDataList.add(customData);

    updateCustomData(customData);
}

void setDataSlider(String moduleName, String alias, String displayString, String[] placeholders) {
    Map<String, Object> customData = new HashMap<>();
    customData.put("moduleName", moduleName);
    if (!alias.isEmpty()) customData.put("alias", alias);
    customData.put("displayString", displayString);
    customData.put("placeholders", placeholders);
    customData.put("type", "placeholders");
    customDataList.add(customData);

    updateCustomData(customData);
}

void setDataArray(String moduleName, String alias, String setting, String[] possibleValues) {
    Map<String, Object> customData = new HashMap<>();
    customData.put("moduleName", moduleName);
    if (!alias.isEmpty()) customData.put("alias", alias);
    customData.put("setting", setting);
    customData.put("possibleValues", possibleValues);
    customData.put("type", "strings");
    customDataList.add(customData);

    updateCustomData(customData);
}

void updateCustomData(Map<String, Object> customData) {
    String moduleName = (String) customData.get("moduleName");
    String alias = moduleName;

    if (customData.containsKey("alias")) {
        String customAlias = (String) customData.get("alias");
        if (customAlias != null && !customAlias.isEmpty()) {
            alias = customAlias;
        }
    }

    String overrideValue = "";

    switch ((String) customData.get("type")) {
        case "fixed":
            overrideValue = (String) customData.get("overrideValue");
            break;

        case "placeholders":
            String displayString = (String) customData.get("displayString");
            String[] placeholders = (String[]) customData.get("placeholders");

            for (int i = 0; i < placeholders.length; i++) {
                String placeholder = "%v" + (i + 1);
                String sliderValue = formatDoubleStr(modules.getSlider(moduleName, placeholders[i]));
                displayString = displayString.replace(placeholder, sliderValue);
            }

            overrideValue = displayString;
            break;

        case "strings":
            String setting = (String) customData.get("setting");
            String[] possibleValues = (String[]) customData.get("possibleValues");

            int index = (int) modules.getSlider(moduleName, setting);
            overrideValue = possibleValues[Math.min(index, possibleValues.length - 1)];
            break;
    }

    if ("NoSlow".equals(moduleName)) {
        int noSlowPlusMode = (int) modules.getSlider("Speed", "Speed");
        int alternativeSuffixMode = (int) modules.getSlider(scriptName, "Alternative Suffixes");

        if (modules.isEnabled("Speed")) {
            if (noSlowPlusMode == 1 && "Float".equals(overrideValue)) {
                overrideValue = "Boost";
            }
        }
    }

    Map<String, String> data = new HashMap<>();
    data.put("alias", alias);
    data.put("overrideValue", overrideValue);
    customModuleData.put(moduleName, data);
}

void onEnable() {
    mods.clear();
    resetTicks = 0;
    Map<String, List<String>> categories = modules.getCategories();
    
    for (String category : categories.keySet()) {
        if (category.equalsIgnoreCase("profiles")) continue;
        
        boolean categoryEnabled = modules.getButton(scriptName, "Show " + category);
        excludedCategories.put(category, !categoryEnabled);
        
        if (!categoryEnabled) continue;
        
        List<String> modulesList = categories.get(category);
        for (String module : modulesList) {
            if (EXCLUDED_MODULES.contains(module)) continue;
            
            Map<String, Object> modData = new HashMap<>();
            modData.put("name", module);
            modData.put("visibility", false);
            modData.put("offset", 0);
            modData.put("scale", 0);
            modData.put("animating", false);
            modData.put("animatingUp", false);
            modData.put("animationStart", 0L);
            modData.put("animationProgress", 0);
            mods.add(modData);
        }
    }

    updateButtonStates();
    updateSliders();
    updateEnabledModules();
    sortModules();
}

void onPreUpdate() {
    resetTicks++;
    
    boolean needsRebuild = false;
    for (String category : excludedCategories.keySet()) {
        boolean currentSetting = !modules.getButton(scriptName, "Show " + category);
        if (currentSetting != excludedCategories.get(category)) {
            needsRebuild = true;
            break;
        }
    }
    
    if (needsRebuild) {
        onEnable(); // Rebuild the entire arraylist
        return;
    }
    
    updateEnabledModules();
    lineGap = (float) modules.getSlider(scriptName, "Line Gap");
    moduleHeight = (float) render.getFontHeight() + gap;
    xOffset = (float) modules.getSlider(scriptName, "X-Offset");
    yOffset = (float) modules.getSlider(scriptName, "Y-Offset");

    if (resetTicks == 1 || resetTicks % 20 == 0) {
        updateSliders();
    }
}

void updateSliders() {
    lowercase = modules.getButton(scriptName, "Lowercase");
    direction = (int) modules.getSlider(scriptName, "Direction");
    textScale = (float) modules.getSlider(scriptName, "Scale");
    animationDuration = (int) modules.getSlider(scriptName, "Animation Speed");
    animationMode = (int) modules.getSlider(scriptName, "Animations");
    outlineMode = (int) modules.getSlider(scriptName, "Outline Mode");

    updateColors();

    int alternativeSuffixMode = (int) modules.getSlider(scriptName, "Alternative Suffixes");
    
    if (alternativeSuffixMode == 1) { // Server
        setDataArray("KillAura", "Aura", "Targets", new String[]{"Single", "Single", "Switch"});
        setDataArray("Bhop", "Speed", "Mode", new String[]{"Strafe", "Ground", "Glide", "Low", "Semi", "Low", "Semi", "Hurt-Time"});
        setDataArray("Speed", "Momentum", "Speed", new String[]{"Vanilla", "Hypixel"});
        setDataSlider("AntiVoid", "AntiVoid", "Hypixel", new String[]{""});
        setDataSlider("Disabler", "Disabler", "Hypixel", new String[]{""});
        setDataSlider("Virtu", "Virtu", "Private", new String[]{""});
        setDataSlider("TargetStrafe", "TargetStrafe", "Circle", new String[]{""});
        setDataSlider("AntiKnockback", "Velocity", "%v1% %v2%", new String[]{"Horizontal", "Vertical"});
        setDataSlider("FastMine", "", "%v1x", new String[]{"Break speed"});
        setDataSlider("Jump Reset", "", "%v1%", new String[]{"Chance"});
        setDataSlider("WTap", "", "%v1%", new String[]{"Chance"});
        setDataSlider("InvManager", "InvManager", "%v1x", new String[]{"Auto sort"});
        setDataArray("NoSlow", "NoSlowdown", "Mode", new String[]{"Vanilla", "Hypixel", "Interact", "Invalid", "Jump", "Sneak"});
        setDataArray("NoFall", "", "Mode", new String[]{"Spoof", "Single", "Hypixel", "NoGround A", "NoGround B", "Precision", "Position"});
        setDataArray("BedAura", "BedNuker", "Break mode", new String[]{"Legit", "Instant", "Swap"});
        setDataArray("InvMove", "InvWalk", "Inventory", new String[]{"Disabled", "Hypixel", "Blink", "Close"});

        if (modules.getButton("Safewalk", "Shift")) {
            setDataSlider("Safewalk", "SafeWalk", "Legit", new String[]{""});
        } else {
            setDataSlider("Safewalk", "SafeWalk", "Blatant", new String[]{""});
        }

        if (modules.getButton("AntiBot", "Filter watchdog")) {
            setDataSlider("AntiBot", "AntiBot", "Hypixel", new String[]{""});
        } else {
            setDataSlider("AntiBot", "AntiBot", "Generic", new String[]{""});
        }

        if (modules.getButton("Long Jump", "Allow strafe")) {
            setDataArray("Long Jump", "Flight", "Mode", new String[]{"Vanilla", "FireballStrafe"});
        } else {
            setDataArray("Long Jump", "Flight", "Mode", new String[]{"Vanilla", "Fireball"});
        }

        if (modules.getButton("Scaffold", "Jump facing forward")) {
            setDataArray("Scaffold", "Scaffold", "Rotation", new String[]{"ResetStrict", "ResetSimple", "ResetLazy", "ResetOffset"});
        } else {
            setDataArray("Scaffold", "Scaffold", "Rotation", new String[]{"Strict", "Simple", "Lazy", "Offset"});
        }
    } else if (alternativeSuffixMode == 2) { // Raven
        setDataArray("KillAura", "KillAura", "Rotation", new String[]{"Silent", "Lock view", "NoRotationSet"});
        setDataSlider("AntiKnockback", "Velocity", "%v1% %v2%", new String[]{"Horizontal", "Vertical"});
        setDataSlider("FastMine", "", "%v1x", new String[]{"Break speed"});
        setDataSlider("Jump Reset", "", "%v1%", new String[]{"Chance"});
        setDataSlider("WTap", "", "%v1%", new String[]{"Chance"});
        setDataSlider("InvManager", "InvManager", "", new String[]{""});
        setDataArray("NoSlow", "NoSlow", "Mode", new String[]{"Vanilla", "Float", "Interact", "Invalid", "Jump", "Sneak"});
        setDataArray("NoFall", "", "Mode", new String[]{"Spoof", "Single", "Extra", "NoGround A", "NoGround B", "Precision", "Position"});
        setDataArray("BedAura", "BedAura", "Break mode", new String[]{"Legit", "Instant", "Swap"});
        setDataArray("Scaffold", "Scaffold", "Fast scaffold", new String[]{"Disabled", "Vanilla", "Constant", "Edge", "Float", "Jump", "Jump", "Jump", "Jump", "Keep-Y", "Keep-Y", "Slide"});
        setDataArray("Bhop", "", "Mode", new String[]{"Strafe", "Ground", "Glide", "Low", "Semi", "Low", "Semi", "Hurt time"});
        setDataArray("Speed", "Speed", "Speed", new String[]{"Vanilla", "Float"});

        if (modules.getButton("Long Jump", "Allow strafe")) {
            setDataArray("Long Jump", "Flight", "Mode", new String[]{"Vanilla", "FireballStrafe"});
        } else {
            setDataArray("Long Jump", "Flight", "Mode", new String[]{"Vanilla", "Fireball"});
        }
    } else { // Bypass
        setDataArray("KillAura", "KillAura", "Autoblock", new String[]{"Legit", "Blinkless", "Damage", "Visual", "Semi", "Packet", "Interaction", "Blink", "Buffer"});
        setDataSlider("Disabler", "Disabler", "FastFall", new String[]{""});
        setDataSlider("Virtu", "Virtu", "Private", new String[]{""});
        setDataSlider("TargetStrafe", "TargetStrafe", "Circle", new String[]{""});
        setDataSlider("AntiKnockback", "", "%v1% %v2%", new String[]{"Horizontal", "Vertical"});
        setDataSlider("FastMine", "", "%v1x", new String[]{"Break speed"});
        setDataSlider("Jump Reset", "", "%v1%", new String[]{"Chance"});
        setDataSlider("WTap", "", "%v1%", new String[]{"Chance"});
        setDataSlider("InvManager", "Inventory", "%v1x", new String[]{"Auto sort"});
        setDataArray("NoSlow", "NoSlow", "Mode", new String[]{"Vanilla", "Float", "Interact", "Invalid", "Jump", "Sneak"});
        setDataArray("NoFall", "", "Mode", new String[]{"Spoof", "Single", "Packet", "NoGround A", "NoGround B", "Precision", "Position"});
        setDataArray("BedAura", "", "Break mode", new String[]{"Legit", "Instant", "Dynamic"});
        setDataArray("Long Jump", "Flight", "Mode", new String[]{"Vanilla", "Fireball"});
        setDataArray("Scaffold", "Scaffold", "Fast scaffold", new String[]{"Walk", "Vanilla", "Constant", "Edge", "Float", "Jump", "Jump", "Jump", "Jump", "Keep-Y", "Keep-Y", "Slide"});
        setDataArray("Bhop", "", "Mode", new String[]{"Strafe", "GroundStrafe", "GlideStrafe", "7 Tick", "8 Tick AirStrafe", "9 Tick", "9 Tick AirStrafe", "Hurt-Time"});
        setDataArray("Speed", "Speed", "Speed", new String[]{"Motion", "Float"});
        setDataArray("InvMove", "", "Inventory", new String[]{"Disabled", "Vanilla", "Blink", "Close"});

        if (modules.getButton("AntiBot", "Filter watchdog")) {
            setDataSlider("AntiBot", "AntiBot", "Watchdog", new String[]{""});
        } else {
            setDataSlider("AntiBot", "AntiBot", "Universal", new String[]{""});
        }
        
        if (modules.getButton("Long Jump", "Allow strafe")) {
            setDataArray("Long Jump", "LongJump", "Mode", new String[]{"Motion", "ItemDamageStrafe"});
        } else {
            setDataArray("Long Jump", "LongJump", "Mode", new String[]{"Motion", "ItemDamage"});
        }

        if (modules.getButton("Safewalk", "Shift")) {
            setDataSlider("Safewalk", "SafeWalk", "Sneak", new String[]{""});
        } else {
            setDataSlider("Safewalk", "SafeWalk", "Motion", new String[]{""});
        }
    }
    
    for (Map<String, Object> customData : customDataList) {
        updateCustomData(customData);
    }

    sortModules();
}

void onRenderTick(float partialTicks) {
    int[] displaySize = client.getDisplaySize();
    float x = xOffset;
    float y = yOffset;
    float displayWidth = displaySize[0] - x;

    updateAnimations();

    int suffixAddonMode = (int) modules.getSlider(scriptName, "Suffix Addons");
    int suffixColorIndex = (int) modules.getSlider(scriptName, "Suffix Color");
    String suffixColor = getColorCode(suffixColorIndex);

    long index = 0;
    for (Map<String, Object> mod : mods) {
        boolean animating = (boolean) mod.get("animating");
        if (!(boolean) mod.get("visibility") && !animating) {
            continue;
        }

        String moduleName = (String) mod.get("name");
        String displayName = moduleName;
        String displayValue = "";

        if (customModuleData.containsKey(moduleName)) {
            Map<String, String> customData = customModuleData.get(moduleName);
            displayName = customData.getOrDefault("alias", moduleName);
            displayValue = customData.getOrDefault("overrideValue", "");
        }

        float scale = (float) mod.get("scale") * textScale;
        String textToDisplay = displayName + (displayValue.isEmpty() ? "" : " " + suffixColor + formatSuffix(displayValue, suffixAddonMode));

        float textWidth = (float) render.getFontWidth(textToDisplay) * textScale;
        float scaledTextWidth = textWidth * scale;
        float finalXPosition;

        switch (animationMode) {
            case 1: // Scale Center
                finalXPosition = displayWidth - x - (textWidth / 2f) - ((textWidth * scale) / (2f * textScale));
                break;
            case 0: // Scale Right
                finalXPosition = displayWidth - (scaledTextWidth / textScale) - x + (1 - scale);
                break;
            default:
                finalXPosition = displayWidth - scaledTextWidth - x;
                break;
        }

        float x1 = finalXPosition - scale;
        float y1 = y;
        float x2 = finalXPosition + (textWidth / textScale) * scale + scale;
        float y2 = y + render.getFontHeight() * scale + scale;

        render.rect(x1, y1, x2, y2, getBackgroundColor());

        int color = getCurrentColor(index);
        
        render.text(lowercase ? textToDisplay.toLowerCase() : textToDisplay, finalXPosition, y1 + scale, scale, color, true);

        if (outlineMode == 2) {
            float outlineX = displayWidth - x + lineGap * textScale;
            float outlineY1 = y1;
            float outlineY2 = y2;

            render.rect(outlineX, outlineY1, outlineX + textScale, outlineY2, color);
        }

        y += moduleHeight * scale;
        index += (direction == 0) ? 100 * scale : -100 * scale;
    }
}

void updateEnabledModules() {
    long now = client.time();
    List<String> previousEnabledModules = new ArrayList<>();

    if (resetTicks < 60 || resetTicks % 20 == 0) {
        updateButtonStates();
    }

    for (Map<String, Object> mod : mods) {
        String moduleName = (String) mod.get("name");
        boolean currentlyVisible = (boolean) mod.get("visibility");
        boolean shouldBeVisible = (boolean) mod.getOrDefault("buttonEnabled", false) && modules.isEnabled(moduleName);

        if (currentlyVisible) {
            previousEnabledModules.add(moduleName);
        }

        if (shouldBeVisible != currentlyVisible) {
            mod.put("visibility", shouldBeVisible);
            mod.put("animating", true);
            mod.put("animatingUp", !shouldBeVisible);

            float animationProgress = ((Number) mod.get("animationProgress")).floatValue();
            animationProgress = (animationProgress >= 1f) ? 0f : (animationProgress > 0f ? 1f - animationProgress : animationProgress);

            long adjustedStartTime = now - (long) (animationDuration * animationProgress);
            mod.put("animationStart", adjustedStartTime);
        }
    }
}

void updateAnimations() {
    long currentTime = client.time();

    for (Map<String, Object> mod : mods) {
        if ((boolean) mod.get("animating")) {
            long startTime = (long) mod.get("animationStart");
            float elapsed = (float) (currentTime - startTime) / (float) animationDuration;

            if (elapsed >= 1f) {
                elapsed = 1f;
                mod.put("animating", false);
            }

            mod.put("animationProgress", elapsed);

            float easedOffset = quadInOut(elapsed) * moduleHeight;
            float easedScale = quadInOut(elapsed);
            if ((boolean) mod.get("animatingUp")) {
                mod.put("offset", moduleHeight - easedOffset);
                mod.put("scale", 1f - easedScale);
            } else {
                mod.put("offset", easedOffset);
                mod.put("scale", easedScale);
            }
        }
    }
}

float quadInOut(float t) {
    if (t < 0.5f) {
        return 2 * t * t;
    } else {
        return -1 + (4 - 2 * t) * t;
    }
}

void updateButtonStates() {
    for (Map<String, Object> mod : mods) {
        String moduleName = (String) mod.get("name");
        boolean isButtonEnabled = !modules.isHidden(moduleName);
        mod.put("buttonEnabled", isButtonEnabled);
    }

    sortModules();
}

void sortModules() {
    mods.sort((a, b) -> {
        String aName = (String) a.get("name");
        String bName = (String) b.get("name");

        String aDisplayName = aName;
        String bDisplayName = bName;

        if (customModuleData.containsKey(aName)) {
            Map<String, String> customDataA = customModuleData.get(aName);
            aDisplayName = customDataA.getOrDefault("alias", aName) + (customDataA.containsKey("overrideValue") ? ": " + customDataA.get("overrideValue") : "");
        }

        if (customModuleData.containsKey(bName)) {
            Map<String, String> customDataB = customModuleData.get(bName);
            bDisplayName = customDataB.getOrDefault("alias", bName) + (customDataB.containsKey("overrideValue") ? ": " + customDataB.get("overrideValue") : "");
        }

        int widthA = render.getFontWidth(aDisplayName);
        int widthB = render.getFontWidth(bDisplayName);

        return Integer.compare(widthB, widthA);
    });
}

String formatDoubleStr(double val) {
    return val == (long) val ? Long.toString((long) val) : Double.toString(val);
}

String formatSuffix(String suffix, int mode) {
    switch (mode) {
        case 1: // Angle Brackets
            return "<" + suffix + ">";
        case 2: // Brackets
            return "[" + suffix + "]";
        case 3: // Curly Braces
            return "{" + suffix + "}";
        case 4: // Dash
            return "- " + suffix;
        case 0: // Disabled
            return suffix;
        case 5: // Parentheses
            return "(" + suffix + ")";
        default:
            return suffix;
    }
}

String getColorCode(int index) {
    switch (index) {
        case 0: return util.colorSymbol + "0"; // Black
        case 1: return util.colorSymbol + "1"; // Dark Blue
        case 2: return util.colorSymbol + "2"; // Dark Green
        case 3: return util.colorSymbol + "3"; // Dark Aqua
        case 4: return util.colorSymbol + "4"; // Dark Red
        case 5: return util.colorSymbol + "5"; // Dark Purple
        case 6: return util.colorSymbol + "6"; // Gold
        case 7: return util.colorSymbol + "7"; // Gray
        case 8: return util.colorSymbol + "8"; // Dark Gray
        case 9: return util.colorSymbol + "9"; // Blue
        case 10: return util.colorSymbol + "a"; // Green
        case 11: return util.colorSymbol + "b"; // Aqua
        case 12: return util.colorSymbol + "c"; // Red
        case 13: return util.colorSymbol + "d"; // Light Purple
        case 14: return util.colorSymbol + "e"; // Yellow
        case 15: return util.colorSymbol + "f"; // White
        default: return util.colorSymbol + "7"; // Default to Gray
    }
}
