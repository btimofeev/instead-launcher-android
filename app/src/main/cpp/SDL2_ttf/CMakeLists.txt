
#----------------
# Build FreeType
#----------------

set( FREETYPE_DIR "SDL2_ttf/external/freetype-2.10.1" )

set( CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DFT2_BUILD_LIBRARY -Os" )

include_directories( "${FREETYPE_DIR}/include/" )

set( SOURCES
        "${FREETYPE_DIR}/src/autofit/autofit.c"
        "${FREETYPE_DIR}/src/base/ftbase.c"
        "${FREETYPE_DIR}/src/base/ftbbox.c"
        "${FREETYPE_DIR}/src/base/ftbdf.c"
        "${FREETYPE_DIR}/src/base/ftbitmap.c"
        "${FREETYPE_DIR}/src/base/ftcid.c"
        "${FREETYPE_DIR}/src/base/ftdebug.c"
        "${FREETYPE_DIR}/src/base/ftfstype.c"
        "${FREETYPE_DIR}/src/base/ftgasp.c"
        "${FREETYPE_DIR}/src/base/ftglyph.c"
        "${FREETYPE_DIR}/src/base/ftgxval.c"
        "${FREETYPE_DIR}/src/base/ftinit.c"
        "${FREETYPE_DIR}/src/base/ftlcdfil.c"
        "${FREETYPE_DIR}/src/base/ftmm.c"
        "${FREETYPE_DIR}/src/base/ftotval.c"
        "${FREETYPE_DIR}/src/base/ftpatent.c"
        "${FREETYPE_DIR}/src/base/ftpfr.c"
        "${FREETYPE_DIR}/src/base/ftstroke.c"
        "${FREETYPE_DIR}/src/base/ftsynth.c"
        "${FREETYPE_DIR}/src/base/ftsystem.c"
        "${FREETYPE_DIR}/src/base/fttype1.c"
        "${FREETYPE_DIR}/src/base/ftwinfnt.c"
        "${FREETYPE_DIR}/src/bdf/bdf.c"
        "${FREETYPE_DIR}/src/bzip2/ftbzip2.c"
        "${FREETYPE_DIR}/src/cache/ftcache.c"
        "${FREETYPE_DIR}/src/cff/cff.c"
        "${FREETYPE_DIR}/src/cid/type1cid.c"
        "${FREETYPE_DIR}/src/gzip/ftgzip.c"
        "${FREETYPE_DIR}/src/lzw/ftlzw.c"
        "${FREETYPE_DIR}/src/pcf/pcf.c"
        "${FREETYPE_DIR}/src/pfr/pfr.c"
        "${FREETYPE_DIR}/src/psaux/psaux.c"
        "${FREETYPE_DIR}/src/pshinter/pshinter.c"
        "${FREETYPE_DIR}/src/psnames/psmodule.c"
        "${FREETYPE_DIR}/src/raster/raster.c"
        "${FREETYPE_DIR}/src/sfnt/sfnt.c"
        "${FREETYPE_DIR}/src/smooth/smooth.c"
        "${FREETYPE_DIR}/src/tools/apinames.c"
        "${FREETYPE_DIR}/src/truetype/truetype.c"
        "${FREETYPE_DIR}/src/type1/type1.c"
        "${FREETYPE_DIR}/src/type42/type42.c"
        "${FREETYPE_DIR}/src/winfonts/winfnt.c" )

add_library( freetype STATIC ${SOURCES} )


#----------------
# Build HarfBuzz
#----------------

set( HARFBUZZ_DIR "SDL2_ttf/external/harfbuzz-2.3.1" )

include_directories( "${HARFBUZZ_DIR}/src/" )
include_directories( "${HARFBUZZ_DIR}/src/hb-ucdn/" )

add_definitions("-DTTF_USE_HARFBUZZ=1")
add_definitions("-DHAVE_UCDN=1")

set( SOURCES
        "${HARFBUZZ_DIR}/src/hb-blob.cc"
        "${HARFBUZZ_DIR}/src/hb-buffer-serialize.cc"
        "${HARFBUZZ_DIR}/src/hb-face.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-tag.cc"
        "${HARFBUZZ_DIR}/src/hb-buffer.cc"
        "${HARFBUZZ_DIR}/src/hb-font.cc"
        "${HARFBUZZ_DIR}/src/hb-common.cc"
        "${HARFBUZZ_DIR}/src/hb-set.cc"
        "${HARFBUZZ_DIR}/src/hb-shape-plan.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-font.cc"
        "${HARFBUZZ_DIR}/src/hb-shaper.cc"
        "${HARFBUZZ_DIR}/src/hb-unicode.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-face.cc"
        "${HARFBUZZ_DIR}/src/hb-shape.cc"
        "${HARFBUZZ_DIR}/src/hb-static.cc"
        "${HARFBUZZ_DIR}/src/hb-warning.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-default.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-layout.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-math.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-map.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-arabic.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-hangul.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-hebrew.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-indic.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-indic-table.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-khmer.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-myanmar.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-thai.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-use.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-use-table.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-normalize.cc"
        "${HARFBUZZ_DIR}/src/hb-fallback-shape.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-fallback.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-shape-complex-vowel-constraints.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-var.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-cff1-table.cc"
        "${HARFBUZZ_DIR}/src/hb-ot-cff2-table.cc"
        "${HARFBUZZ_DIR}/src/hb-ft.cc"
        "${HARFBUZZ_DIR}/src/hb-ucdn.cc"
        "${HARFBUZZ_DIR}/src/hb-ucdn/ucdn.c"
        "${HARFBUZZ_DIR}/src/hb-aat-layout.cc"
        "${HARFBUZZ_DIR}/src/hb-aat-map.cc" )

add_library( harfbuzz STATIC ${SOURCES} )
#add_library( harfbuzz)

#----------------
# Build SDL_ttf
#----------------

#include_directories( SDL2_ttf/ ../SDL2/SDL2/include/ SDL2_ttf/external/harfbuzz-2.3.1/ "${HARFBUZZ_DIR}/src/" "${HARFBUZZ_DIR}/src/hb-ucdn/")

add_library( SDL2_ttf SHARED SDL2_ttf/SDL_ttf.c )

target_link_libraries( SDL2_ttf
                        SDL2
                        freetype 
                        harfbuzz)
