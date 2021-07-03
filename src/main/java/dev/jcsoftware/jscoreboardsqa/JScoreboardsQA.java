package dev.jcsoftware.jscoreboardsqa;

import dev.jcsoftware.jscoreboards.JGlobalScoreboard;
import dev.jcsoftware.jscoreboards.JScoreboardOptions;
import dev.jcsoftware.jscoreboards.JScoreboardTabHealthStyle;
import dev.jcsoftware.jscoreboards.JScoreboardTeam;
import dev.jcsoftware.jscoreboards.versioning.SpigotAPIVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class JScoreboardsQA extends JavaPlugin {
  private JGlobalScoreboard scoreboard;
  private JScoreboardTeam taggedTeam;

  private final String serverVersion =
      Bukkit.getServer().getClass().getPackage().getName()
          .split("\\.")[3];

  private int explosionTimer = 30;

  private int cycleNumber = 0;
  private Map<Integer, ChatColor> remainderToColorMap = new HashMap<>();

  @Override
  public void onEnable() {
    super.onEnable();

    Bukkit.getLogger().info("QA Plugin running server version " + serverVersion);

    getServer().getScheduler().runTaskTimer(this, () -> {
      remainderToColorMap.clear();
      switch (cycleNumber) {
        case 0:
          remainderToColorMap.put(0, ChatColor.RED);
          remainderToColorMap.put(1, ChatColor.GREEN);
          remainderToColorMap.put(2, ChatColor.BLUE);
          break;
        case 1:
          remainderToColorMap.put(0, ChatColor.BLUE);
          remainderToColorMap.put(1, ChatColor.RED);
          remainderToColorMap.put(2, ChatColor.GREEN);
          break;
        case 2:
          remainderToColorMap.put(0, ChatColor.GREEN);
          remainderToColorMap.put(1, ChatColor.BLUE);
          remainderToColorMap.put(2, ChatColor.RED);
          break;
      }

      cycleNumber++;
      if (cycleNumber > 2) {
        cycleNumber = 0;
      }

      scoreboard.updateScoreboard();
    }, 0, 3);

    this.scoreboard = new JGlobalScoreboard(
        () -> "&4&lTNT&f&lTag",
        () -> {
          if (SpigotAPIVersion.getCurrent().lessThan(SpigotAPIVersion.v1_13)) {
            return Collections.singletonList("&eExplosion in &f" + explosionTimer);
          } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 32; i++) {
              ChatColor color = remainderToColorMap.getOrDefault(i % 3, ChatColor.RESET);
              stringBuilder.append("&").append(color.getChar()).append("\u2588");
            }

            List<String> lines = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
              lines.add(stringBuilder.toString());
            }
            return lines;
          }
        },
        new JScoreboardOptions(JScoreboardTabHealthStyle.NONE, true)
    );

    this.taggedTeam = this.scoreboard.createTeam("Tagged", "&c&lIT ", ChatColor.RED);

    getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
      explosionTimer--;
      if (explosionTimer < 0) {
        explosionTimer = 30;
      }

      for (Player player : Bukkit.getOnlinePlayers()) {
        this.scoreboard.addPlayer(player);
      }

      this.scoreboard.updateScoreboard();
    }, 0, 20);
  }

  @Override
  public void onDisable() {
    super.onDisable();

    scoreboard.destroy();

    getServer().getScheduler().cancelTasks(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;

    Player player = (Player) sender;

    if (label.equalsIgnoreCase("jointeam")) {
      taggedTeam.addPlayer(player);
    } else {
      taggedTeam.removePlayer(player);
    }

    return true;
  }
}
