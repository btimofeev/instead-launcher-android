
set(INSTEAD_SRC_DIR Instead/src)

include_directories( ${INSTEAD_SRC_DIR} ${INSTEAD_SRC_DIR}/instead ../Lua/Lua/src ${CMAKE_BINARY_DIR}/libiconv/libiconv/include
        ../SDL2/SDL2/include ../SDL2_mixer/SDL2_mixer ../SDL2_image/SDL2_image ../SDL2_ttf/SDL2_ttf )

set( SOURCES
        "instead_launcher.c"
        "${INSTEAD_SRC_DIR}/instead/cache.c"
        "${INSTEAD_SRC_DIR}/instead/idf.c"
        "${INSTEAD_SRC_DIR}/instead/instead.c"
        "${INSTEAD_SRC_DIR}/instead/lfs.c"
        "${INSTEAD_SRC_DIR}/instead/list.c"
        "${INSTEAD_SRC_DIR}/instead/snprintf.c"
        "${INSTEAD_SRC_DIR}/instead/tinymt32.c"
        "${INSTEAD_SRC_DIR}/instead/util.c"
        "${INSTEAD_SRC_DIR}/instead_bits.c"
        "${INSTEAD_SRC_DIR}/instead_paths.c"
        "${INSTEAD_SRC_DIR}/instead_sound.c"
        "${INSTEAD_SRC_DIR}/instead_sprites.c"
        "${INSTEAD_SRC_DIR}/instead_timer.c"
        "${INSTEAD_SRC_DIR}/graphics.c"
        "${INSTEAD_SRC_DIR}/input.c"
        "${INSTEAD_SRC_DIR}/game.c"
        "${INSTEAD_SRC_DIR}/main.c"
        "${INSTEAD_SRC_DIR}/sound.c"
        "${INSTEAD_SRC_DIR}/config.c"
        "${INSTEAD_SRC_DIR}/themes.c"
        "${INSTEAD_SRC_DIR}/menu.c"
        "${INSTEAD_SRC_DIR}/utils.c"
        "${INSTEAD_SRC_DIR}/unzip.c"
        "${INSTEAD_SRC_DIR}/ioapi.c"
        "${INSTEAD_SRC_DIR}/unpack.c"
        "${INSTEAD_SRC_DIR}/unix.c"
        "${INSTEAD_SRC_DIR}/SDL2_rotozoom.c"
        "${INSTEAD_SRC_DIR}/SDL_anigif.c"
        "${INSTEAD_SRC_DIR}/SDL_gfxBlitFunc.c"
        "${INSTEAD_SRC_DIR}/noise1234.c" )

set( CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DANDROID -DNOMAIN -D_USE_SDL -D_LOCAL_APPDATA -D_SDL_MOD_BUG -D_HAVE_ICONV" )
add_definitions( -DVERSION=\"3.2.1\" -DGAMES_PATH=\"./games/\" -DTHEME_PATH=\"./themes/\" -DLANG_PATH=\"./lang/\" -DSTEAD_PATH=\"./stead/\" )
add_definitions( -DSDL_JAVA_PACKAGE_PATH=org_emunix_insteadlauncher )

add_library( instead SHARED ${SOURCES} )

find_library( log-lib log )

target_link_libraries( instead
                       lua
                       iconv
                       SDL2
                       SDL2_ttf
                       SDL2_mixer
                       SDL2_image
                       ${log-lib} )
