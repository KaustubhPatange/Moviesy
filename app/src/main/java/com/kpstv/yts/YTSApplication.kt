package com.kpstv.yts

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import com.kpstv.yts.data.db.localized.*
import com.kpstv.yts.data.db.repository.*
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.extensions.utils.interceptors.NetworkConnectionInterceptor
import com.kpstv.yts.ui.viewmodels.providers.FinalViewModelFactory
import com.kpstv.yts.ui.viewmodels.providers.MainViewModelFactory
import com.kpstv.yts.ui.viewmodels.providers.MoreViewModelFactory
import com.kpstv.yts.extensions.utils.FlagUtils
import es.dmoral.toasty.Toasty
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

@Suppress("unused")
class YTSApplication : Application() {
   /* override val kodein = Kodein.lazy {
        import(androidXModule(this@YTSApplication))

        bind() from singleton { MainDatabase(instance()) }
        bind() from singleton { SuggestionDatabase(instance()) }
        bind() from singleton { RecommendDatabase(instance()) }
        bind() from singleton { instance<MainDatabase>().getMainDao() }
        bind() from singleton { instance<SuggestionDatabase>().getTMdbDao() }
        bind() from singleton { instance<RecommendDatabase>().getTMdbDao() }
        bind() from singleton { instance<MainDatabase>().getFavDao() }
        bind() from singleton { instance<MainDatabase>().getDownloadDao() }
        bind() from singleton { instance<MainDatabase>().getMovieDao() }
        bind() from singleton { instance<MainDatabase>().getPauseDao() }
        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { RetrofitUtils(instance()) }
        bind() from singleton { FlagUtils(instance()) }
        bind() from singleton { MovieRepository(instance(), instance()) }
        bind() from singleton { MainRepository(instance(), instance()) }
        bind() from singleton { TMdbRepository(instance(), instance()) }
        bind() from singleton { FavouriteRepository(instance()) }
        bind() from singleton { DownloadRepository(instance()) }
        bind() from singleton { PauseRepository(instance()) }
        bind() from singleton { YTSPlaceholderApi(instance()) }
        bind() from singleton { TMdbPlaceholderApi(instance()) }
        bind() from singleton {
            FinalViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from singleton { MoreViewModelFactory(instance(), instance(), instance()) }
        bind() from singleton {
            MainViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
    }
*/

}