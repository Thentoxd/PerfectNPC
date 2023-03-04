package com.thento.instance.npc

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.datafixers.util.Pair
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.ChatColor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

interface PacketDuplexHandler {
    fun onChannelRead(player: Player,rawPacket: Any?): Boolean {
        return true
    }

    fun inject(player: Player) {
        val handler = object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, rawPacket: Any?) {
                if(onChannelRead(player, rawPacket)) {
                    super.channelRead(ctx, rawPacket)
                    return
                }
            }
        }

        val pipeline = (player as CraftPlayer).handle.connection.getConnection().channel.pipeline()
        uninject(player)
        pipeline.addBefore("packet_handler", player.name, handler)
    }

    fun uninject(player: Player) {
        val channel = (player as CraftPlayer).handle.connection.getConnection().channel
        channel.eventLoop().submit {
            channel.pipeline().remove(player.name)
            return@submit
        }
    }
}

open class NPC(var player: Player, name: String, var location: Location): PacketDuplexHandler {
    private var npc: ServerPlayer? = null
    private var profile: GameProfile? = null
    private var skinProperty: Property? = null

    var isGlowing: Boolean = false

    init {
        val serverPlayer: ServerPlayer = ((player as CraftPlayer).handle)
        val server: MinecraftServer = serverPlayer.server
        val level: ServerLevel = serverPlayer.getLevel()
        val profile: GameProfile = GameProfile(UUID.randomUUID(), name)
        this.profile = profile

        val npc = ServerPlayer(server, level, profile, null)
        this.npc = npc
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc))
        this.sendPacket(ClientboundAddPlayerPacket(npc))

        teleport(location)
    }

    fun injectAll() {
        for(player in Bukkit.getOnlinePlayers()) {
            inject(player)
        }
    }

    fun setSkin(value: String, signature: String) {
        val property = Property("textures", value, signature)
        profile!!.properties.put("textures", property)
        this.skinProperty = property

        update()
    }

    private fun sendPacket(packet: Packet<*>) {
        for(player in Bukkit.getOnlinePlayers()) {
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

    fun teleport(location: Location) {
        npc!!.setPos(location.x, location.y, location.z)
        update()
    }

    private fun update() {
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc))
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc))
        this.sendPacket(ClientboundAddPlayerPacket(npc))
    }

    fun lookAtPoint(location: Location) {
        val eyeLocation: Location = this.location
        var yaw = Math.toDegrees(Math.atan2(location.z - eyeLocation.z, location.x - eyeLocation.x)).toFloat() - 90
        yaw = (yaw + ceil((-yaw / 360).toDouble()) * 360).toFloat()
        val deltaXZ = sqrt(Math.pow(eyeLocation.x - location.x, 2.0) + (eyeLocation.z - location.z).pow(2.0)).toFloat()
        var pitch = Math.toDegrees(Math.atan2(deltaXZ.toDouble(), location.y - eyeLocation.y)).toFloat() - 90
        pitch = (pitch + ceil((-pitch / 360).toDouble()) * 360).toFloat()
        this.rotateHead(pitch, yaw)
    }

    private fun rotateHead(pitch: Float, yaw: Float) {
        this.location.pitch = pitch
        this.location.yaw = yaw
        sendPacket(ClientboundRotateHeadPacket(npc, ((yaw%360)*256/360).toInt().toByte()))
        sendPacket(ClientboundMoveEntityPacket.Rot(getEntityID(), ((yaw%360)*256/360).toInt().toByte(), ((pitch%360)*256/360).toInt().toByte(), false))
    }

    fun lookAtPlayer(player: Player) {
        lookAtPoint(player.location)
    }

    fun isInRange(location: Location, range: Int): Boolean {
        if(this.location.distance(location) < range) {
            return true
        }

        return false
    }

    object SkinTextures {

        fun getByUsername(plugin: Plugin, name: String): Property {
            return if(Bukkit.getPlayer(name) == null) {
                val player = plugin.server.getOfflinePlayer(name)
                val entityPlayer = (player as CraftPlayer).handle

                entityPlayer.gameProfile.properties.get("textures").iterator().next()
            } else {
                getSkin(Bukkit.getPlayer(name)!!)!!
            }
        }

        fun getSkin(player: Player): Property? {
            try {
                return (player as CraftPlayer).handle.gameProfile.properties.get("textures").iterator().next()
            } catch (exception: NullPointerException) {
                Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}Couldn't set NPC")
            }
            return null
        }
    }

    fun walkTo(x: Double, y: Double, z: Double) {
        sendPacket(ClientboundMoveEntityPacket.Pos(getEntityID(), (x * 4096).toInt().toShort(), (y * 4096).toInt().toShort(), (z * 4096).toInt().toShort(), true))
    }

    fun setItemInMainHand(item: ItemStack) {
        sendPacket(ClientboundSetEquipmentPacket(getEntityID(), listOf(Pair(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(item)))))
    }

    fun setItem(slot: EquipmentSlot, item: ItemStack) {
        sendPacket(ClientboundSetEquipmentPacket(getEntityID(), listOf(Pair(slot, CraftItemStack.asNMSCopy(item)))))
    }

    private fun getEntityID(): Int { return npc!!.bukkitEntity.entityId }
    fun getSkin(): Property { return skinProperty!! }
    fun getSkinValue(): String { return getSkin().value }
    fun getSkinSignature(): String { return getSkin().signature }
}