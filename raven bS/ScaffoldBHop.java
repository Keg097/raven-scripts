void onLoad() {
    modules.registerDescription("======Made By @key.97======");
    modules.registerButton("Disable BHop while scaffolding", false);
    modules.registerButton("Fast on Bhop", false);}

void onPreMotion(PlayerState state) {
    if (modules.getButton(scriptName, "Disable BHop while scaffolding") && modules.isEnabled("Scaffold")) {
        if (modules.isEnabled("BHop")) {
            modules.disable("BHop");
        }
    }

    if (modules.isEnabled("Scaffold") && !modules.isEnabled("BHop")) {
        modules.setButton("BHop", "Enabled", false);
    }

    if (modules.getButton(scriptName, "Fast on Bhop") && modules.isEnabled("BHop")) {
        modules.setSlider("Scaffold", "Fast scaffold", 4.0);
    } else {
        modules.setSlider("Scaffold", "Fast scaffold", 0.0);
    }
}

