package com.thento

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.datafixers.util.Pair
import com.thento.testNPC.NPCPlugin
import com.thento.testNPC.getNPCS
import com.thento.testNPC.npcs
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.GameType
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.math.*

internal fun GameMode.getNMSValue(): GameType {
    return GameType.valueOf(this.name)
}

abstract class PlayerNPC(name: String, var location: Location) {

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

            Bukkit.getServer().getNPCS()[location.world!!] = this

            hasSpawned = true
            return true
        }

        update()
        return !(hasSpawned)
    }

    fun setGameMode(gameMode: GameMode) {
        val playerUpdate = ClientboundPlayerInfoPacket.PlayerUpdate(this.profile, 0, gameMode.getNMSValue(), null, null)
    }

    fun spawn(player: Player) {
        player.sendPacket(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer))
        player.sendPacket(ClientboundAddPlayerPacket(serverPlayer))
    }

    fun walkTo(plugin: JavaPlugin, finishLocation: Location, octaves: Int): BukkitTask {
        finishLocation.block.type = Material.REDSTONE_BLOCK
        location.block.type = Material.EMERALD_BLOCK

        val z1 = finishLocation.z - location.z / octaves
        val x1 = finishLocation.x - location.x / octaves

        return object : BukkitRunnable() {
            override fun run() {
                lookAtPoint(finishLocation)

                location.add(x1, 0.0, z1)
                moveRelative(x1, 0.0, z1)
            }

        }.runTaskTimer(plugin, 0, 40)
    }

    fun moveRelative(x: Double, y: Double, z: Double) {
        sendPacket(ClientboundMoveEntityPacket.Pos(
            serverPlayer.id,
            (x * 4096).toInt().toShort(),
            (y * 4096).toInt().toShort(),
            (z * 4096).toInt().toShort(),
            true))
    }

    fun moveRelative(x: Int, y: Int, z: Int) {
        moveRelative((x.toDouble() + .5), y.toDouble(), (z.toDouble() + .5))
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

    fun setSkin(offlinePlayer: OfflinePlayer) {
        val sUrl = "https://sessionserver.mojang.com/session/minecraft/profile/${offlinePlayer.uniqueId}?unsigned=false"

        val req = URL(sUrl).openConnection()
        req.connect()

        val reader = InputStreamReader(req.content as InputStream)
        val json = JsonParser.parseReader(reader).asJsonObject
        val array = json.get("properties").asJsonArray

        for (element in array) {
            val obj = element.asJsonObject
            if (obj.get("name") != null && obj.get("name").asString == "textures") {
                setSkin(obj.get("value").asString, obj.get("signature").asString)
                break
            }
        }
    }

    fun setSkin(name: String) {
        setSkin(Bukkit.getOfflinePlayer(name))
    }

    fun setSkin(player: Player) {
        val properties = (player as CraftPlayer).profile.properties.get("textures").stream().findFirst().orElse(null)

        setSkin(properties.value, properties.signature)
    }

    private fun sendPacket(packet: Packet<*>) {
        for(player in Bukkit.getOnlinePlayers()) {
            (player as CraftPlayer).handle.connection.send(packet)
        }
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

fun Player.sendPacket(packet: Packet<*>) {
    (this as CraftPlayer).handle.connection.send(packet)
}

fun Player.addPacketListener() {
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

                searchForPlayerNPC(world, id.getInt(rawPacket))!!.onPlayerInteract(player!!)
            }

            super.channelRead(ctx, rawPacket)
        }
    }

    val pipeline = (this as CraftPlayer).handle.connection.getConnection().channel.pipeline()
    pipeline.addBefore("packet_handler", name, handler)
}

internal fun Player.spawnWorldNPCS() {
    for(npc in Bukkit.getServer().getNPCS(world)) {
        npc.spawn(this)
    }
}

internal fun searchForPlayerNPC(world: World, entityID: Int): PlayerNPC? {
    for(npc in Bukkit.getServer().getNPCS(world)) {
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