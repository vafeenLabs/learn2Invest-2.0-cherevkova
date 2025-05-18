package ru.surf.learn2invest.data.converters

import ru.surf.learn2invest.data.services.settings_manager.SettingsSerializable
import ru.surf.learn2invest.domain.domain_models.Settings

// Преобразование SettingsSerializable в Profile
internal fun SettingsSerializable.toSettings(): Settings = Settings(
    firstName = this.firstName,
    lastName = this.lastName,
    biometry = this.biometry,
    fiatBalance = this.fiatBalance,
    assetBalance = this.assetBalance,
    hash = this.hash,
    tradingPasswordHash = this.tradingPasswordHash
)

// Преобразование Profile в SettingsSerializable
internal fun Settings.toSettingsSerializable(): SettingsSerializable = SettingsSerializable(
    firstName = this.firstName,
    lastName = this.lastName,
    biometry = this.biometry,
    fiatBalance = this.fiatBalance,
    assetBalance = this.assetBalance,
    hash = this.hash,
    tradingPasswordHash = this.tradingPasswordHash
)
