package ua.vdev.clanshopaddon.config;

import ua.vdev.primeclans.addon.AbstractAddon;

public class ShopConfig {

    private final AbstractAddon addon;

    public ShopConfig(AbstractAddon addon) {
        this.addon = addon;
    }

    public void reload() {
        addon.reloadConfig();
    }

    public String getMessage(String key) {
        return addon.getConfig().getString("messages." + key, "<red>Сообщение не найдено: " + key);
    }
}