package com.thento.testNPC

import com.thento.PlayerNPC
import com.thento.addPacketListener
import com.thento.removePacketListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin


class NPCPlugin : JavaPlugin(), CommandExecutor, Listener {
    val npcs: MutableList<PlayerNPC> = mutableListOf()
    val runnables: MutableList<Runnable> = mutableListOf()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(command.name.equals("npc", ignoreCase = true)) {
                val npc = MailMan(sender.location) // Test Custom NPC
                npc.spawn(true)

                npcs.add(npc)
            }
        }
        return false
    }

    @EventHandler
    fun npcPlayerJoin(event: PlayerJoinEvent) {
        event.player.addPacketListener(this)
    }

    @EventHandler
    fun npcPlayerQuit(event: PlayerQuitEvent) {
        event.player.removePacketListener()
    }
}