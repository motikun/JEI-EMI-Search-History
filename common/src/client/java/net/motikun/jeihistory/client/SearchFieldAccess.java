package net.motikun.jeihistory.client;

import net.minecraft.client.renderer.Rect2i;

/** Bounds bridge for either recipe viewer; shared code never loads JEI or EMI classes. */
public interface SearchFieldAccess {
    Rect2i jeihistory$getArea();
}
