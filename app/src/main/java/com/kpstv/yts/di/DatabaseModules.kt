package com.kpstv.yts.di

import android.content.Context
import androidx.room.Room
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.data.db.localized.RecommendDatabase
import com.kpstv.yts.data.db.localized.SuggestionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object MainModule {
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MainDatabase{
       return Room.databaseBuilder(
            context,
            MainDatabase::class.java,
            "main.db"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}

@Module
@InstallIn(ApplicationComponent::class)
object RecommendModule {
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RecommendDatabase{
        return Room.databaseBuilder(
            context,
            RecommendDatabase::class.java,
            "recommendMovie.db"
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}

@Module
@InstallIn(ApplicationComponent::class)
object SuggestionModule {
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SuggestionDatabase{
        return  Room.databaseBuilder(
            context,
            SuggestionDatabase::class.java,
            "suggestionMovie.db"
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}