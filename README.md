
Requirements:
-----
You need to be using mojang mappings for version 1.19+.

How to Use
-----
Clone the npc [file](https://github.com/Thentoxd/PerfectNPC/blob/master/src/main/java/com/thento/instance/npc/NPC.kt) into your project and spawn them using:
```kotlin
val npc = NPC(sender, "Billy Bob", sender.location)
```
And thats it! 🎉

Tutorials:
-----
Set the NPC's item in specific slots:
```kotlin
npc.setItem(EquipmentSlot.CHEST, ItemStack(Material.DIAMOND_CHESTPLATE))
```
-----
Set the NPC's skin (1st param = skin value, 2nd param = skin signature):
```kotlin
npc.setSkin(
"ewogICJ0aW1lc3RhbXAiIDogMTY2NjYwNjUwNjA1NywKICAicHJvZmlsZUlkIiA6ICI2NDg4MzMwOGZhNGI0MWU2YjY2ZWQ2NGQwNjBhMTJkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVudG8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTIyNzlhYTdlNDUwZDJiZTQ2MjI4MzkwYWU2YTU4NTBmZTI3YTZiOTc5YTViYmJkMTE1NDc1NWNjZmRiYWMwOCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
"BgY3Nk8Dr73ldMOhyLVpxFMsTgjMlPm3COAFFlXlfQVsoIE4w0ErcOogGU5+QtcAUAltMaeoK8KXurc0zRCVrALY27Uxw8fdeY3zo+FGeEE7ofEOYgpJeXCdvsWCB+/ceWmYj1Tl9tonR21Q3HlJvCgYH+otlhG3qCmkkG/zusas+0btwIVwmWmrGRiPD1xV8TAv0arPtzNvFPsyVYXzy+Z1SYu4PwR9w7no2did4ERjT3V80SWxlDP2yOVEeOTBKZgYQdbDNULnIT3slbwY0BfuK5qbbVI1URsKZqaH2VWkwqTB65S7D5dlLK/+VPzGZ6qZukHfaYmRgu7GZK+jEh/pS8U8cPrx1BuERpEfYghFez+p/0HLeBlJTjWTqErunMRqwlG/vM/30zaCxlqPBcMibO020MOFSS9Ci9MRhDYoFJsiw2Pig7kkvFCWHXQGUYo9JXpChzwmT7i83hQwZZCyY/koBdlnuU0Xnyi3qqF5KRyFYeH2VzxdZDmpPJqKh0K3wGfeOB7i6ZmK80endQDe+X849iz+2mc6b+1BQ9uC3iiW7Uu2PxeKl2zSyA77O20F7rzsYWkTKjOgu/rsnJIODOWEw+QgJc+PUVU1pEZ6B4smDFa4aUi3xXYWWdm7eCoOGvJ4y2Zc9iaBRuX0VwR3B3MBF2hEE3L/GB1KGr8="
)
```
Use [this site](https://mineskin.org/) to get the skin textures of a player.

-----
Make the npc look at a point:
```kotlin
npc.lookAtPoint(sender.location)
```
Make the npc look at player:
```kotlin
npc.lookAtPlayer(sender)
```
-----

Make the NPC always look at a player:
```kotlin
@EventHandler
fun onMove(event: PlayerMoveEvent) {
    this.npc!!.lookAtPlayer(event.player)
}
```
Of course - this will make the npc look at every player in the server every time they move. You probably don't want that, so let's check if they're in a close range!
```kotlin
@EventHandler
fun onMove(event: PlayerMoveEvent) {
    if(npc!!.isInRange(event.player.location, 5)) {
        this.npc!!.lookAtPlayer(event.player)   
    }
}
```
PacketDuplexHandler Reader
-----
Although the name is very difficult to understand. It's actually quite simple, this has been intergrated into the kotlin file, all it does it looks at *EVERY* packet sent to a player.

We will now need to edit the code and change the NPC instance to an object:
```kotlin
val npc = object : NPC(sender, "Billy Bob", sender.location) {
                    
}
```
To listen for the packets, add this inside:
```kotlin
override fun onChannelRead(player: Player, rawPacket: Any?): Boolean {
    return super.onChannelRead(player, rawPacket)
}
```
You can now see all the packets that are being sent to the player

But you need to add this outside the method, this tells the handler which player it's listening in for:
```kotlin
npc.inject(sender)
```
OR...
Add everybody in the server instance to be listened:
```kotlin
npc.injectAll()
```

So your code should now look something like this:
```kotlin
val npc = object : NPC(sender, "Billy Bob", sender.location) {
    override fun onChannelRead(player: Player, rawPacket: Any?): Boolean {
        return super.onChannelRead(player, rawPacket)
    }
}
npc.injectAll()
```

*I am working on giving the npc walking abilites. Any help needed, ask! 😄*
