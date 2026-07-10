package com.krylo.smp.listeners;

import com.krylo.smp.KryloSMP;
import com.krylo.smp.commands.RtpCommand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

/**
 * Listens for players stepping on the gold pressure plate
 * at spawn to trigger Random Teleport.
 */
public class SpawnInteractionListener implements Listener {

    private final KryloSMP plugin;
    private final RtpCommand rtpCommand;

    public SpawnInteractionListener(KryloSMP plugin, RtpCommand rtpCommand) {
        this.plugin = plugin;
        this.rtpCommand = rtpCommand;
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        // Only trigger on gold pressure plates (our RTP pad)
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) return;

        // Check if this pressure plate has the krylo_rtp tag via metadata
        // We identify it by checking if there's a diamond block 2 below it
        // (the RTP pad has: diamond_block at Y, gold plate at Y+1)
        Block below = block.getRelative(BlockFace.DOWN);
        Block belowBelow = below.getRelative(BlockFace.DOWN);

        if (below.getType() == Material.GOLD_BLOCK ||
            belowBelow.getType() == Material.GOLD_BLOCK) {
            Player player = event.getPlayer();

            // Trigger the RTP command
            rtpCommand.onCommand(player, null, "rtp", new String[]{});
        }
    }
}
