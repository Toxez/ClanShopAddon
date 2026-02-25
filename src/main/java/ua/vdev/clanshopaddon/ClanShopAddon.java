package ua.vdev.clanshopaddon;

import ua.vdev.clanshopaddon.action.ShopActions;
import ua.vdev.clanshopaddon.config.ShopConfig;
import ua.vdev.clanshopaddon.menu.ShopCommand;
import ua.vdev.clanshopaddon.menu.ShopMenu;
import ua.vdev.primeclans.addon.AbstractAddon;
import ua.vdev.primeclans.api.AddonAPI;

public class ClanShopAddon extends AbstractAddon {

    private ShopActions shopActions;

    @Override
    protected void onEnable() {
        saveResource("config.yml", false);
        saveResource("menu/clan-shop.yml", false);
        ShopConfig shopConfig = new ShopConfig(this);
        shopActions = new ShopActions(shopConfig).withDataFolder(getDataFolder());
        shopActions.register();

        AddonAPI.registerMenu("clan-shop", clan -> new ShopMenu(clan, getDataFolder()));
        AddonAPI.registerSubCommand(new ShopCommand());
        AddonAPI.onLevelUp(e -> getLogger().info("Клан " + e.clan().name() + " достиг " + e.newLevel() + " уровня!")
        );

        getLogger().info("ClanShopAddon v" + getDescription().version() + " включён");
    }

    @Override
    protected void onDisable() {
        if (shopActions != null) shopActions.unregister();
        AddonAPI.unregisterMenu("clan-shop");
        AddonAPI.unregisterSubCommand("shop");
        getLogger().info("ClanShopAddon выключен");
    }
}