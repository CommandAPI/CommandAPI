package dev.jorel.commandapi;

/**
 * This file handles loading the correct mappings information. The MojangMappedVersionHandler
 * file within the commandapi-core module may not be used at run time. Instead, if a platform module
 * wants to be mojang-mapped, they can replace it with this class where {@link #isMojangMapped()} returns {@code true}.
 */
public interface MojangMappedVersionHandler {
    static boolean isMojangMapped() {
        return true;
    }
}
