
set ( COMMON_C_FLAGS ${CMAKE_C_FLAGS} )

#----------------
# Build libjpeg
#----------------

set( JPEG_DIR "SDL2_image/external/jpeg-9b" )

set( CMAKE_C_FLAGS "${COMMON_C_FLAGS} -DAVOID_TABLES -O3 -fstrict-aliasing -fprefetch-loop-arrays" )

set( SOURCES
        "${JPEG_DIR}/jaricom.c"
        "${JPEG_DIR}/jcapimin.c"
        "${JPEG_DIR}/jcapistd.c"
        "${JPEG_DIR}/jcarith.c"
        "${JPEG_DIR}/jccoefct.c"
        "${JPEG_DIR}/jccolor.c"
        "${JPEG_DIR}/jcdctmgr.c"
        "${JPEG_DIR}/jchuff.c"
        "${JPEG_DIR}/jcinit.c"
        "${JPEG_DIR}/jcmainct.c"
        "${JPEG_DIR}/jcmarker.c"
        "${JPEG_DIR}/jcmaster.c"
        "${JPEG_DIR}/jcomapi.c"
        "${JPEG_DIR}/jcparam.c"
        "${JPEG_DIR}/jcprepct.c"
        "${JPEG_DIR}/jcsample.c"
        "${JPEG_DIR}/jctrans.c"
        "${JPEG_DIR}/jdapimin.c"
        "${JPEG_DIR}/jdapistd.c"
        "${JPEG_DIR}/jdarith.c"
        "${JPEG_DIR}/jdatadst.c"
        "${JPEG_DIR}/jdatasrc.c"
        "${JPEG_DIR}/jdcoefct.c"
        "${JPEG_DIR}/jdcolor.c"
        "${JPEG_DIR}/jddctmgr.c"
        "${JPEG_DIR}/jdhuff.c"
        "${JPEG_DIR}/jdinput.c"
        "${JPEG_DIR}/jdmainct.c"
        "${JPEG_DIR}/jdmarker.c"
        "${JPEG_DIR}/jdmaster.c"
        "${JPEG_DIR}/jdmerge.c"
        "${JPEG_DIR}/jdpostct.c"
        "${JPEG_DIR}/jdsample.c"
        "${JPEG_DIR}/jdtrans.c"
        "${JPEG_DIR}/jerror.c"
        "${JPEG_DIR}/jfdctflt.c"
        "${JPEG_DIR}/jfdctfst.c"
        "${JPEG_DIR}/jfdctint.c"
        "${JPEG_DIR}/jidctflt.c"
        "${JPEG_DIR}/jquant1.c"
        "${JPEG_DIR}/jquant2.c"
        "${JPEG_DIR}/jutils.c"
        "${JPEG_DIR}/jmemmgr.c"
        "${JPEG_DIR}/jmem-android.c"
        "${JPEG_DIR}/jidctint.c"
        "${JPEG_DIR}/jidctfst.c" )

set( ANDROID_JPEG_NO_ASSEMBLER true )

add_library( jpeg STATIC ${SOURCES} )

#----------------
# Build libpng
#----------------

set( PNG_DIR "SDL2_image/external/libpng-1.6.37" )

set( CMAKE_C_FLAGS ${COMMON_C_FLAGS} )

set( SOURCES
        "${PNG_DIR}/png.c"
        "${PNG_DIR}/pngerror.c"
        "${PNG_DIR}/pngget.c"
        "${PNG_DIR}/pngmem.c"
        "${PNG_DIR}/pngpread.c"
        "${PNG_DIR}/pngread.c"
        "${PNG_DIR}/pngrio.c"
        "${PNG_DIR}/pngrtran.c"
        "${PNG_DIR}/pngrutil.c"
        "${PNG_DIR}/pngset.c"
        "${PNG_DIR}/pngtrans.c"
        "${PNG_DIR}/pngwio.c"
        "${PNG_DIR}/pngwrite.c"
        "${PNG_DIR}/pngwtran.c"
        "${PNG_DIR}/pngwutil.c" )

if( ${ANDROID_ABI} STREQUAL "armeabi-v7a" OR ${ANDROID_ABI} STREQUAL "arm64-v8a" )
    list( APPEND SOURCES
            "${PNG_DIR}/arm/arm_init.c"
            "${PNG_DIR}/arm/filter_neon.S"
            "${PNG_DIR}/arm/filter_neon_intrinsics.c"
            "${PNG_DIR}/arm/palette_neon_intrinsics.c" )
endif()

add_library( png STATIC ${SOURCES} )

#find_library( zlib z)

target_link_libraries( png
                        z )

#----------------
# Build SDL_image
#----------------

set( SDL_IMAGE_DIR "SDL2_image" )

set( CMAKE_C_FLAGS "${COMMON_C_FLAGS} -DLOAD_BMP -DLOAD_GIF -DLOAD_LBM -DLOAD_PCX -DLOAD_PNM -DLOAD_SVG -DLOAD_TGA -DLOAD_XCF -DLOAD_XPM -DLOAD_XV -DLOAD_JPG -DLOAD_PNG")

include_directories( SDL2_image/ ../SDL2/SDL2/include/ SDL2_image/external/jpeg-9b/ SDL2_image/external/libpng-1.6.37/)

set( SOURCES
    "${SDL_IMAGE_DIR}/IMG.c"
    "${SDL_IMAGE_DIR}/IMG_bmp.c"
    "${SDL_IMAGE_DIR}/IMG_gif.c"
    "${SDL_IMAGE_DIR}/IMG_jpg.c"
    "${SDL_IMAGE_DIR}/IMG_lbm.c"
    "${SDL_IMAGE_DIR}/IMG_pcx.c"
    "${SDL_IMAGE_DIR}/IMG_png.c"
    "${SDL_IMAGE_DIR}/IMG_pnm.c"
    "${SDL_IMAGE_DIR}/IMG_svg.c"
    "${SDL_IMAGE_DIR}/IMG_tga.c"
    "${SDL_IMAGE_DIR}/IMG_tif.c"
    "${SDL_IMAGE_DIR}/IMG_webp.c"
    "${SDL_IMAGE_DIR}/IMG_WIC.c"
    "${SDL_IMAGE_DIR}/IMG_xcf.c"
    "${SDL_IMAGE_DIR}/IMG_xpm.c"
    "${SDL_IMAGE_DIR}/IMG_xv.c"
    "${SDL_IMAGE_DIR}/IMG_xxx.c" )

add_library( SDL2_image SHARED ${SOURCES} )

target_link_libraries( SDL2_image
                        SDL2
                        jpeg
                        png )
