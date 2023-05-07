package com.thento

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.world.entity.EquipmentSlot
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin


class NPCPlugin : JavaPlugin(), CommandExecutor, Listener {
    val nonPlayableCharacters: MutableList<PlayerNPC> = mutableListOf()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(command.name.equals("npc", ignoreCase = true)) {
                val npc = object : PlayerNPC("Billy Bob", sender.location) {
                    override fun onPlayerInteractPacket(player: Player) {
                        player.sendMessage("${this.ping.milliseconds}")
                    }
                }

                npc.setSkin("Thento")
                npc.setItem(EquipmentSlot.CHEST, ItemStack(Material.DIAMOND_CHESTPLATE))
                nonPlayableCharacters.add(npc)
                npc.spawn(true)
            }
        }
        return false
    }

    @EventHandler
    fun npcPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val handler = object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, rawPacket: Any?) {
                if(rawPacket is ServerboundInteractPacket) {
                    Bukkit.getConsoleSender().sendMessage(rawPacket.toString())

                    val type = rawPacket.javaClass.getDeclaredField("b")
                    type.isAccessible = true
                    val typeData = type.get(rawPacket)
                    if(typeData.toString().contains("${'$'}e")) return

                    try {
                        val hand = typeData.javaClass.getDeclaredField("a")
                        hand.isAccessible = true
                        if(!hand.get(typeData).toString().equals("MAIN_HAND", ignoreCase = true)) {
                            return
                        }
                    } catch (_: NoSuchFieldException) {

                    }

                    val id = rawPacket.javaClass.getDeclaredField("a")
                    id.isAccessible = true

                    val entityID = id.getInt(rawPacket)

                    player.sendMessage("$entityID")

                    for(npc in nonPlayableCharacters) {
                        if(npc.getEntityID() == entityID) {
                            npc.onPlayerInteractPacket(player)
                            break
                        }
                    }
                }

                super.channelRead(ctx, rawPacket)
            }
        }

        val pipeline = (player as CraftPlayer).handle.connection.getConnection().channel.pipeline()
        pipeline.addBefore("packet_handler", player.name, handler)
    }

    @EventHandler
    fun npcPlayerQuit(event: PlayerQuitEvent) {
        val channel = (event.player as CraftPlayer).handle.connection.getConnection().channel
        channel.eventLoop().submit {
            channel.pipeline().remove(event.player.name)
            return@submit
        }
    }

    @EventHandler
    fun onNPCClick(event: NPCInteractEvent) {
        val player = event.player

        Bukkit.broadcastMessage("wdawdawdadawdadadawd")

        if(event.hand == Hand.MainHand) {
            Bukkit.broadcastMessage("wdawdawdadawdadadawd")
        }
    }
}