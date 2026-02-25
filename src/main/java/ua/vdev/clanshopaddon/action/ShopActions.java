package ua.vdev.clanshopaddon.action;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import ua.vdev.clanshopaddon.config.ShopConfig;
import ua.vdev.clanshopaddon.menu.ShopMenu;
import ua.vdev.primeclans.api.AddonAPI;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.MenuManager;

import java.io.File;

public class ShopActions {

    private final ShopConfig config;
    private File dataFolder;

    public ShopActions(ShopConfig config) {
        this.config = config;
    }

    public ShopActions withDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    public void register() {
        registerTakeMoney();
        registerGiveExp();
        registerPlayerBalancePlaceholder();
    }

    public void unregister() {
        AddonAPI.unregisterAction("[take-money]");
        AddonAPI.unregisterAction("[give-exp]");
        AddonAPI.unregisterPlaceholder("player_balance");
    }

    private void registerTakeMoney() {
        AddonAPI.registerAction("[take-money]", (player, arg, ctx) -> {
            double amount;
            try {
                amount = Double.parseDouble(arg.trim());
            } catch (NumberFormatException e) {
                send(player, config.getMessage("invalid-amount").replace("{arg}", arg));
                return;
            }

            EconomyManager eco = getEconomy();
            if (!eco.has(player, amount)) {
                send(player, config.getMessage("not-enough-money"));
                return;
            }

            eco.withdraw(player, amount);
            getClanManager().getPlayerClan(player.getUniqueId())
                    .ifPresent(clan ->
                            MenuManager.openMenu(player, new ShopMenu(clan, dataFolder))
                    );
        });
    }

    private void registerGiveExp() {
        AddonAPI.registerAction("[give-exp]", (player, arg, ctx) -> {
            long amount;
            try {
                amount = Long.parseLong(arg.trim());
            } catch (NumberFormatException e) {
                send(player, config.getMessage("invalid-exp").replace("{arg}", arg));
                return;
            }

            getClanManager().getPlayerClan(player.getUniqueId())
                    .ifPresentOrElse(
                            clan -> getClanManager().addExp(player.getUniqueId(), amount),
                            () -> send(player, config.getMessage("no-clan"))
                    );
        });
    }

    private void registerPlayerBalancePlaceholder() {
        AddonAPI.registerPlaceholder("player_balance", (clan, player) ->
                getEconomy().format(getEconomy().getBalance(player))
        );
    }

    private void send(Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    private EconomyManager getEconomy() {
        return ua.vdev.primeclans.PrimeClans.getInstance().getEconomyManager();
    }

    private ClanManager getClanManager() {
        return ua.vdev.primeclans.PrimeClans.getInstance().getClanManager();
    }
}