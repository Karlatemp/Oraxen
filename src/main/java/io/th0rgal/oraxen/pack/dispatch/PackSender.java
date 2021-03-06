package io.th0rgal.oraxen.pack.dispatch;

import io.th0rgal.oraxen.settings.Pack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PackSender implements Listener {

    private Set<UUID> packLoaded = new HashSet<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(PlayerJoinEvent event) {
        if ((boolean) Pack.SEND_WELCOME_MESSAGE.getValue())
            PackDispatcher.sendWelcomeMessage(event.getPlayer());
        if ((boolean) Pack.SEND_PACK.getValue()) {
            PackDispatcher.sendPack(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(PlayerResourcePackStatusEvent event) {

        Player player = event.getPlayer();

        switch (event.getStatus()) {
            case DECLINED:
                // todo: send a configurable alert message
                break;
            case FAILED_DOWNLOAD:
                if (!packLoaded.contains(player.getUniqueId())) {
                    packLoaded.add(player.getUniqueId());
                    PackDispatcher.sendPack(player);
                }
            case ACCEPTED:
                packLoaded.remove(player.getUniqueId());
                break;
        }

    }

}
