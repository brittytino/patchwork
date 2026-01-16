package com.guardian.launcher.rules

/**
 * App-specific rules.
 * 
 * Controls:
 * - Which apps are allowed/blocked
 * - Per-app time limits
 * - Age-appropriate content filtering
 */
data class AppRule(
    override val id: String,
    override val name: String,
    override val enabled: Boolean,
    val packageName: String,
    val isAllowed: Boolean,
    val maxDailyMinutes: Int? = null,
    val requiresSupervision: Boolean = false
) : Rule() {
    
    override fun evaluate(): Boolean {
        // TODO: Check if app is allowed
        // TODO: Check time limit for this app
        // TODO: Check supervision requirements
        return false
    }
}

/**
 * App categories for easier management
 */
enum class AppCategory {
    EDUCATIONAL,
    ENTERTAINMENT,
    SOCIAL_MEDIA,
    GAMES,
    PRODUCTIVITY,
    SYSTEM,
    OTHER
}

/**
 * App rule builder
 */
class AppRuleBuilder {
    private var id: String = ""
    private var name: String = ""
    private var enabled: Boolean = true
    private var packageName: String = ""
    private var isAllowed: Boolean = true
    private var maxDailyMinutes: Int? = null
    private var requiresSupervision: Boolean = false
    
    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun enabled(enabled: Boolean) = apply { this.enabled = enabled }
    fun packageName(pkg: String) = apply { this.packageName = pkg }
    fun allowed(allowed: Boolean) = apply { this.isAllowed = allowed }
    fun maxDailyMinutes(minutes: Int) = apply { this.maxDailyMinutes = minutes }
    fun requiresSupervision(requires: Boolean) = apply { this.requiresSupervision = requires }
    
    fun build() = AppRule(
        id = id,
        name = name,
        enabled = enabled,
        packageName = packageName,
        isAllowed = isAllowed,
        maxDailyMinutes = maxDailyMinutes,
        requiresSupervision = requiresSupervision
    )
}
