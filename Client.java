void onLoad() {
    modules.registerDescription("> Theme");
    modules.registerButton("Show Preview", true);
    modules.registerSlider("Color 1 - Red", "", 255, 0, 255, 1);
    modules.registerSlider("Color 1 - Green", "", 0, 0, 255, 1);
    modules.registerSlider("Color 1 - Blue", "", 0, 0, 255, 1);
    modules.registerDescription("");
    modules.registerSlider("Color 2 - Red", "", 255, 0, 255, 1);
    modules.registerSlider("Color 2 - Green", "", 255, 0, 255, 1);
    modules.registerSlider("Color 2 - Blue", "", 255, 0, 255, 1);
    modules.registerDescription("");
    modules.registerSlider("Background Alpha", "", 70, 0, 255, 5);
    modules.registerSlider("Mode", "", 0, new String[]{"Static", util.color("&cR&6a&ei&an&bb&do&5w"), util.color("&4G&cr&5a&bd&3i&9e&1n&1t")});
    modules.registerSlider("Wave Speed", "s", 5, 0.1, 10, 0.1);
    modules.registerDescription("Made by @desiyn");
    modules.registerDescription("Edited by @.key97");
}

Color startColor, endColor, staticColor;
int colorMode;

void onRenderTick(float partialTicks) {
    if (client.getScreen().equals("GuiRaven")) {
        updateColors();
        if (modules.getButton(scriptName, "Show Preview")) {
            renderColorPreviews();
        }
    }
}

void updateColors() {
    colorMode = (int) modules.getSlider(scriptName, "Mode");
    
    int color1Red = (int) modules.getSlider(scriptName, "Color 1 - Red");
    int color1Green = (int) modules.getSlider(scriptName, "Color 1 - Green");
    int color1Blue = (int) modules.getSlider(scriptName, "Color 1 - Blue");
    int color2Red = (int) modules.getSlider(scriptName, "Color 2 - Red");
    int color2Green = (int) modules.getSlider(scriptName, "Color 2 - Green");
    int color2Blue = (int) modules.getSlider(scriptName, "Color 2 - Blue");

    staticColor = new Color(color1Red, color1Green, color1Blue);

    endColor = new Color(color2Red, color2Green, color2Blue);
    
    if (colorMode == 2) {
        startColor = staticColor;
    }
}

void renderColorPreviews() {
    if (!client.getScreen().equals("GuiRaven")) return;
    
    int[] displaySize = client.getDisplaySize();
    int screenWidth = displaySize[0];
    int rectY = 20;
    int rectSize = 50;

    render.roundedRect(screenWidth / 2 - rectSize - 10, rectY, 
                      screenWidth / 2 - 10, rectY + rectSize, 
                      10, staticColor.getRGB());

    render.roundedRect(screenWidth / 2 + 10, rectY,
                      screenWidth / 2 + rectSize + 10, rectY + rectSize,
                      10, endColor.getRGB());
}
