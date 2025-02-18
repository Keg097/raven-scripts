boolean falling = false, air = false, killaura = false, rcing = false, ljing = false;
int jumpticks = 0, scaffoldTimer = 0;

void onLoad() {
    modules.registerDescription("Made By @notanundercoveragent.");
    modules.registerDescription("Edited By @.key97");
    modules.registerButton("Off on scaffold", true);
    modules.registerSlider("Fall Distance", " Blocks", 3, 1, 8, 1);
    modules.registerSlider("Scaffold Delay", " Seconds", 5, 1, 10, 1);
}

void onPreUpdate() {
    Entity player = client.getPlayer();
    double pitch = player.getPitch();
    int dist = fallDistance();
    double blinkDist = modules.getSlider(scriptName, "Fall Distance");

    if (scaffoldTimer > 0) scaffoldTimer--;

    if (player.onGround()) ljing = true;
    if (!modules.isEnabled("FBFly") && !modules.isEnabled("FBFly") && jumpticks-- <= 0 && !client.isFlying() && scaffoldDisable() == false && dist == -1 && !falling && !player.onGround() && modules.getKillAuraTarget() == null) {
        falling = true;
        killaura = modules.isEnabled("KillAura");
        modules.disable("KillAura");
        modules.enable("Blink");
    } else if (falling && player.getFallDistance() > blinkDist && dist == -1 && !air) {
        Vec3 pos = player.getPosition();
        air = true;
        client.sendPacketNoEvent(new C03(new Vec3(pos.x, -420, pos.z), false));
        modules.disable("Blink");
    } else if (falling && (player.onGround() || dist != -1 || modules.getKillAuraTarget() != null)) {
        if (killaura) modules.enable("KillAura");
        falling = air = killaura = false;
        modules.disable("Blink");
    }
}

void onPostPlayerInput() {
    rcing = keybinds.isPressed("use");
    if (keybinds.isPressed("jump")) jumpticks = 0;
    if (keybinds.isKeyDown(45)) ljing = true;
}

boolean scaffoldDisable() {
    int delay = (int) (modules.getSlider(scriptName, "Scaffold Delay") * 20);

    if (modules.getButton(scriptName, "Off on scaffold") == true) {
        if (modules.isEnabled("Scaffold")) {
            scaffoldTimer = delay;
            return true;
        } else if (scaffoldTimer > 0) {
            return true;
        }
    }
    return false;
}

int fallDistance() {
    int fallDist = -1;
    Vec3 pos = client.getPlayer().getPosition();
    int y = (int) Math.floor(pos.y) - 1;

    for (int i = y; i > -1; i--) {
        Block block = world.getBlockAt((int) Math.floor(pos.x), i, (int) Math.floor(pos.z));

        if (block.name.equals("air") || 
            block.name.contains("sign") || 
            block.name.equals("water") || 
            block.name.equals("lava") || 
            block.name.equals("slime_block") || 
            block.name.equals("honey_block")) {
            continue;
        }

        fallDist = y - i;
        break;
    }
    return fallDist;
}

