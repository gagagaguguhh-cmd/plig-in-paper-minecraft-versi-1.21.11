package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderManager {

    public record OrderListing(String id, UUID requesterUUID, String requesterName, ItemStack requestedItem, double rewardDollar, long createdTime) {}

    private final MedievalEconomyPlugin plugin;
    private File orderFile;
    private FileConfiguration orderConfig;
    private final List<OrderListing> orders = new ArrayList<>();

    public OrderManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        loadOrders();
    }

    public void loadOrders() {
        orderFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!orderFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                orderFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat file orders.yml: " + e.getMessage());
            }
        }
        orderConfig = YamlConfiguration.loadConfiguration(orderFile);

        orders.clear();
        for (String id : orderConfig.getKeys(false)) {
            try {
                UUID requesterUUID = UUID.fromString(orderConfig.getString(id + ".requester_uuid", ""));
                String requesterName = orderConfig.getString(id + ".requester_name", "Anonim");
                ItemStack requestedItem = orderConfig.getItemStack(id + ".requested_item");
                double reward = orderConfig.getDouble(id + ".reward_dollar");
                long time = orderConfig.getLong(id + ".created_time");

                if (requestedItem != null) {
                    orders.add(new OrderListing(id, requesterUUID, requesterName, requestedItem, reward, time));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal membaca order ID: " + id);
            }
        }
    }

    public void saveOrders() {
        for (String key : orderConfig.getKeys(false)) {
            orderConfig.set(key, null);
        }

        for (OrderListing order : orders) {
            String id = order.id();
            orderConfig.set(id + ".requester_uuid", order.requesterUUID().toString());
            orderConfig.set(id + ".requester_name", order.requesterName());
            orderConfig.set(id + ".requested_item", order.requestedItem());
            orderConfig.set(id + ".reward_dollar", order.rewardDollar());
            orderConfig.set(id + ".created_time", order.createdTime());
        }

        try {
            orderConfig.save(orderFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan file orders.yml: " + e.getMessage());
        }
    }

    public OrderListing createOrder(UUID requesterUUID, String requesterName, ItemStack item, double rewardDollar) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        OrderListing order = new OrderListing(id, requesterUUID, requesterName, item, rewardDollar, System.currentTimeMillis());
        orders.add(order);
        saveOrders();
        return order;
    }

    public boolean removeOrder(String id) {
        boolean removed = orders.removeIf(o -> o.id().equals(id));
        if (removed) {
            saveOrders();
        }
        return removed;
    }

    public List<OrderListing> getOrders() {
        return new ArrayList<>(orders);
    }
}
