package io.namastack.outbox.priority

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.namastack.outbox.priority.OutboxPriorityProvider.Companion.DEFAULT_PRIORITY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("OutboxPriorityResolver")
class OutboxPriorityResolverTest {
    @Nested
    @DisplayName("resolvePriority()")
    inner class ResolvePriorityTests {
        @Test
        fun `should return default priority when no provider assigns one`() {
            val provider = OutboxPriorityProvider { null }

            assertThat(OutboxPriorityResolver.DEFAULT.resolvePriority("payload")).isEqualTo(DEFAULT_PRIORITY)
            assertThat(OutboxPriorityResolver(listOf(provider)).resolvePriority("payload")).isEqualTo(DEFAULT_PRIORITY)
        }

        @Test
        fun `should return first non-null priority in provider order`() {
            val undecided = mockk<OutboxPriorityProvider>()
            val first = mockk<OutboxPriorityProvider>()
            val second = mockk<OutboxPriorityProvider>()
            every { undecided.provide("payload") } returns null
            every { first.provide("payload") } returns -1
            every { second.provide("payload") } returns 5

            val priority = OutboxPriorityResolver(listOf(undecided, first, second)).resolvePriority("payload")

            assertThat(priority).isEqualTo(-1)
            verify(exactly = 0) { second.provide(any()) }
        }

        @Test
        fun `should skip failing provider`() {
            val failing = mockk<OutboxPriorityProvider>()
            every { failing.provide(any()) } throws IllegalStateException("boom")

            val fallback = OutboxPriorityProvider { 3 }

            val priority = OutboxPriorityResolver(listOf(failing, fallback)).resolvePriority("payload")

            assertThat(priority).isEqualTo(3)
        }
    }
}
