package net.voidhub.voidlauncher.coroutine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRetryStateTest {
    @Test
    fun resetForRetryRestoresInitialTaskState() {
        val task = Task.runTask(task = { })

        task.updateStage(TaskStage.FAILED)
        task.updateProgress(0.72f)
        task.updateSpeed(4096L)

        task.resetForRetry()

        assertEquals(TaskStage.PREPARING, task.stage.value)
        assertEquals(-1f, task.progress.value)
        assertNull(task.message.value)
        assertNull(task.rateBytesPerSec.value)
    }
}
