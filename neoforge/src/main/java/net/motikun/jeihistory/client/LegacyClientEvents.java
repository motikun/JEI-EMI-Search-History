package net.motikun.jeihistory.client;

import mezz.jei.common.Internal;
import net.motikun.jeihistory.Jeihistory;
import net.motikun.jeihistory.JeiCompatibility;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Jeihistory.MODID, value = Dist.CLIENT)
public final class LegacyClientEvents {
    private LegacyClientEvents() {}

    private static LegacyHistoryInput input() {
        return LegacyRuntime.INPUT;
    }

    private static boolean active() {
        return JeiCompatibility.hasJei() && !(HistoryController.focusedField() instanceof EmiSearchAccess)
                && LegacyRuntime.hasKeyboardFocus();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void keyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (active() && input().keyPressed(event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (active() && input().mousePressed(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (JeiCompatibility.hasJei()
                && input().mouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (active() && input().mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (active() && input().scroll(event.getMouseX(), event.getMouseY(), event.getScrollDeltaY())) {
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
    public static void tick(ClientTickEvent.Post event) {
        if (JeiCompatibility.hasJei() && !(HistoryController.focusedField() instanceof EmiSearchAccess)) {
            input().updateVisibility(active());
        }
    }

    @SubscribeEvent
    public static void screenClosing(ScreenEvent.Closing event) {
        if (JeiCompatibility.hasJei()) {
            input().reset();
        }
    }

    private static final class LegacyRuntime {
        private static final LegacyHistoryInput INPUT = new LegacyHistoryInput();

        private static boolean hasKeyboardFocus() {
            if (HistoryController.focusedField() == null) {
                return false;
            }
            try {
                return Internal.getJeiRuntime().getIngredientListOverlay().hasKeyboardFocus();
            } catch (IllegalStateException unavailable) {
                // Older JEI has no optional runtime accessor; it throws before/after runtime availability.
                return false;
            }
        }
    }
}
