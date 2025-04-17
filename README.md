Doordeck SDK
=================

The official Doordeck SDK for Android


### What Is This?

The Doordeck SDK enables you to unlock doors. You can unlock doors using the NFC on your android device or simply to tap the QR located on the door to unlock.

### Integration

Integrate the module `ui` inside your project and add `implementation project(path: ':ui')` in the build.gradle of your app.

### Running the SDK Sample Code

Developers can run the sample application, located in the `sampleApp` directory, to immediately run code and see how the Doordeck Android SDK can be used.


### How to use the SDK ? 


#### Initialization


The Doordeck SDK is a singleton that needs to initialized before using it, either in your Application or your MainActivity.
firstly, the method `initialize` will need to be called with the provided `apiKey`.
The 2nd param is optional, and correspond to the theme to use. Light or Dark. By default the dark theme is used.

```
/**
 * Initializes the Doordeck SDK.
 *
 * You must call this before using any other SDK methods.
 * It can be called at any point in your app lifecycle, and with any valid context.
 *
 * @param context Any valid Android context (application or activity).
 * @param darkMode Optional: Enables dark mode for SDK UI. Default is false.
 * @return The initialized Doordeck instance.
 */
@JvmOverloads
fun initialize(
    context: Context,
    darkMode: Boolean = false,
): Doordeck
``` 

#### Tweak the NFC Uri settings
Default values for a NFC Uri link would be `https://doordeck.link/${uuid}`.
If you want to customise this values, go to your main project's `build.gradle`, inside `buildscript { }` define:

```
ext.nfcUri = [
    "scheme": "https", // Replace with the scheme you want or leave it empty
    "host": "doordeck.link", // Replace with the host you want or leave it empty
]
```

#### Proguard

When enabling `minifyEnabled`, proguard and or R8 tools, you need to include these rules to the proguard-rules.pro:

```
-keep class com.doordeck.** { *; }
-dontwarn javax.naming.**
-keepattributes *Annotation*
```
    
#### Unlock a door by NFC/QR

The SDK has a method that will open an activity to get the information regarding the door to open, and open it according to the option you give to the method.
- The `Context` needs to be provided, it's required.
- the `ScanType`, QR or NFC, with NFC by default. It's optional
- A `callback`, providing you some info regarding the action (auth OK, auth KO, unlock OK, unlock KO)

Once the door unlocked, the activity opened by the SDK will close automatically and get back to the screen to open door (using NFC or QR)


```
/**
 * Shows the unlock screen using the specified scan type.
 *
 * @param context Context for launching the screen.
 * @param type Type of unlock scan (NFC, QR, or UNLOCK).
 * @return A CompletableFuture that completes after navigation is triggered.
 */
fun showUnlock(context: Context, type: ScanType = ScanType.NFC)

```

#### Unlock by Tile ID
The SDK has a method that will open an activity to pass the information once obtained the Tile ID (UUID) beforehand.
- You need the `UUID (String)`

```
/**
 * Triggers the unlock flow using a tile UUID (e.g. from QR or NFC).
 *
 * @param context Valid context to launch the unlock screen.
 * @param tileId A valid tile UUID string.
 * @return A CompletableFuture that completes when the UI is shown.
 */
fun unlockTileID(context: Context, tileId: String): CompletableFuture<Void>
```

## 📡 Listening to SDK Events

The Doordeck SDK exposes important events — such as unlock status, tile resolution, or geofence issues — through a Kotlin `Flow`.

This is the official and only supported way to observe SDK state in version `3.0.0`.

---

### ✅ Subscribing to Events

Collect events from the SDK using a coroutine:

```kotlin
lifecycleScope.launch {
    Doordeck.eventsFlow().collect { event ->
        when (event) {
            is UnlockSuccessEvent -> {
                // Successfully unlocked
                val deviceId = event.deviceId
            }
            is UnlockFailedEvent -> {
                // Unlock failed
                val deviceId = event.deviceId
                val error = event.exception
            }
            is ResolveTileSuccess -> {
                val tileId = event.tileId
                // Successfully resolved tile
            }
            is ResolveTileFailed -> {
                val tileId = event.tileId
                val error = event.exception
                // Failed to resolve tile
            }
            is GeofenceError -> {
                val deviceId = event.deviceId
                val requirement = event.locationRequirement
                // Location requirements not met
            }
            is GeneralErrorEvent -> {
                val error = event.exception
                // Catch-all for unexpected or SDK-level errors
            }
        }
    }
}
```

## ⚠️ Breaking Changes in v3.0.0

Version `3.0.0` introduces major refactors to modernize the SDK and simplify the interface.
Several legacy APIs have been removed, event handling has been updated to use Kotlin `Flow`, and the unlock mechanism has been restructured.

# We strongly suggest this is the right time for you to move into our [Doordeck Headless SDK](https://github.com/doordeck/doordeck-headless-sdk/) 

---

### ❌ Removed APIs

#### Removed callback-based event system

- `IEventCallback`, `EventCallback`, and related types have been **deleted**.
- `withEventsCallback(...)` has been **removed**.
- `UnlockCallback` is no longer supported.
- Sample app methods `listenForEventsRx()` and `listenForAllEventsCallback()` have been removed.
- All override methods like `noInternet()`, `networkError()`, etc., are no longer available.

#### Removed Rx-style observables

- The method `eventsObservable()` has been removed.
- The SDK no longer uses or supports RxJava.

---

### ✅ Replaced with Kotlin Flow

Event handling now uses a `Flow<DoordeckEvent>`, which can be collected in a coroutine:

```kotlin
fun eventsFlow(): Flow<DoordeckEvent>
```

Example usage:

```kotlin
lifecycleScope.launch {
    Doordeck.eventsFlow().collect { event ->
        when (event) {
            is UnlockSuccessEvent -> { /* handle success */ }
            is UnlockFailedEvent -> { /* handle failure */ }
            is GeneralErrorEvent -> { /* handle general SDK error */ }
            is GeofenceError -> { /* handle geofence mismatch */ }
            // etc.
        }
    }
}
```

-------------------

### 📦 New Event Model

DoordeckEvent is now a sealed class. The SDK emits the following events:
- UnlockSuccessEvent(deviceId: String)
- UnlockFailedEvent(deviceId: String, exception: Throwable)
- ResolveTileSuccess(tileId: String)
- ResolveTileFailed(tileId: String, exception: Throwable)
- GeofenceError(deviceId: String, locationRequirement: LocationRequirementResponse)
- GeneralErrorEvent(exception: Throwable)

### 🧱 Interface Changes

```kotlin
fun initialize(ctx: Context, darkMode: Boolean = false): Doordeck
```

- Now accepts any valid Context (Application or Activity).
- Can be called at any point in your app’s lifecycle.
- No longer accepts an auth token directly — you must call setToken(...) separately.

```kotlin
fun setToken(authToken: String)
```

- Must be called after login with a valid JWT.
- Throws if an empty or blank token is passed.
- Automatically generates a key pair if needed.

```kotlin
fun showUnlock(context: Context, type: ScanType = ScanType.NFC): CompletableFuture<Void>
fun unlock(ctx: Context, device: LockResponse): CompletableFuture<Void>
fun unlockTileID(ctx: Context, tileId: String): CompletableFuture<Void>
```

- All these won't use the old `UnlockCallback`. Errors related to Auth will be thrown inside the `CompletableFuture` and
performing errors will be thrown inside the `DoordeckEvent`'s flow and visually.

### 🧹 General Cleanup
- Removed the api/ module *(to favour the new Doordeck Headless SDK)*
- README was updated for clarity and correctness.
