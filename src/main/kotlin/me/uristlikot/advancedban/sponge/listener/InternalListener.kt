package me.uristlikot.advancedban.sponge.listener

import me.uristlikot.advancedban.PunishmentType
import me.uristlikot.advancedban.sponge.event.PunishmentEvent
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Listener
import org.spongepowered.api.profile.GameProfile
import org.spongepowered.api.service.ban.BanService
import org.spongepowered.api.text.Text
import org.spongepowered.api.util.ban.Ban
import org.spongepowered.api.util.ban.BanTypes
import java.net.InetAddress
import java.util.*


class InternalListener : EventListener<PunishmentEvent> {
    @Listener
    override fun handle(event: PunishmentEvent) {
        val service = Sponge.getServiceManager().provide(BanService::class.java).get()

//        if (event.punishment.type.equals(PunishmentType.BAN)||event.punishment.getType().equals(PunishmentType.TEMP_BAN)){
//            val ban=Ban.builder().type(BanTypes.PROFILE).profile(event.punishment.name)
//                    .reason(event.punishment.reason)
//                    .expirationDate()
//            banList.addBan(ban)
//
//            )
        }

    }
