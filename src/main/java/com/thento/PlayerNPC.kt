package com.thento

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.datafixers.util.Pair
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt


abstract class PlayerNPC(var name: String, var location: Location) {
    protected var profile: GameProfile = GameProfile(UUID.randomUUID(), name)
    protected var npc: ServerPlayer = ServerPlayer((Bukkit.getServer() as CraftServer).server, (Bukkit.getServer() as CraftServer).server.allLevels.toMutableList()[0], profile, null)
    protected var skinProperty: Property? = null
    var ping = Ping.FIVE_BARS
    var hasSpawned = false

    init {
        teleport(location)
    }

    abstract fun onPlayerInteractPacket(player: Player)

    fun spawn(isViewable: Boolean): Boolean {
        if(!hasSpawned && isViewable) {
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc))
            this.sendPacket(ClientboundAddPlayerPacket(npc))

            hasSpawned = true
            return true
        }

        update()
        return !(hasSpawned)
    }

    fun remove() {
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc))
        hasSpawned = false
    }

    fun setSkin(value: String, signature: String) {
        val property = Property("textures", value, signature)
        profile.properties.put("textures", property)
        this.skinProperty = property

        update()
    }

    fun setSkin(playerName: String) {
        try {
            val textureProperty = JsonParser().parse(InputStreamReader(
                URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/${JsonParser().parse(InputStreamReader(URL("https://api.mojang.com/users/profiles/minecraft/$playerName").openStream())).asJsonObject["id"].asString}?unsigned=false"
                ).openStream())).asJsonObject["properties"].asJsonArray[0].asJsonObject

            setSkin(textureProperty["value"].asString, textureProperty["signature"].asString)
        } catch (exception: Exception) {
            return
        }
    }

    private fun sendPacket(packet: Packet<*>) {
        for(player in Bukkit.getOnlinePlayers()) {
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

    fun updatePing(ping: Ping) {
        this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, npc))

        this.ping = ping
    }

    fun teleport(location: Location) {
        npc!!.setPos(location.x, location.y, location.z)
        update()
    }

    private fun update() {
        if(hasSpawned) {
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc))
            this.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc))
            this.sendPacket(ClientboundAddPlayerPacket(npc))
        }
    }

    fun lookAtPoint(location: Location): Boolean {
        if(location.world != this.location.world) {
            return false
        }

        val eyeLocation: Location = this.location
        var yaw = Math.toDegrees(Math.atan2(location.z - eyeLocation.z, location.x - eyeLocation.x)).toFloat() - 90
        yaw = (yaw + ceil((-yaw / 360).toDouble()) * 360).toFloat()
        val deltaXZ = sqrt(Math.pow(eyeLocation.x - location.x, 2.0) + (eyeLocation.z - location.z).pow(2.0)).toFloat()
        var pitch = Math.toDegrees(Math.atan2(deltaXZ.toDouble(), location.y - eyeLocation.y)).toFloat() - 90
        pitch = (pitch + ceil((-pitch / 360).toDouble()) * 360).toFloat()
        this.rotateHead(pitch, yaw)

        return true
    }

    private fun rotateHead(pitch: Float, yaw: Float) {
        this.location.pitch = pitch
        this.location.yaw = yaw
        sendPacket(ClientboundRotateHeadPacket(npc, ((yaw%360)*256/360).toInt().toByte()))
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
        npc.pose = pose
        update()
    }

    fun getEntityID(): Int { return npc.bukkitEntity.entityId }
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

class NPCInteractEvent(var player: Player, var entityID: Int, var hand: Hand): Event(false) {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()


        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}