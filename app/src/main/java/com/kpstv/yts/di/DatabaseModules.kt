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
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MainDatabase {
        return Room.databaseBuilder(
            context,
            MainDatabase::class.java,
            "main.db"
        )
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideMainDao(mainDatabase: MainDatabase) = mainDatabase.getMainDao()

    @Singleton
    @Provides
    fun provideFavDao(mainDatabase: MainDatabase) = mainDatabase.getFavDao()

    @Singleton
    @Provides
    fun provideDownloadDao(mainDatabase: MainDatabase) = mainDatabase.getDownloadDao()

    @Singleton
    @Provides
    fun provideMovieDao(mainDatabase: MainDatabase) = mainDatabase.getMovieDao()

    @Singleton
    @Provides
    fun providePauseDao(mainDatabase: MainDatabase) = mainDatabase.getPauseDao()

    @Singleton
    @Provides
    fun provideHistoryDao(mainDatabase: MainDatabase) = mainDatabase.getHistoryDao()
}

@Module
@InstallIn(ApplicationComponent::class)
object RecommendModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RecommendDatabase {
        return Room.databaseBuilder(
            context,
            RecommendDatabase::class.java,
            "recommendMovie.db"
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Singleton
    @Provides
    fun provideRecommendDao(recommendDatabase: RecommendDatabase) = recommendDatabase.getTMdbDao()
}

@Module
@InstallIn(ApplicationComponent::class)
object SuggestionModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SuggestionDatabase {
        return Room.databaseBuilder(
            context,
            SuggestionDatabase::class.java,
            "suggestionMovie.db"
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Singleton
    @Provides
    fun provideSuggestionDao(suggestionDatabase: SuggestionDatabase) = suggestionDatabase.getTMdbDao()
}