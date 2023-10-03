Doordeck SDK
=================

[![Build Status](https://travis-ci.org/doordeck/doordeck-sdk-java.svg?branch=master)](https://travis-ci.org/doordeck/doordeck-sdk-java)

The official Doordeck SDK for Android


### What Is This?

The Doordeck SDK enables you to unlock doors. You can unlock doors using the NFC on your android device or simply to tapa the QR located on the door to unlock.

### Integration

Integrate the module `ui` and `api` inside your project and add `implementation project(path: ':ui')` in the build.gradle of your app.

A gradle version is coming soon.

### Running the SDK Sample Code

Developers can run the sample application, located in the `sample` directory, to immediately run code and see how the Doordeck Android SDK can be used.


### SDK Key
SDK Key can be  found ...


### How to use the SDK ? 


#### Initialization


The Doordeck SDK is a singleton that needs to initialized before using it, either in your Application or your MainActivity.
firstly, the merhod `initialize` will need to be called with the provided `apiKey`.
The 2nd param is optional, and correspond to the theme to use. Light or Dark. By default the dark theme is used.

```
 /**
     * Initialize the Doordeck SDK and get the Api key from the manifest file.
     * That's the first method to call, preferable in your Application or MainActivity, before using
     * the SDK.
     * @return Doordeck the current instance of the SDK
     */
    fun initialize(apiKey: String, darkMode: Boolean = true): Doordeck
```
    
#### Proguard

When enabling `minifyEnabled`, proguard and or R8 tools, you need to include these rules to the proguard-rules.pro:

```
-keep class com.doordeck.** { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }
-dontwarn javax.naming.**
-keep class retrofit2.** { *; }
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
```

    
#### Unlock a door by NFC/QR

The SDK has a method that will open an activity to get the information regarding the door to open, and open it according to the option you give to the method.
- The `Context` needs to be provided, it's required.
- the `ScanType`, QR or NFC, with NFC by default. It's optional
- A `callback`, providing you some info regarding the action (auth OK, auth KO, unlock OK, unlock KO)

Once the door unlocked, the activity opened by the SDK will close automatically and get back to the screen to open door (using NFC or QR)


```
/**
     * Show the unlock screen given the Scan type given in parameter
     *
     * @param context current context
     * @param type type of scan to use (NFC or QR) , NFC by default if not provided, optional
     * @param callback callback of the method, optional
     */
    fun showUnlock(context: Context, type: ScanType = ScanType.NFC, callback: UnlockCallback? = null)

```

#### Unlock a door by UUID
The SDK has a method that will open an activity to pass the information once obtained the UUID beforehand.
- You need the `uuid (String)`

```
    /**
     * Unlock method for unlocking via UUID
     *
     * @param ctx current Context
     * @param uuid, a valid uuid to a [Device]'s id. It has to be a valid UUID format
     * @param callback (optional) callback function for catching async response after unlock.
     *
     */
    @JvmOverloads
    fun unlock(ctx: Context, uuid: String, callback: UnlockCallback? = null){
        this.deviceToUnlock = PartialDevice(uuid)
        showUnlock(ctx, ScanType.UNLOCK, callback)
    }
```

#### Being aware of events

#### With a callback

The SDK contains a public method for you to provide a callback of the `IEventCallback` in order to be aware of the errors or other important events that we think you need to be aware of.
It's optional, you are not required to provide a callback.
If you do not wish to implement all those methods, you can simply provide a `EventCallback` which is a abstract class of `IEventCallback`. 

```
/**
     * Set a callback to listen to the events sent by the SDK
     */
    fun withEventsCallback(callback: IEventCallback) {
        this.callback = callback
    }
    
 interface IEventCallback {
    fun noInternet()
    fun networkError()
    fun verificationCodeSent()
    fun verificationCodeFailedSending()
    fun codeVerificationSuccess()
    fun codeVerificationFailed()
    fun sdkError()
    fun unlockSuccess()
    fun unlockFailed()
    fun resolveTileFailed()
    fun resolveTileSuccess()
    fun unlockedInvalidTileID()
    fun authentificationSuccess()
}

abstract class EventCallback : IEventCallback {
    override fun noInternet() {}
    override fun networkError() {}
    override fun verificationCodeSent() {}
    override fun verificationCodeFailedSending() {}
    override fun codeVerificationSuccess() {}
    override fun codeVerificationFailed() {}
    override fun sdkError() {}
    override fun unlockSuccess() {}
    override fun unlockFailed() {}
    override fun resolveTileFailed() {}
    override fun resolveTileSuccess() {}
    override fun unlockedInvalidTileID() {}
}
    
```

#### With an observable


The SDK contains a public method for you to listen on an observable, emitting the events listed above.

```
fun eventsObservable(): Observable<DDEVENT> {
        return EventsManager.eventsObservable()
    }
    
enum class EventAction {
    NO_INTERNET,
    SDK_ERROR,
    SDK_NETWORK_ERROR,
    VERIFICATION_CODE_SENT,
    VERIFICATION_CODE_FAILED_SENDING,
    CODE_VERIFICATION_SUCCESS,
    CODE_VERIFICATION_FAILED,
    UNLOCK_INVALID_TILE_ID,
    AUTHENTICATED,
    UNLOCK_SUCCESS,
    UNLOCK_FAILED,
    RESOLVE_TILE_FAILED,
    RESOLVE_TILE_SUCCESS
}
    
```


