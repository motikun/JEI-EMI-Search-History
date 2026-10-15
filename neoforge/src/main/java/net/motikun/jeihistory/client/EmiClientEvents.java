package net.motikun.jeihistory.client;

import net.motikun.jeihistory.Jeihistory;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** All referenced types are present without either optional recipe viewer installed. */
@EventBusSubscriber(modid = Jeihistory.MODID, value = Dist.CLIENT)
public final class EmiClientEvents {
    private static final HistoryInput INPUT = new HistoryInput();

    private EmiClientEvents() {}

    private static boolean active() {
        return HistoryController.focusedField() instanceof EmiSearchAccess field && field.jeihistory$isEnabled();
    }

    // EMI's Mouse/Keyboard mixins run before ScreenEvent.Pre. These shared entry points
    // let its screen manager and direct search-widget calls consume history first.
    public static boolean handleKeyPressed(int keyCode, int scanCode) {
        return active() && INPUT.keyPressed(keyCode, scanCode);
    }

    public static boolean handleMousePressed(Screen screen, double x, double y, int button) {
        return active() && INPUT.mousePressed(screen, x, y, button);
    }

    public static boolean handleMouseReleased(Screen screen, double x, double y, int button) {
        return INPUT.mouseReleased(screen, x, y, button);
    }

    public static boolean handleMouseDragged(double x, double y, int button) {
        return active() && INPUT.mouseDragged(x, y, button);
    }

    public static boolean handleMouseScrolled(double x, double y, double delta) {
        return active() && INPUT.scroll(x, y, delta);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void keyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (handleKeyPressed(event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (handleMousePressed(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (handleMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (handleMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (handleMouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void render(ScreenEvent.Render.Post event) {
        if (active()) {
            HistoryController.draw(HistoryController.focusedField(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void screenClosing(ScreenEvent.Closing event) {
        INPUT.reset();
    }
}
