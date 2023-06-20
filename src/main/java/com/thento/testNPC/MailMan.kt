package com.thento.testNPC

import com.thento.PlayerNPC
import org.bukkit.Location
import org.bukkit.entity.Player

class MailMan(location: Location): PlayerNPC("Mail Man", location) {

    init {
        /*8
                skinProperty = setSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY4MzQ2MDQxNzc2NCwKICAicHJvZmlsZUlkIiA6ICIxNmJhNWU4MDJhMmU0ZDJhYjEwZmZiYWJiYmQ1MDdlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzbGlua3l1c2VyMzMxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kZGI1Y2ZjZDJlMmFiZDAwZDk2MTMyM2EwMzEyMjlkYmE4Y2MxNzZjMzNiOTM4N2U3NWNhNGJlODEzM2JiMDJiIgogICAgfQogIH0KfQ==",
            "WNF7DEF+rl3ftIgz//WVOl383rWYVMUMg7C5tbMaZrZ0yAwp4yL4pHorX28MjrA5R2BHukvCQSvXo5SdbRZhdiIPMF32Kc5gKoZBRDsM7lRLzSM7JC9KiuBnZYe84uS2ywTcKW0rBvSAX11Xqj6Dx17hpy44RpB4gFAeMLyq3HvDhL6jt8EVj9lOCbmGiOn6b7KAT6FASV5asNTJ2NZ8/FV/oNuFXTigjXOpxvRAxpXf8DTSMyRJLLzzOhMzfrgUVcdr3RPIOnZ5DouOpu0fTnE37w0x2gejuKRrweJteXH7OVl9ewiqoSaVAogH64sKAD8nmVCJUgcAS58VTV+oUSgg5c7J86aX1DOftrnlbAf7tt+5XYUPxFt10fusNhL/NTZ5l/J3ac1eQFiZrfmxnQ0Cj7JPjjZlZAAZVytRzJRXitUFyv337wi/5zvaM6xTfb4zYLhUlUepLPE1t/lMGtby8DL6aTwr6n6sjlfuuVsqTq+KaJ5dfAEJpLMKHOb7xYOOf9a7SrD4Wg1Sk+729+4TZFxTIW6Bs5J5/6PqUh1u7rS4OhVdyg42lyPW1qp14vbtG6G8MrYTS4OvmGcikaVXXeeYufUge3RMqIobeZvbOqUz0q+BNeCIC7a/F0wi6Mb3HaxqP4TByaYbY59H7fqtN0DrtQpJgl85frbMtdo="
        )
         */

        setSkin("Dream")
    }

    override fun onPlayerInteract(player: Player) {
        player.sendMessage("Stop It Gay boy!")
    }
}