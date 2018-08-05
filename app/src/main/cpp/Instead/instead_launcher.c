#include <SDL.h>
#include <jni.h>
#include <unistd.h>
#include <android/log.h>

static int pfd[2];
static const char *tag = "InsteadLauncher";

int instead_main(int argc, char** argv);

int SDL_main(int argc, char** argv) {
    const char* path = argv[1];
    const char* appdata = argv[2];
    const char* gamespath = argv[3];
    const char* themespath = argv[4];
    const char* modes = argv[5];
    const char* lang = argv[6];
    const char* game = argv[7];

    __android_log_write(ANDROID_LOG_DEBUG, tag, argv[0]);
    __android_log_write(ANDROID_LOG_DEBUG, tag, path);
    __android_log_write(ANDROID_LOG_DEBUG, tag, appdata);
    __android_log_write(ANDROID_LOG_DEBUG, tag, gamespath);
    __android_log_write(ANDROID_LOG_DEBUG, tag, themespath);
    __android_log_write(ANDROID_LOG_DEBUG, tag, modes);
    __android_log_write(ANDROID_LOG_DEBUG, tag, lang);
    __android_log_write(ANDROID_LOG_DEBUG, tag, game);

    int status;
    char* _argv[13];
    int n = 1;
    chdir(path);

    _argv[0] = SDL_strdup(argv[0]);

    _argv[n++] = SDL_strdup("-nostdgames");
    _argv[n++] = SDL_strdup("-fullscreen");
    _argv[n++] = SDL_strdup("-modes");
    _argv[n++] = SDL_strdup(modes);
    _argv[n++] = SDL_strdup("-hires");

    if (strlen(lang) > 0) {
        _argv[n++] = SDL_strdup("-lang");
        _argv[n++] = SDL_strdup(lang);
    }

    _argv[n++] = SDL_strdup("-appdata");
    _argv[n++] = SDL_strdup(appdata);

    _argv[n++] = SDL_strdup("-gamespath");
    _argv[n++] = SDL_strdup(gamespath);

    _argv[n++] = SDL_strdup("-themespath");
    _argv[n++] = SDL_strdup(themespath);

    _argv[n++] = SDL_strdup("-game");
    _argv[n++] = SDL_strdup(game);

    _argv[n] = NULL;


    status = instead_main(n, _argv);

    __android_log_print(ANDROID_LOG_DEBUG, tag, "status = %d", status);

    fflush(NULL);
    for (int i = 0; i < n; ++i) {
        SDL_free(_argv[i]);
    }

    // Kill it with fire, or else we'll get the error when restarting the activity
    _exit(status);

    return status;
}

void rotate_landscape() {
    JNIEnv *env = (JNIEnv*)SDL_AndroidGetJNIEnv();
    jobject activity = (jobject)SDL_AndroidGetActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jstring jstr = (*env)->NewStringUTF(env, "LandscapeRight LandscapeLeft");
    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "setOrientation", "(IIZLjava/lang/String;)V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id, (jint)1, (jint)0, (jboolean)0, jstr);

    (*env)->DeleteLocalRef(env, jstr);
    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

void rotate_portrait() {
    JNIEnv *env = (JNIEnv*)SDL_AndroidGetJNIEnv();
    jobject activity = (jobject)SDL_AndroidGetActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jstring jstr = (*env)->NewStringUTF(env, "Portrait PortraitUpsideDown");
    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "setOrientation", "(IIZLjava/lang/String;)V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id, (jint)0, (jint)1, (jboolean)0, jstr);

    (*env)->DeleteLocalRef(env, jstr);
    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

void unlock_rotation() {
    JNIEnv *env = (JNIEnv*)SDL_AndroidGetJNIEnv();
    jobject activity = (jobject)SDL_AndroidGetActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "unlockRotation", "()V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id);

    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

/* vi: set ts=4 sw=4 expandtab: */
