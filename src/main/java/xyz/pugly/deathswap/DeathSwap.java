package xyz.pugly.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class DeathSwap extends JavaPlugin implements CommandExecutor, Listener {

    int time;
    double startTime = 300;
    boolean toggle;

    @Override
    public void onEnable() {
        // Plugin startup logic
        time = (int) startTime;
        toggle = false;
        BossBar bar = Bukkit.createBossBar("\u00a7c\u00a7lDeath Swap", BarColor.RED, BarStyle.SOLID);
        bar.setProgress(1.0);
        bar.setVisible(true);

        Bukkit.getServer().getPluginCommand("toggle").setExecutor(new command());
        Bukkit.getPluginManager().registerEvents(new events(), this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            if (toggle) {

                for (Player p : Bukkit.getOnlinePlayers()) {
                    bar.addPlayer(p);
                }

                String timer = ((time % 60) < 10) ? ("0" + (time % 60)): ("" + time % 60);
                bar.setTitle("\u00a7c\u00a7lDeath Swap \u00a7f- \u00a7c" + (time / 60) + ":" + timer);

                bar.setProgress(time / startTime);

                if (time == 0) {
                    time = (int) startTime;

                    if (Bukkit.getOnlinePlayers().size() > 1) {
                        ArrayList<Player> players =  new ArrayList<>(Bukkit.getOnlinePlayers());

                        for (int i = 0; i < players.size(); i++) {
                            if (!players.get(i).getGameMode().equals(GameMode.SURVIVAL)) {
                                players.remove(i);
                                i--;
                            }
                        }

                        Collections.shuffle(players);

                        Location temp = players.get(0).getLocation();
                        for (int i = 0; i < players.size() - 1; i++) {
                            players.get(i).teleport(players.get(i + 1).getLocation());
                            players.get(i).sendMessage("\u00a7cTeleporting to a random player...");
                        }
                        players.get(players.size() - 1).teleport(temp);
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage("\u00a7cNot enough players.");
                        }
                    }
                }

                if (time == 3) {
                    Bukkit.broadcastMessage("\u00a7cTeleporting in 3 seconds...");
                }
                else if (time == 2) {
                    Bukkit.broadcastMessage("\u00a7cTeleporting in 2 seconds...");
                }
                else if (time == 1) {
                    Bukkit.broadcastMessage("\u00a7cTeleporting in 1 second...");
                }

                time -= 1;
            } else {
                bar.setProgress(1);
                bar.setTitle("\u00a7c\u00a7lDeath Swap \u00a7f- \u00a7cDisabled");
            }
        }, 0L, 20L);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private class command implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (toggle) {
                sender.sendMessage("\u00a7cDeath Swap toggled off.");
                time = (int) startTime;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.teleport(p.getLocation().toHighestLocation().add(0,1,0));
                }
            } else {
                time = (int) startTime;
                Bukkit.broadcastMessage("\u00a7cDeath Swap started.");
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    Location start = new Location(p.getWorld(), Math.random()*100000-50000, 0, Math.random()*100000-50000);
                    start = start.toHighestLocation();

                    start.getBlock().setType(Material.BEDROCK);
                    start.add(0,1,0);

                    p.getWorld().setSpawnLocation(start);
                    p.getWorld().setTime(0);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.teleport(start);
                        resetPlayer(player);
                    }
                }
            }

            toggle = !toggle;
            return true;
        }
    }

    private class events implements Listener {

        @EventHandler
        public void onDeath(PlayerDeathEvent e) {
            if (toggle) {
                Player p = e.getEntity();
                p.setGameMode(GameMode.SPECTATOR);
            }
        }

    }

    public void resetPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(10);
        player.setGameMode(GameMode.SURVIVAL);
    }
}
