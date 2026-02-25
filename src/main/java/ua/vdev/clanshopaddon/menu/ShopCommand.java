package ua.vdev.clanshopaddon.menu;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.api.AddonAPI;
import ua.vdev.primeclans.api.command.AddonSubCommand;
import ua.vdev.vlibapi.player.PlayerMsg;
import java.util.List;

public class ShopCommand implements AddonSubCommand {

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public boolean requiresClan() {
        return true;
    }

    @Override
    public void execute(Player player, String[] args) {
        AddonAPI.getClanProvider()
                .getPlayerClan(player.getUniqueId())
                .ifPresentOrElse(
                        clan -> AddonAPI.openMenu(player, "clan-shop", clan),
                        () -> PlayerMsg.send(player, "<red>Вы не состоите в клане!")
                );
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }
}