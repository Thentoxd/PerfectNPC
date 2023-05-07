package com.thento

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.datafixers.util.Pair
import com.thento.testNPC.NPCPlugin
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt


abstract class PlayerNPC(var name: String, var location: Location, var ping: Ping) {

    protected var profile: GameProfile = GameProfile(UUID.randomUUID(), name)
    protected var serverPlayer: ServerPlayer = ServerPlayer((Bukkit.getServer() as CraftServer).server, (Bukkit.getServer() as CraftServer).server.allLevels.toMutableList()[0], profile, null)
    var skinProperty: Property? = null
    var hasSpawned: Boolean = false


    init {
        teleport(location)
    }

    abstract fun onPlayerInteract(player: Player)

    fun spawn(isViewable: Boolean): Boolean {
        if(!hasSpawned && isViewable) {
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer))
            this.sendPacket(ClientboundAddPlayerPacket(serverPlayer))

            hasSpawned = true
            return true
        }

        update()
        return !(hasSpawned)
    }

    fun setTab(value: Boolean) {
        if(!value) {
            sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer))
        } else {
            sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer))
        }
    }

    fun remove() {
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer))
        hasSpawned = false
    }

    fun setSkin(value: String, signature: String): Property {
        val property = Property("textures", value, signature)
        profile.properties.put("textures", property)
        this.skinProperty = property

        update()

        return property
    }

    fun setSkin(playerName: String): Property {
        return try {
            val textureProperty = JsonParser().parse(InputStreamReader(
                URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/${JsonParser().parse(InputStreamReader(URL("https://api.mojang.com/users/profiles/minecraft/$playerName").openStream())).asJsonObject["id"].asString}?unsigned=false"
                ).openStream())).asJsonObject["properties"].asJsonArray[0].asJsonObject

            setSkin(textureProperty["value"].asString, textureProperty["signature"].asString)
        } catch (exception: Exception) {
            setSkin("", "")
        }
    }

    private fun sendPacket(packet: Packet<*>) {
        for(player in Bukkit.getOnlinePlayers()) {
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

    fun updatePing(ping: Ping) {
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, serverPlayer))

        this.ping = ping
    }

    fun teleport(location: Location) {
        serverPlayer.setPos(location.x, location.y, location.z)
        update()
    }

    private fun update() {
        if(hasSpawned) {
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer))
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer))
            this.sendPacket(ClientboundAddPlayerPacket(serverPlayer))
        }
    }

    fun lookAtPoint(location: Location): Boolean {
        if(location.world != this.location.world) {
            return false
        }

        val eyeLocation: Location = this.location
        var yaw = Math.toDegrees(atan2(location.z - eyeLocation.z, location.x - eyeLocation.x)).toFloat() - 90
        yaw = (yaw + ceil((-yaw / 360).toDouble()) * 360).toFloat()
        var pitch = Math.toDegrees(atan2(sqrt((eyeLocation.x - location.x).pow(2.0) + (eyeLocation.z - location.z).pow(2.0)), location.y - eyeLocation.y)).toFloat() - 90
        pitch = (pitch + ceil((-pitch / 360).toDouble()) * 360).toFloat()

        this.rotateHead(pitch, yaw)

        return true
    }

    private fun rotateHead(pitch: Float, yaw: Float) {
        this.location.pitch = pitch
        this.location.yaw = yaw
        sendPacket(ClientboundRotateHeadPacket(serverPlayer, ((yaw%360)*256/360).toInt().toByte()))
        sendPacket(ClientboundMoveEntityPacket.Rot(getEntityID(), ((yaw%360)*256/360).toInt().toByte(), ((pitch%360)*256/360).toInt().toByte(), false))
    }

    fun talkTo(player: Player, message: String) {
        player.sendMessage("${name}: $message")
    }

    fun lookAtPlayer(player: Player) {
        lookAtPoint(player.location)
    }

    fun isInRange(location: Location, range: Int): Boolean {
        return this.location.distance(location) < range
    }

    fun setItemInMainHand(item: ItemStack) {
        sendPacket(ClientboundSetEquipmentPacket(getEntityID(), listOf(Pair(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(item)))))
    }

    fun setItem(slot: EquipmentSlot, item: ItemStack) {
        sendPacket(ClientboundSetEquipmentPacket(getEntityID(), listOf(Pair(slot, CraftItemStack.asNMSCopy(item)))))
    }

    fun setPose(pose: Pose) {
        serverPlayer.pose = pose
        update()
    }

    fun getEntityID(): Int { return serverPlayer.bukkitEntity.entityId }
    fun getSkin(): Property { return skinProperty!! }
}

enum class Hand {
    MainHand,
    OffHand
}

enum class Ping(val milliseconds: Int) {
    NO_CONNECTION(-1),
    ONE_BAR(1000),
    TWO_BARS(999),
    THREE_BARS(599),
    FOUR_BARS(299),
    FIVE_BARS(149)
}

fun Player.addPacketListener(plugin: NPCPlugin) {
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
                    if(!hand.get(typeData).toString().equals("MAIN_HAND", ignoreCase = true)) {
                        return
                    }
                } catch (_: NoSuchFieldException) {

                }

                val id = rawPacket.javaClass.getDeclaredField("a")
                id.isAccessible = true

                searchForPlayerNPC(plugin, id.getInt(rawPacket))!!.onPlayerInteract(player!!)
            }

            super.channelRead(ctx, rawPacket)
        }
    }

    val pipeline = (this as CraftPlayer).handle.connection.getConnection().channel.pipeline()
    pipeline.addBefore("packet_handler", name, handler)
}

internal fun searchForPlayerNPC(main: NPCPlugin, entityID: Int): PlayerNPC? {
    for(npc in main.npcs) {
        if(npc.getEntityID() == entityID) {
            return npc
        }
    }

    return null
}

fun Player.removePacketListener() {
    val channel = (this as CraftPlayer).handle.connection.getConnection().channel
    channel.eventLoop().submit {
        channel.pipeline().remove(name)
        return@submit
    }
}