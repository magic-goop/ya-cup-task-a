package ru.ya.cup.resources

import android.content.Context
import androidx.annotation.StringRes
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface ResourcesManager {
    fun getString(@StringRes stringRes: Int): String
    fun getString(@StringRes stringRes: Int, vararg args: String): String
    fun getUserFilesDir(childDir: String = "reports"): File
}

internal class ResourcesManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourcesManager {

    override fun getString(@StringRes stringRes: Int): String = context.getString(stringRes)

    override fun getString(@StringRes stringRes: Int, vararg args: String): String =
        context.getString(stringRes, *args)

    override fun getUserFilesDir(childDir: String): File =
        File(context.filesDir, childDir).apply { mkdirs() }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ResourcesManagerModule {

    @Binds
    @Singleton
    abstract fun bindResourceManager(impl: ResourcesManagerImpl): ResourcesManager
}
