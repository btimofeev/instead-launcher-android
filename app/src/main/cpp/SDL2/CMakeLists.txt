
#----------------
# Build hidapi
#----------------

set( CMAKE_CXX_FLAGS "-std=c++11" )

set( SOURCES
		"SDL2/src/hidapi/android/hid.cpp" )

add_library( hidapi SHARED ${SOURCES} )

target_link_libraries(hidapi
						log )

#----------------
# Build SDL2
#----------------

include_directories( SDL2/include/ )

include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()

set( CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DGL_GLEXT_PROTOTYPES" )

file( GLOB SOURCES
        	"SDL2/src/*.c"
        	"SDL2/src/audio/*.c"
        	"SDL2/src/audio/android/*.c"
        	"SDL2/src/audio/dummy/*.c"
			"SDL2/src/audio/openslES/*.c"
        	"SDL2/src/atomic/SDL_atomic.c"
        	"SDL2/src/atomic/SDL_spinlock.c"
        	"SDL2/src/core/android/*.c"
        	"SDL2/src/cpuinfo/*.c"
        	"SDL2/src/dynapi/*.c"
        	"SDL2/src/events/*.c"
        	"SDL2/src/file/*.c"
        	"SDL2/src/haptic/*.c"
        	"SDL2/src/haptic/android/*.c"
        	"SDL2/src/joystick/*.c"
        	"SDL2/src/joystick/android/*.c"
        	"SDL2/src/joystick/hidapi/*.c"
        	"SDL2/src/loadso/dlopen/*.c"
        	"SDL2/src/power/*.c"
        	"SDL2/src/power/android/*.c"
        	"SDL2/src/filesystem/android/*.c"
			"SDL2/src/sensor/*.c"
			"SDL2/src/sensor/android/*.c"
        	"SDL2/src/render/*.c"
        	"SDL2/src/render/*/*.c"
        	"SDL2/src/stdlib/*.c"
        	"SDL2/src/thread/*.c"
        	"SDL2/src/thread/pthread/*.c"
        	"SDL2/src/timer/*.c"
        	"SDL2/src/timer/unix/*.c"
        	"SDL2/src/video/*.c"
        	"SDL2/src/video/android/*.c"
        	"SDL2/src/video/yuv2rgb/*.c"
        	"SDL2/src/test/*.c" )

add_library( SDL2 SHARED ${SOURCES} )

#find_library( dl-lib dl )
#find_library( GLESv1_CM-lib GLESv1_CM )
#find_library( GLESv2-lib GLESv2 )
#find_library( log-lib log )
#find_library( android-lib android )

target_link_libraries( SDL2
						hidapi
                        dl
                        GLESv1_CM
                        GLESv2
						OpenSLES
                        log
                        android
						cpufeatures )
