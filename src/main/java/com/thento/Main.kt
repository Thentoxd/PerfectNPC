package com.thento

import com.thento.instance.npc.NPC
import com.thento.instance.npc.NPCListener
import net.md_5.bungee.api.ChatColor
import net.minecraft.world.entity.EquipmentSlot
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin(), CommandExecutor, Listener {
    var npc: NPC? = null

    override fun onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(NPCListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(command.name.equals("npc", ignoreCase = true)) {
                val npc = NPC(sender, "Billy Bob", sender.location)
                npc.setSkin(
                    "ewogICJ0aW1lc3RhbXAiIDogMTY2NjYwNjUwNjA1NywKICAicHJvZmlsZUlkIiA6ICI2NDg4MzMwOGZhNGI0MWU2YjY2ZWQ2NGQwNjBhMTJkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVudG8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTIyNzlhYTdlNDUwZDJiZTQ2MjI4MzkwYWU2YTU4NTBmZTI3YTZiOTc5YTViYmJkMTE1NDc1NWNjZmRiYWMwOCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "BgY3Nk8Dr73ldMOhyLVpxFMsTgjMlPm3COAFFlXlfQVsoIE4w0ErcOogGU5+QtcAUAltMaeoK8KXurc0zRCVrALY27Uxw8fdeY3zo+FGeEE7ofEOYgpJeXCdvsWCB+/ceWmYj1Tl9tonR21Q3HlJvCgYH+otlhG3qCmkkG/zusas+0btwIVwmWmrGRiPD1xV8TAv0arPtzNvFPsyVYXzy+Z1SYu4PwR9w7no2did4ERjT3V80SWxlDP2yOVEeOTBKZgYQdbDNULnIT3slbwY0BfuK5qbbVI1URsKZqaH2VWkwqTB65S7D5dlLK/+VPzGZ6qZukHfaYmRgu7GZK+jEh/pS8U8cPrx1BuERpEfYghFez+p/0HLeBlJTjWTqErunMRqwlG/vM/30zaCxlqPBcMibO020MOFSS9Ci9MRhDYoFJsiw2Pig7kkvFCWHXQGUYo9JXpChzwmT7i83hQwZZCyY/koBdlnuU0Xnyi3qqF5KRyFYeH2VzxdZDmpPJqKh0K3wGfeOB7i6ZmK80endQDe+X849iz+2mc6b+1BQ9uC3iiW7Uu2PxeKl2zSyA77O20F7rzsYWkTKjOgu/rsnJIODOWEw+QgJc+PUVU1pEZ6B4smDFa4aUi3xXYWWdm7eCoOGvJ4y2Zc9iaBRuX0VwR3B3MBF2hEE3L/GB1KGr8="
                )
                npc.setItem(EquipmentSlot.CHEST, ItemStack(Material.DIAMOND_CHESTPLATE))

                this.npc = npc
            }
        }
        return false
    }
}