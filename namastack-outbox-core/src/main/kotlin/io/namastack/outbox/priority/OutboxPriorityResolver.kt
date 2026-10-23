package io.namastack.outbox.priority

import org.slf4j.LoggerFactory

/**
 * Resolves the priority of a scheduled record from the registered [OutboxPriorityProvider] implementations.
 *
 * Providers are consulted in order and the first non-null value wins. A failing provider is logged
 * and skipped. Without a value the record gets [OutboxPriorityProvider.DEFAULT_PRIORITY].
 *
 * @author AdzerKI
 * @since 1.12.0
 */
class OutboxPriorityResolver internal constructor(
    providersSupplier: () -> List<OutboxPriorityProvider>,
) {
    private val log = LoggerFactory.getLogger(OutboxPriorityResolver::class.java)
    private val providers: List<OutboxPriorityProvider> by lazy {
        providersSupplier()
    }

    /**
     * Creates a resolver backed by the supplied providers.
     *
     * @param providers Priority providers consulted by [resolvePriority].
     */
    constructor(providers: List<OutboxPriorityProvider>) : this({ providers })

    /**
     * Resolves the priority of the record scheduled for the given payload.
     *
     * @param payload Payload being scheduled
     * @return First non-null priority of the providers, or the default priority
     */
    fun resolvePriority(payload: Any): Int =
        providers.firstNotNullOfOrNull { provider ->
            try {
                provider.provide(payload)
            } catch (ex: Exception) {
                log.warn("Priority provider {} failed: {}", provider::class.simpleName, ex.message, ex)
                null
            }
        } ?: OutboxPriorityProvider.DEFAULT_PRIORITY

    companion object {
        /** Resolver without providers: every record gets the default priority. */
        @JvmField
        val DEFAULT: OutboxPriorityResolver = OutboxPriorityResolver(emptyList())
    }
}
