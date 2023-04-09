package com.thento

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin


class NPCPlugin : JavaPlugin(), CommandExecutor, Listener {

    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(command.name.equals("npc", ignoreCase = true)) {
                val npc = NPC("Billy Bob", sender.location, true)
                npc.setSkin("Notch")
            }
        }
        return false
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val handler = object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, rawPacket: Any?) {
                if(rawPacket is ServerboundInteractPacket) {
                    val type = rawPacket.javaClass.getDeclaredField("b")
                    type.isAccessible = true
                    val typeData = type.get(rawPacket)
                    if(typeData.toString().contains("${'$'}e")) return
                    var handType = Hand.OffHand

                    try {
                        val hand = typeData.javaClass.getDeclaredField("a")
                        hand.isAccessible = true
                        if(!hand.get(typeData).toString().equals("MAIN_HAND", ignoreCase = true)) {
                            handType = Hand.Hand
                        }
                    } catch (_: NoSuchFieldException) {

                    }

                    val id = rawPacket.javaClass.getDeclaredField("a")
                    id.isAccessible = true

                    Bukkit.getPluginManager().callEvent(NPCInteractEvent(player, id.getInt(rawPacket), handType))
                }

                super.channelRead(ctx, rawPacket)
            }
        }

        val pipeline = (player as CraftPlayer).handle.connection.getConnection().channel.pipeline()
        pipeline.addBefore("packet_handler", player.name, handler)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val channel = (event.player as CraftPlayer).handle.connection.getConnection().channel
        channel.eventLoop().submit {
            channel.pipeline().remove(event.player.name)
            return@submit
        }
    }
}