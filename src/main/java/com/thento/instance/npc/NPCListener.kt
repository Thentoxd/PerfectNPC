package com.thento.instance.npc

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.ChatColor
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class NPCListener() : Listener {

    fun inject(player: Player) {
        val handler = object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, rawPacket: Any?) {
                if(rawPacket is ServerboundInteractPacket) {
                    val type = rawPacket.javaClass.getDeclaredField("b")
                    type.isAccessible = true
                    val typeData = type.get(rawPacket)
                    if(typeData.toString().contains("${'$'}e")) return

                    try {
                        val hand = typeData.javaClass.getDeclaredField("a")
                        hand.isAccessible = true
                        if(!hand.get(typeData).toString().equals("MAIN_HAND", ignoreCase = true)) return
                    } catch (exception: NoSuchFieldException) {

                    }

                    val id = rawPacket.javaClass.getDeclaredField("a")
                    id.isAccessible = true
                    val entityID = id.getInt(rawPacket)
                    player.sendMessage("Clicked on Entity ID = ${entityID}!")
                    // Bukkit.getPluginManager().callEvent(NPCInteractEvent(player, entityID))
                }

                super.channelRead(ctx, rawPacket)
            }
        }

        val pipeline = (player as CraftPlayer).handle.connection.getConnection().channel.pipeline()
        pipeline.addBefore("packet_handler", player.name, handler)
    }

    fun uninject(player: Player) {
        val channel = (player as CraftPlayer).handle.connection.getConnection().channel
        channel.eventLoop().submit {
            channel.pipeline().remove(player.name)
            return@submit
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        inject(event.player)
    }

    @EventHandler
    fun onJoin(event: PlayerQuitEvent) {
        uninject(event.player)
    }
}