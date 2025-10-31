package org.shotrush.atom.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import org.shotrush.atom.Atom;

@CommandAlias("wrench")
@Description("Get a wrench tool")
public class WrenchCommand extends BaseCommand {

    @Default
    @CommandPermission("atom.wrench")
    public void onWrench(Player player) {
        Atom.getInstance().getBlockManager().giveWrench(player);
    }
}
