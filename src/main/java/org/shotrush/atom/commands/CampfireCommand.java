package org.shotrush.atom.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import org.shotrush.atom.Atom;
import org.shotrush.atom.commands.annotation.AutoRegister;

@AutoRegister(priority = 32)
@CommandAlias("campfire")
@Description("Get campfire blocks")
public class CampfireCommand extends BaseCommand {

    @Default
    @CommandPermission("atom.campfire")
    public void onCampfire(Player player) {
        Atom.getInstance().getBlockManager().giveBlockItem(player, "campfire");
        player.sendMessage("§aGiven campfire!");
    }
}
