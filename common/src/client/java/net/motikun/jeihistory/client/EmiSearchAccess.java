package net.motikun.jeihistory.client;

/** Marker and live visibility bridge; the interface itself is safe without EMI installed. */
public interface EmiSearchAccess extends SearchFieldAccess {
    boolean jeihistory$isEnabled();
}
