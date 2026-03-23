// src/main/java/com/team/flopkart/pattern/singleton/CartSessionManager.java
package com.team.flopkart.pattern.singleton;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         DESIGN PATTERN: SINGLETON (Creational)              ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║ Spring's @Component ensures only ONE instance of this class ║
 * ║ exists in the entire application context. Every class that  ║
 * ║ injects CartSessionManager gets the SAME object.            ║
 * ║                                                             ║
 * ║ Classic Singleton enforces this via a private constructor   ║
 * ║ + static getInstance(). Spring's IoC container achieves     ║
 * ║ the same guarantee through bean scope (default = singleton).║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Responsibility: Tracks which users currently have an active cart
 * session (i.e., have items in their cart). Used to show a cart
 * badge count in the navbar without hitting the DB on every request.
 */
@Component
public class CartSessionManager {

    /**
     * ConcurrentHashMap is used because multiple users can add/remove
     * items at the same time (thread-safe).
     *
     * Key   = userId
     * Value = number of items currently in their cart
     */
    private final Map<Long, Integer> activeCartSessions = new ConcurrentHashMap<>();

    /**
     * Called when a user adds/updates items in their cart.
     * Stores the latest item count for that user.
     */
    public void updateSession(Long userId, int itemCount) {
        if (itemCount <= 0) {
            activeCartSessions.remove(userId);
        } else {
            activeCartSessions.put(userId, itemCount);
        }
    }

    /**
     * Returns how many items are in a user's cart.
     * Returns 0 if user has no active session.
     * Used by the navbar to show the cart badge number.
     */
    public int getCartItemCount(Long userId) {
        return activeCartSessions.getOrDefault(userId, 0);
    }

    /**
     * Called when cart is cleared (after order placed, or manual clear).
     */
    public void clearSession(Long userId) {
        activeCartSessions.remove(userId);
    }

    /**
     * Returns an unmodifiable view of all active sessions.
     * Useful for admin/debug purposes.
     */
    public Map<Long, Integer> getAllActiveSessions() {
        return Collections.unmodifiableMap(activeCartSessions);
    }
}

// CartSessionManager.java
// Tracks active cart sessions (item count per user)
// OOAD: Singleton Pattern