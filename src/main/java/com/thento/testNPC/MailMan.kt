package com.thento.testNPC

import com.thento.Ping
import com.thento.PlayerNPC
import org.bukkit.Location
import org.bukkit.entity.Player

class MailMan(location: Location, ping: Ping): PlayerNPC("Mail Man", location, ping) {

    init {
        skinProperty = setSkin(
        "ewogICJ0aW1lc3RhbXAiIDogMTU5MDg3ODQ2NzUwNiwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmNzAwMDI4ZWYxMzBmNWQzZTQ4MjNjYWNlZDY1OThlNGVlNzVkMDg2MmI2YWU0YjU3M2E3OGRjNjJmNjAxMDgiCiAgICB9CiAgfQp9",
        "qCFeNJOB1QcX5GSRHWGEF6Dm0XXsP54QDydhbO/uCYQmN+w57hw/zFCKLITTV8GCydDzusrNBQ8veD+2hDHSdnczpzJueXzBw3RD7rGqGlcFhRhSBI0vPF8jGwJij+hzkrwt6ZChAlLzskHAp7dxqeBn/JPJ/FwPDuLbfvU175wcPPWLNil0Yw40irsQ/lbyaxGjC2I/U8P6j5tukRXnWYWcKUrRRlSdwGH+3rK+SbrhaX6MLv8RHgOmGCWBhvk4VYIAFET+K73EbjmKZjaJWPB9swp5QRFBGF4/kzLm/O8di3RH8nI+J0XtScLPU5obWhQa256I52flDCE8qzjnNoLfmGMYKOh6Uq9ZJBU4EOpxmnj7MZp3YHUb8+zJHcjRO7iog7mdMsaQUrc4xUyFMHAUurZcftc3xE23f9pszKxGcVySMFLUl3yPf8mNWHxm+qox4FFQXjexuijQsmeXn6vPh6LxtXeERIDgp2kskwai0j3uDIfQAUYneeybKvRIszKKpRv+EgIEF/kEhYAYWhuElD3Ynd00EQ1GIi6pfIq+c//V8HhzFz8RdHr5ZW1GGjQnAInxTKYRMuVf+5bdHo3omAb8wye1/tgrB6jaWfFrxA3E2LRRkvkjcFKolMw4dPCC37sgNvmCKBNUB9UyDfjCyZFRzTanCm/lXneF0ZI="
        )
    }

    override fun onPlayerInteract(player: Player) {
        TODO()
    }
}