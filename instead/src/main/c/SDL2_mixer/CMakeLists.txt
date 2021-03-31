
set ( COMMON_C_FLAGS ${CMAKE_C_FLAGS} )

#------------------
# Build libmodplug
#------------------

set( MODPLUG_DIR "SDL2_mixer/external/libmodplug-0.8.9.0" )

set( CMAKE_C_FLAGS ${COMMON_C_FLAGS} )

#check_function_exists("setenv" HAVE_SETENV)
#check_function_exists("sinf" HAVE_SINF)

#if(HAVE_SETENV)
add_definitions(-DHAVE_SETENV)
#endif(HAVE_SETENV)
#if(HAVE_SINF)
add_definitions(-DHAVE_SINF)
#endif(HAVE_SINF)

include_directories( ${MODPLUG_DIR}/src ${MODPLUG_DIR}/src/libmodplug )

set( SOURCES
        "${MODPLUG_DIR}/src/fastmix.cpp"
        "${MODPLUG_DIR}/src/load_669.cpp"
        "${MODPLUG_DIR}/src/load_abc.cpp"
        "${MODPLUG_DIR}/src/load_amf.cpp"
        "${MODPLUG_DIR}/src/load_ams.cpp"
        "${MODPLUG_DIR}/src/load_dbm.cpp"
        "${MODPLUG_DIR}/src/load_dmf.cpp"
        "${MODPLUG_DIR}/src/load_dsm.cpp"
        "${MODPLUG_DIR}/src/load_far.cpp"
        "${MODPLUG_DIR}/src/load_it.cpp"
        "${MODPLUG_DIR}/src/load_j2b.cpp"
        "${MODPLUG_DIR}/src/load_mdl.cpp"
        "${MODPLUG_DIR}/src/load_med.cpp"
        "${MODPLUG_DIR}/src/load_mid.cpp"
        "${MODPLUG_DIR}/src/load_mod.cpp"
        "${MODPLUG_DIR}/src/load_mt2.cpp"
        "${MODPLUG_DIR}/src/load_mtm.cpp"
        "${MODPLUG_DIR}/src/load_okt.cpp"
        "${MODPLUG_DIR}/src/load_pat.cpp"
        "${MODPLUG_DIR}/src/load_psm.cpp"
        "${MODPLUG_DIR}/src/load_ptm.cpp"
        "${MODPLUG_DIR}/src/load_s3m.cpp"
        "${MODPLUG_DIR}/src/load_stm.cpp"
        "${MODPLUG_DIR}/src/load_ult.cpp"
        "${MODPLUG_DIR}/src/load_umx.cpp"
        "${MODPLUG_DIR}/src/load_wav.cpp"
        "${MODPLUG_DIR}/src/load_xm.cpp"
        "${MODPLUG_DIR}/src/mmcmp.cpp"
        "${MODPLUG_DIR}/src/modplug.cpp"
        "${MODPLUG_DIR}/src/snd_dsp.cpp"
        "${MODPLUG_DIR}/src/snd_flt.cpp"
        "${MODPLUG_DIR}/src/snd_fx.cpp"
        "${MODPLUG_DIR}/src/sndfile.cpp"
        "${MODPLUG_DIR}/src/sndmix.cpp" )

add_library( modplug STATIC ${SOURCES} )

#--------------
# Build libogg
#--------------

set( OGG_DIR "SDL2_mixer/external/libogg-1.3.2" )

set( CMAKE_C_FLAGS ${COMMON_C_FLAGS} )

include_directories( ${OGG_DIR}/include ${OGG_DIR}/android )

set( SOURCES
        "${OGG_DIR}/src/framing.c"
        "${OGG_DIR}/src/bitwise.c" )

add_library( ogg STATIC ${SOURCES} )

#---------------------
# Build libvorbisidec
#---------------------

set( VORBISIDEC_DIR "SDL2_mixer/external/libvorbisidec-1.2.1" )

set( CMAKE_C_FLAGS ${COMMON_C_FLAGS} )

if( ${ANDROID_ABI} EQUAL "armeabi-v7a" )
    add_definitions( -D_ARM_ASSEM_ )
endif()

set( SOURCES
        "${VORBISIDEC_DIR}/mdct.c"
        "${VORBISIDEC_DIR}/block.c"
        "${VORBISIDEC_DIR}/window.c"
        "${VORBISIDEC_DIR}/synthesis.c"
        "${VORBISIDEC_DIR}/info.c"
        "${VORBISIDEC_DIR}/floor1.c"
        "${VORBISIDEC_DIR}/floor0.c"
        "${VORBISIDEC_DIR}/vorbisfile.c"
        "${VORBISIDEC_DIR}/res012.c"
        "${VORBISIDEC_DIR}/mapping0.c"
        "${VORBISIDEC_DIR}/registry.c"
        "${VORBISIDEC_DIR}/codebook.c"
        "${VORBISIDEC_DIR}/sharedbook.c" )

add_library( vorbisidec STATIC ${SOURCES} )

target_link_libraries( vorbisidec
                       ogg )

#-----------------
# Build SDL_mixer
#-----------------

set( SDL_MIXER_DIR "SDL2_mixer" )

include_directories( ${SDL_MIXER_DIR} ../SDL2/SDL2/include/ ${VORBISIDEC_DIR} )

set( CMAKE_C_FLAGS "${COMMON_C_FLAGS} -DMUSIC_OGG -DOGG_USE_TREMOR -DOGG_HEADER=\"<ivorbisfile.h>\" -DMUSIC_MOD_MODPLUG -DMODPLUG_HEADER=\"<modplug.h>\""  )

file( GLOB SOURCES
        "${SDL_MIXER_DIR}/*.c" )
list( FILTER SOURCES EXCLUDE REGEX ".*playmus.c$" )
list( FILTER SOURCES EXCLUDE REGEX ".*playwave.c$" )

add_library( SDL2_mixer SHARED ${SOURCES} )

target_link_libraries( SDL2_mixer
                        SDL2
                        modplug
                        ogg
                        vorbisidec )
