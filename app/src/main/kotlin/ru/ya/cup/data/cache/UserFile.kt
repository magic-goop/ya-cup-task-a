package ru.ya.cup.data.cache

import androidx.annotation.WorkerThread
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

internal class UserFile(private val filesDir: File, fileName: String) {
    private val mutex = Mutex()
    private val file by lazy {
        val parent = filesDir
        parent.mkdirs()
        File(parent, fileName)
    }

    @WorkerThread
    suspend fun writeToFile(data: List<String>) {
        mutex.withLock {
            swallowAnyError {
                FileOutputStream(file, true).use { fos ->
                    OutputStreamWriter(fos).use { writer ->
                        data.forEach {
                            writer.write(it)
                            writer.write("\n")
                        }
                        writer.flush()
                    }
                }
            }
        }
    }

    @WorkerThread
    suspend fun deleteFile() {
        mutex.withLock {
            swallowAnyError {
                file.delete()
            }
        }
    }

    private suspend fun swallowAnyError(block: suspend () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            // Don't do this in real app. Implement proper error handling
            Timber.e(t)
        }
    }
}