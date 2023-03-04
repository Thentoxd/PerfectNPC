package com.thento

import com.thento.instance.npc.NPC
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin(), CommandExecutor, Listener {
    var npc: NPC? = null

    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(command.name.equals("npc", ignoreCase = true)) {
                val npc = object : NPC(sender, "Billy Bob", sender.location) {
                    override fun onChannelRead(player: Player, rawPacket: Any?): Boolean {
                        return super.onChannelRead(player, rawPacket)
                    }
                }
                npc.injectAll()
                this.npc = npc
            }
        }
        return false
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if(npc!!.isInRange(event.player.location, 5)) {
            this.npc!!.lookAtPlayer(event.player)
        }
    }
}