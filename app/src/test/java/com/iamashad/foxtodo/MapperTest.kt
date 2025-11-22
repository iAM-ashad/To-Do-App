package com.iamashad.foxtodo

import com.iamashad.foxtodo.data.local.TaskEntity
import com.iamashad.foxtodo.data.mapper.toDomain
import com.iamashad.foxtodo.data.mapper.toEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `entity to domain and back preserves fields`() {
        val entity = TaskEntity(
            id = 5L,
            title = "Hello",
            description = "desc",
            dueDateEpoch = 12345L,
            completed = true,
            category = "Work",
            priority = 2
        )
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.priority, domain.priority)

        val back = domain.toEntity()
        assertEquals(entity, back)
    }
}
