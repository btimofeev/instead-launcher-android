
set(INSTEAD_SRC_DIR Instead/src)

include_directories( ${INSTEAD_SRC_DIR} ${INSTEAD_SRC_DIR}/instead ../LuaJIT/src ${CMAKE_BINARY_DIR}/libiconv/libiconv/include
        ../SDL3/SDL3/include ../SDL3_mixer/SDL3_mixer/include ../SDL3_image/SDL3_image/include ../SDL3_ttf/SDL3_ttf/include )

set( SOURCES
        "instead_launcher.c"
        "${INSTEAD_SRC_DIR}/instead/cache.c"
        "${INSTEAD_SRC_DIR}/instead/idf.c"
        "${INSTEAD_SRC_DIR}/instead/instead.c"
        "${INSTEAD_SRC_DIR}/instead/lfs.c"
        "${INSTEAD_SRC_DIR}/instead/list.c"
        "${INSTEAD_SRC_DIR}/instead/snprintf.c"
        "${INSTEAD_SRC_DIR}/instead/util.c"
        "${INSTEAD_SRC_DIR}/instead/xoshiro128.c"
        "${INSTEAD_SRC_DIR}/instead_bits.c"
        "${INSTEAD_SRC_DIR}/instead_paths.c"
        "${INSTEAD_SRC_DIR}/instead_sound.c"
        "${INSTEAD_SRC_DIR}/instead_sprites.c"
        "${INSTEAD_SRC_DIR}/instead_timer.c"
        "${INSTEAD_SRC_DIR}/graphics.c"
        "${INSTEAD_SRC_DIR}/input.c"
        "${INSTEAD_SRC_DIR}/game.c"
        "${INSTEAD_SRC_DIR}/dialog.c"
        "${INSTEAD_SRC_DIR}/main.c"
        "${INSTEAD_SRC_DIR}/sound.c"
        "${INSTEAD_SRC_DIR}/config.c"
        "${INSTEAD_SRC_DIR}/themes.c"
        "${INSTEAD_SRC_DIR}/menu.c"
        "${INSTEAD_SRC_DIR}/utils.c"
        "${INSTEAD_SRC_DIR}/unix.c"
        "${INSTEAD_SRC_DIR}/SDL3_rotozoom.c"
        "${INSTEAD_SRC_DIR}/SDL3_gif.c"
        "${INSTEAD_SRC_DIR}/SDL3_gfxBlitFunc.c"
        "${INSTEAD_SRC_DIR}/noise1234.c" )

add_definitions( -DANDROID -DNOMAIN -D_USE_SDL -D_LOCAL_APPDATA -D_HAVE_ICONV )
add_definitions( -DSDL_ENABLE_OLD_NAMES )
add_definitions( -DVERSION=\"3.6.0\" -DGAMES_PATH=\"./games/\" -DTHEMES_PATH=\"./themes/\" -DLANG_PATH=\"./lang/\" -DSTEAD_PATH=\"./stead/\" )
add_definitions( -DSDL_JAVA_PACKAGE_PATH=org_libsdl_app )

set( CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-incompatible-function-pointer-types" )

add_library( instead SHARED ${SOURCES} )

find_library( log-lib log )

target_link_libraries( instead
                       luajit
                       iconv
                       SDL3::SDL3
                       SDL3_ttf::SDL3_ttf
                       SDL3_mixer::SDL3_mixer
                       SDL3_image::SDL3_image
                       ${log-lib} )
