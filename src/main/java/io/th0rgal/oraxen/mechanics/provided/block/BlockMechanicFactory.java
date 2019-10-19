package io.th0rgal.oraxen.mechanics.provided.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.pack.ResourcePack;
import io.th0rgal.oraxen.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class BlockMechanicFactory extends MechanicFactory {

    private static List<JsonObject> mushroomStemBlockstateOverrides = new ArrayList<>();

    public BlockMechanicFactory(ConfigurationSection section) {
        super(section);
        ResourcePack.addModifiers(packFolder -> {
            File blockstatesFolder = new File(packFolder, "blockstates");
            if (!blockstatesFolder.exists())
                blockstatesFolder.mkdirs();
            File file = new File(blockstatesFolder, "mushroom_stem.json");
            Utils.writeStringToFile(file, getBlockstateContent());
        });
        MechanicsManager.registerListeners(OraxenPlugin.get(), new BlockMechanicsManager(this));
    }

    public static void addBlock(ConfigurationSection mechanicSection) {
        // todo: use the itemstack model if block model isn't set
        mushroomStemBlockstateOverrides.add(getBlockstateOverride(mechanicSection.getString("model"),
                mechanicSection.getInt("custom_variation")));
    }

    public static JsonObject getBlockstateOverride(String modelName, int when) {
        JsonObject content = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("model", modelName);
        content.add("apply", model);
        content.add("when", Utils.getBlockstateWhenFields(when));
        return content;
    }

    private String getBlockstateContent() {
        JsonObject mushroomStem = new JsonObject();
        JsonArray multipart = new JsonArray();
        //adds default override
        multipart.add(getBlockstateOverride("mushroom_stem", 15));
        for (JsonObject override : mushroomStemBlockstateOverrides)
            multipart.add(override);
        mushroomStem.add("multipart", multipart);
        return mushroomStem.toString();
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new BlockMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }

}

class BlockMechanicsManager implements Listener {

    private MechanicFactory factory;

    public BlockMechanicsManager(BlockMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPlacingCustomBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        String itemID = OraxenItems.getIdByItem(item);
        if (factory.isNotImplementedIn(itemID))
            return;

        Player player = event.getPlayer();
        Block placedAgainst = event.getClickedBlock();
        Block target;
        Material type = placedAgainst.getType();
        if (!type.equals(Material.SNOW)
                && !type.equals(Material.GRASS_BLOCK)
                && !type.equals(Material.VINE)
                && !type.equals(Material.TALL_GRASS))
            target = placedAgainst.getRelative(event.getBlockFace());
        else
            target = placedAgainst;

        BlockPlaceEvent blockBreakEvent = new BlockPlaceEvent(target, target.getState(), placedAgainst, item, player, true, event.getHand());
        Bukkit.getPluginManager().callEvent(blockBreakEvent);

        if (target.getLocation().distance(player.getLocation()) > 1 && target.getLocation().distance(player.getLocation()) > 1) {
            if (blockBreakEvent.canBuild() && !blockBreakEvent.isCancelled()) {

                event.setCancelled(true);
                target.setType(Material.BROWN_TERRACOTTA);
                if (!player.getGameMode().equals(GameMode.CREATIVE))
                    item.setAmount(item.getAmount() - 1);
            }
        }
    }

}

class BlockMechanic extends Mechanic {

    List<LinkedHashMap<String, Object>> loots;
    boolean defaultBreakAnimation;

    @SuppressWarnings("unchecked")
    public BlockMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        /* We give:
        - an instance of the Factory which created the mechanic
        - the section used to configure the mechanic
         */
        super(mechanicFactory, section);
        loots = (List<LinkedHashMap<String, Object>>) section.getList("loots");

        if (!section.isConfigurationSection("break_animation")) {
            defaultBreakAnimation = true;
        } else {
            ConfigurationSection breakAnimation = section.getConfigurationSection("break_animation");
            defaultBreakAnimation = !breakAnimation.isBoolean("default") || breakAnimation.getBoolean("default");
        }
    }

    public List<LinkedHashMap<String, Object>> getLoots() {
        return loots;
    }

    public boolean isDefaultBreakAnimation() {
        return defaultBreakAnimation;
    }
}