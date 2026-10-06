/*
 * Copyright (C) 2016-2018 Anton Kolosov https://github.com/instead-hub/instead-android-ng
 * Copyright (c) 2018 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

#include <SDL3/SDL.h>
#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>

static int pfd[2];
static const char *tag = "InsteadLauncher";

int instead_main(int argc, char** argv);

extern int gfx_width;
extern int gfx_height;
extern int gfx_cursor(int *xp, int *yp);
extern void gfx_warp_cursor(int x, int y);
extern int input_text(int start);

int SDL_main(int argc, char** argv) {
    const char* path = argv[1];
    const char* appdata = argv[2];
    const char* gamespath = argv[3];
    const char* themespath = argv[4];
    const char* lang = argv[5];
    const char* music = argv[6];
    const char* cursor = argv[7];
    const char* owntheme = argv[8];
    const char* defaulttheme = argv[9];
    const char* hires = argv[10];
    const char* textsize = argv[11];
    const char* noautosave = argv[12];
    const char* game = argv[13];
    const char* kbd = argv[14];

    __android_log_write(ANDROID_LOG_DEBUG, tag, argv[0]);
    __android_log_write(ANDROID_LOG_DEBUG, tag, path);
    __android_log_write(ANDROID_LOG_DEBUG, tag, appdata);
    __android_log_write(ANDROID_LOG_DEBUG, tag, gamespath);
    __android_log_write(ANDROID_LOG_DEBUG, tag, themespath);
    __android_log_write(ANDROID_LOG_DEBUG, tag, lang);
    __android_log_write(ANDROID_LOG_DEBUG, tag, game);

    int status;
    char* _argv[32];
    int n = 1;
    chdir(path);

    _argv[0] = SDL_strdup(argv[0]);

    _argv[n++] = SDL_strdup("-nostdgames");
    _argv[n++] = SDL_strdup("-fullscreen");

    if (SDL_strcmp(music, "n") == 0) {
        _argv[n++] = SDL_strdup("-nosound");
    }

    if (SDL_strcmp(hires, "y") == 0) {
        _argv[n++] = SDL_strdup("-hires");
    } else {
        _argv[n++] = SDL_strdup("-nohires");
    }

    _argv[n++] = SDL_strdup("-fontscale");
    _argv[n++] = SDL_strdup(textsize);

    if (SDL_strcmp(cursor, "n") == 0) {
        _argv[n++] = SDL_strdup("-nocursor");
    }

    if (SDL_strcmp(owntheme, "y") == 0) {
        _argv[n++] = SDL_strdup("-owntheme");
    } else {
        _argv[n++] = SDL_strdup("-notheme");
    }

    _argv[n++] = SDL_strdup("-theme");
    _argv[n++] = SDL_strdup(defaulttheme);

    if (strlen(lang) > 0) {
        _argv[n++] = SDL_strdup("-lang");
        _argv[n++] = SDL_strdup(lang);
    }

    if (strlen(kbd) > 0) {
        _argv[n++] = SDL_strdup("-kbd");
        _argv[n++] = SDL_strdup(kbd);
    }

    _argv[n++] = SDL_strdup("-appdata");
    _argv[n++] = SDL_strdup(appdata);

    _argv[n++] = SDL_strdup("-gamespath");
    _argv[n++] = SDL_strdup(gamespath);

    _argv[n++] = SDL_strdup("-themespath");
    _argv[n++] = SDL_strdup(themespath);

    if (SDL_strcmp(noautosave, "y") == 0) {
        _argv[n++] = SDL_strdup("-noautosave");
    }

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
    exit(status);

    return status;
}

void rotate_landscape() {
    JNIEnv *env = (JNIEnv*)SDL_GetAndroidJNIEnv();
    jobject activity = (jobject)SDL_GetAndroidActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jstring jstr = (*env)->NewStringUTF(env, "LandscapeRight LandscapeLeft");
    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "setOrientation", "(IIZLjava/lang/String;)V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id, (jint)1, (jint)0, (jboolean)0, jstr);

    (*env)->DeleteLocalRef(env, jstr);
    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

void rotate_portrait() {
    JNIEnv *env = (JNIEnv*)SDL_GetAndroidJNIEnv();
    jobject activity = (jobject)SDL_GetAndroidActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jstring jstr = (*env)->NewStringUTF(env, "Portrait PortraitUpsideDown");
    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "setOrientation", "(IIZLjava/lang/String;)V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id, (jint)0, (jint)1, (jboolean)0, jstr);

    (*env)->DeleteLocalRef(env, jstr);
    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

void unlock_rotation() {
    JNIEnv *env = (JNIEnv*)SDL_GetAndroidJNIEnv();
    jobject activity = (jobject)SDL_GetAndroidActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "unlockRotation", "()V");
    (*env)->CallStaticVoidMethod(env, clazz, method_id);

    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

void get_screen_size(int *w, int *h) {
    const char *str;
    JNIEnv *env = (JNIEnv*)SDL_GetAndroidJNIEnv();
    jobject activity = (jobject)SDL_GetAndroidActivity();
    jclass clazz = (*env)->GetObjectClass(env, activity);

    jmethodID method_id = (*env)->GetStaticMethodID(env, clazz, "getScreenSize", "()Ljava/lang/String;");
    jstring s= (*env)->CallStaticObjectMethod (env, clazz, method_id);

    str = (*env)->GetStringUTFChars(env, s, 0);
    sscanf(str, "%dx%d", w, h);
    (*env)->ReleaseStringUTFChars(env, s, str);

    (*env)->DeleteLocalRef(env, clazz);
    (*env)->DeleteLocalRef(env, activity);
}

#define CURSOR_STEPS 32
#define CURSOR_STEP_MIN 8
#define CURSOR_MAX_SPEED 5

static void push_cursor_motion(int x, int y) {
    SDL_Event event;

    memset(&event, 0, sizeof(event));
    event.type = SDL_EVENT_MOUSE_MOTION;
    event.motion.x = x;
    event.motion.y = y;

    SDL_PushEvent(&event);
}

static void push_cursor_button(int down, int x, int y) {
    SDL_Event event;

    memset(&event, 0, sizeof(event));
    event.type = down ? SDL_EVENT_MOUSE_BUTTON_DOWN : SDL_EVENT_MOUSE_BUTTON_UP;
    event.button.x = x;
    event.button.y = y;
    event.button.button = SDL_BUTTON_LEFT;
    event.button.clicks = 1;

    SDL_PushEvent(&event);
}

void Java_org_emunix_instead_ui_InsteadActivity_moveCursor(JNIEnv* env, jobject thiz, jint dx, jint dy, jint speed) {
    int x, y;
    int step;

    if (gfx_width <= 0 || gfx_height <= 0)
        return;

    step = gfx_height < gfx_width ? gfx_height : gfx_width;
    step /= CURSOR_STEPS;
    if (step < CURSOR_STEP_MIN)
        step = CURSOR_STEP_MIN;
    if (speed > CURSOR_MAX_SPEED)
        speed = CURSOR_MAX_SPEED;

    gfx_cursor(&x, &y);
    x += dx * step * speed;
    y += dy * step * speed;
    if (x < 0)
        x = 0;
    else if (x >= gfx_width)
        x = gfx_width - 1;
    if (y < 0)
        y = 0;
    else if (y >= gfx_height)
        y = gfx_height - 1;

    // Mirrors the gamepad mouse path from Instead's input.c: the event is left
    // with windowID 0, so mouse_watcher() skips the window-to-render conversion
    // and takes the render coordinates as is. The warp has no effect on the
    // Android video driver, the pushed event is what moves the cursor.
    gfx_warp_cursor(x, y);
    push_cursor_motion(x, y);
}

void Java_org_emunix_instead_ui_InsteadActivity_clickCursor(JNIEnv* env, jobject thiz, jboolean down) {
    int x, y;

    if (gfx_width <= 0 || gfx_height <= 0)
        return;

    gfx_cursor(&x, &y);
    push_cursor_button(down ? 1 : 0, x, y);
}

jboolean Java_org_emunix_instead_ui_InsteadActivity_isTextInputActive(JNIEnv* env, jobject thiz) {
    return input_text(-1) ? JNI_TRUE : JNI_FALSE;
}
