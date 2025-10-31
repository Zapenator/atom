package org.shotrush.atom.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.shotrush.atom.Atom;

@CommandAlias("removeblocks|removecogs")
@Description("Remove all custom blocks")
public class RemoveCogsCommand extends BaseCommand {

    @Default
    @CommandPermission("atom.removeblocks")
    public void onRemoveBlocks(CommandSender sender) {
        Atom.getInstance().getBlockManager().removeAllBlocks();
        sender.sendMessage("§aAll blocks have been removed!");
    }
}
