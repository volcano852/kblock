package com.fabien.kblock

import com.fabien.kblock.domain.services.KBlockService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class KBlockAppIT(@Autowired val KBlockService: KBlockService) {

    @Test
    fun `first test`() {
        KBlockService.kblock.difficulty
    }
}