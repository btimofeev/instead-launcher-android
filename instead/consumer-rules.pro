# Static methods
-keep class org.libsdl.app.** { *; }
-keep class org.emunix.instead.ui.** { *; }

# Native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}