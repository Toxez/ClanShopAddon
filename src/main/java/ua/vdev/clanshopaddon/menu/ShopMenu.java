package ua.vdev.clanshopaddon.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.util.TextColor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShopMenu implements Menu {

    private final Clan clan;
    private final File dataFolder;
    private final Map<Integer, List<MenuAction>> leftActions  = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    public ShopMenu(Clan clan, File dataFolder) {
        this.clan = clan;
        this.dataFolder = dataFolder;
    }

    @Override
    public void open(Player player) {
        YamlConfiguration config = loadConfig();
        Optional.ofNullable(config.getConfigurationSection("menu"))
                .ifPresent(section -> {
                    Map<String, String> placeholders = buildPlaceholders(player);
                    Inventory inventory = createInventory(section, placeholders);
                    leftActions.clear();
                    rightActions.clear();
                    Map<String, Object> context = Map.of(
                            "clan_name", clan.name(),
                            "placeholders", placeholders
                    );

                    MenuHelper.loadMenuItems(inventory, section, placeholders, context, leftActions, rightActions);
                    player.openInventory(inventory);
                });
    }

    private Inventory createInventory(ConfigurationSection section, Map<String, String> placeholders) {
        String title = section.getString("title", "Клановый магазин");
        int size   = section.getInt("size", 54);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders));
    }

    private Map<String, String> buildPlaceholders(Player player) {
        EconomyManager eco = PrimeClans.getInstance().getEconomyManager();
        Clan freshClan = PrimeClans.getInstance().getClanManager()
                .getClan(clan.name())
                .orElse(clan);

        Map<String, String> map = new HashMap<>();
        map.put("clan_name", freshClan.name());
        map.put("clan_level", String.valueOf(freshClan.level()));
        map.put("clan_balance", eco.format(freshClan.balance()));
        map.put("clan_members_current", String.valueOf(freshClan.members().size()));
        map.put("clan_members_max", String.valueOf(freshClan.getMaxMembers()));
        map.put("player_name", player.getName());
        map.put("player_balance", eco.format(eco.getBalance(player)));
        return map;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Map<Integer, List<MenuAction>> actionsMap = event.getClick().isLeftClick()
                ? leftActions
                : event.getClick().isRightClick() ? rightActions : null;

        Optional.ofNullable(actionsMap)
                .map(m -> m.get(slot))
                .ifPresent(actions -> actions.forEach(a -> a.execute(player)));
    }

    private YamlConfiguration loadConfig() {
        return YamlConfiguration.loadConfiguration(new File(dataFolder, "menu/clan-shop.yml"));
    }

    @Override
    public String getId() { return "clan-shop"; }
}