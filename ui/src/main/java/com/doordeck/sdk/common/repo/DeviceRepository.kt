package com.doordeck.sdk.common.repo

import com.doordeck.multiplatform.sdk.Doordeck
import com.doordeck.multiplatform.sdk.model.data.LockOperations
import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import java.util.concurrent.CompletableFuture

interface DeviceRepository {
    fun getDevicesAvailable(tileId: String, defaultLockColours: Array<String>): CompletableFuture<List<LockResponse>>
    fun unlockDevice(deviceId: String): CompletableFuture<Unit>
}

class DeviceRepositoryImpl(
    private val doordeck: Doordeck
) : DeviceRepository {
    override fun getDevicesAvailable(tileId: String, defaultLockColours: Array<String>): CompletableFuture<List<LockResponse>> {
        // Get device ids from tile
        return doordeck.tiles().getLocksBelongingToTileAsync(tileId)
            .thenCompose { tileLocksResponse ->

                // Map each device id to a lock
                val lockFutures = tileLocksResponse.deviceIds.map { deviceId -> doordeck.lockOperations().getSingleLockAsync(deviceId) }
                CompletableFuture.allOf(*lockFutures.toTypedArray())
                    .thenCompose {
                        val resolvedLocks = lockFutures.map { it.join() }

                        // Update to a colour if its color is null
                        val updatedLocksIfNeeded = resolvedLocks.map { lock ->
                            if (lock.colour == null) {
                                updateColourOfDevice(lock.id, defaultLockColours.random())
                            } else {
                                CompletableFuture.completedFuture(lock)
                            }
                        }

                        // Return all
                        CompletableFuture.allOf(*updatedLocksIfNeeded.toTypedArray())
                            .thenApply {
                                updatedLocksIfNeeded.map { it.join() }
                            }
                    }
            }
    }

    private fun updateColourOfDevice(lockId: String, colour: String): CompletableFuture<LockResponse> {
        return doordeck.lockOperations()
            // First update
            .updateLockColourAsync(lockId, colour)
            // Server does not answer with the updated lock, retrieve
            .thenCompose {
                doordeck.lockOperations().getSingleLockAsync(lockId)
            }
    }

    override fun unlockDevice(deviceId: String): CompletableFuture<Unit> {
        return doordeck.lockOperations()
            .unlockAsync(
                LockOperations.UnlockOperation(
                    baseOperation = LockOperations.BaseOperation(
                        lockId = deviceId,
                    )
                )
            )
    }
}
