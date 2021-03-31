# Copyright (C) 2007-2012 LuaDist.
# Created by Peter Drahoš
# Redistribution and use of this file is allowed according to the terms of the MIT license.
# For details see the COPYRIGHT file distributed with LuaDist.
# Please note that the package source code is licensed under its own license.

project ( libiconv C )
cmake_minimum_required ( VERSION 2.8 )
include ( cmake/dist.cmake )
include ( configure )

# Options
option ( ENABLE_EXTRA "Enable a few rarely used encodings" OFF)
option ( ENABLE_NLS "Translation of program messages to the user's native language is requested" OFF)
#option ( ENABLE_RELOCATABLE "The package shall run at any location in the file system" ON )

# iconv.h
set ( USE_MBSTATE_T 1 )
set ( BROKEN_WCHAR_H 0 )
set ( HAVE_WCHAR_T 0)

# setup default include directories 
include_directories(${CMAKE_CURRENT_BINARY_DIR}/libiconv/include)

configure_file ( config.h.cmake ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/config.h )

iconv_configure_file (LIBRARY_NAME iconv 
  INPUT libiconv/include/iconv.h.build.in
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/iconv.h )

iconv_configure_file (LIBRARY_NAME iconv 
  INPUT libiconv/srclib/alloca.in.h
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/srclib/alloca.h )

iconv_configure_file (LIBRARY_NAME iconv 
  INPUT libiconv/include/iconv.h.build.in
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/iconv.h )

iconv_configure_file (LIBRARY_NAME iconv 
  INPUT libiconv/srclib/unistd.in.h
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/srclib/unistd.h @ONLY)

iconv_configure_file (LIBRARY_NAME iconv 
  INPUT libiconv/srclib/unitypes.in.h
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/srclib/unitypes.h )

iconv_configure_file (LIBRARY_NAME charset
  INPUT libiconv/libcharset/include/libcharset.h.in
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/libcharset.h )

iconv_configure_file (LIBRARY_NAME charset
  INPUT libiconv/libcharset/include/localcharset.h.build.in
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/localcharset.h )

# Dirty fix for MinGW
if ( MINGW )
  add_definitions ( -DELOOP=0 -DHAVE_DECL_STRERROR_R=0 )
  configure_file ( libiconv/srclib/alloca.in.h ${CMAKE_CURRENT_SOURCE_DIR}/libiconv/srclib/alloca.h )
endif ()

include_directories ( ${CMAKE_CURRENT_SOURCE_DIR} libiconv/include libiconv/srclib )
add_definitions ( -Dset_relocation_prefix=libcharset_set_relocation_prefix -Drelocate=libcharset_relocate -DHAVE_CONFIG_H -DINSTALLPREFIX=NULL -DNO_XMALLOC -DBUILDING_LIBCHARSET -DINSTALLDIR="" -DLIBDIR="" -DENABLE_RELOCATABLE=1 -DIN_LIBRARY )

# libcharset
set ( SRC_LIBCHARSET
        libiconv/libcharset/lib/localcharset.c
        libiconv/libcharset/lib/relocatable.c
)

add_library ( charset ${SRC_LIBCHARSET} )
if ( NOT MSVC )
  target_compile_options (charset PRIVATE -DBUILDING_DLL)
endif()
target_compile_options (charset PRIVATE -DBUILDING_LIBCHARSET)

if ( NOT MSVC )
  # libicrt
  set ( SRC_LIBICRT
          libiconv/srclib/allocator.c
          libiconv/srclib/areadlink.c
          libiconv/srclib/careadlinkat.c
          libiconv/srclib/malloca.c
          libiconv/srclib/progname.c
          libiconv/srclib/safe-read.c
          libiconv/srclib/uniwidth/width.c
          libiconv/srclib/xmalloc.c
          libiconv/srclib/xstrdup.c
          libiconv/srclib/xreadlink.c
          libiconv/srclib/canonicalize-lgpl.c
          libiconv/srclib/error.c
          libiconv/srclib/lstat.c
          libiconv/srclib/readlink.c
          libiconv/srclib/stat.c
          libiconv/srclib/strerror.c
          libiconv/srclib/strerror-override.c
  )
  add_library ( icrt STATIC ${SRC_LIBICRT} )
endif()
# libiconv
set ( SRC_LIBICONV
        libiconv/lib/iconv.c
)

add_library ( iconv ${SRC_LIBICONV} )
target_link_libraries ( iconv charset )
set_target_properties ( iconv PROPERTIES COMPILE_FLAGS -DBUILDING_LIBICONV)

#if(NOT MSVC)
#  add_executable ( iconvcli src/iconv_no_i18n.c )
#  target_link_libraries ( iconvcli iconv icrt charset )
#  set_target_properties ( iconvcli PROPERTIES OUTPUT_NAME iconv )

#  install_executable ( iconvcli )
#endif()

# fix library naming for Visual Studio compilers
#if(MSVC)
#  set_target_properties ( iconv   PROPERTIES OUTPUT_NAME "libiconv" )
#  set_target_properties ( charset PROPERTIES OUTPUT_NAME "libcharset" )
#endif()

#install_library ( iconv charset )
install_header ( ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/iconv.h )
install_header ( ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/libcharset.h )
install_header ( ${CMAKE_CURRENT_BINARY_DIR}/libiconv/include/localcharset.h )
#install_data ( README AUTHORS COPYING )