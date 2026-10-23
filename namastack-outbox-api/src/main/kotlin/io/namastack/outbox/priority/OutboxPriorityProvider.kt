package io.namastack.outbox.priority

/**
 * Provider for the processing priority of outbox records.
 *
 * Priority providers are invoked when an outbox record is scheduled. The scheduler selects
 * record keys whose oldest pending record has the highest priority first; keys of equal
 * priority are selected by the age of their oldest pending record. Priority never reorders
 * records of the same key.
 *
 * Providers are consulted in order; the first non-null value wins. Without a value the
 * record gets [DEFAULT_PRIORITY].
 *
 * ## Example - Lower priority for a bulk replay
 *
 * ```kotlin
 * @Component
 * class ReplayPriorityProvider(private val replay: ReplayScope) : OutboxPriorityProvider {
 *     override fun provide(payload: Any): Int? = if (replay.isActive()) -1 else null
 * }
 * ```
 *
 * @author AdzerKI
 * @since 1.12.0
 */
fun interface OutboxPriorityProvider {
    /**
     * Provides the priority of the record scheduled for the given payload.
     *
     * @param payload Payload being scheduled
     * @return Priority of the record, or null to leave the decision to the next provider
     */
    fun provide(payload: Any): Int?

    companion object {
        /** Priority of records no provider assigned one to. */
        const val DEFAULT_PRIORITY = 0
    }
}
