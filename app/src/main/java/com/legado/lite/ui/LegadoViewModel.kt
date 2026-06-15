package com.legado.lite.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.legado.lite.LegadoApp
import com.legado.lite.data.AppContainer
import kotlinx.coroutines.CoroutineScope

abstract class LegadoViewModel(application: Application) : AndroidViewModel(application) {
    protected val container: AppContainer = (application as LegadoApp).container
    protected val scope: CoroutineScope = viewModelScope
}
