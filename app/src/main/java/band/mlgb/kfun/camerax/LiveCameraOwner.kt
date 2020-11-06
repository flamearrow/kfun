package band.mlgb.kfun.camerax

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import band.mlgb.kfun.MLGBALifeCycleObserver
import band.mlgb.kfun.PickImageActivity

/**
 * Customized life cycle for the live camera in [PickImageActivity], when it's in started state, keep the cameraX
 * usescases live, when destroyed, teardown camerax.
 */

class LiveCameraOwner : LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.addObserver(MLGBALifeCycleObserver())
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    fun startCamera() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun pauseCamera() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun shutDown() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

}