#!/bin/sh
# Build libluajit.so for one Android ABI.
#
# usage: build-luajit.sh <abi> <outdir> <src> <ndk> <workdir> [api]
#
# <src>     - LuaJIT source tree (checked out)
# <outdir>  - where libluajit.so is placed
# <workdir> - scratch dir for the per-ABI build
# Cross-building LuaJIT requires a host C compiler (HOST_CC, default "cc").
set -e

ABI="$1"
OUT="$2"
SRC="$3"
NDK="$4"
WORK="$5"
API="${6:-25}"

case "$ABI" in
  arm64-v8a)   TRIPLE=aarch64-linux-android;     LJARCH=arm64 ;;
  x86_64)      TRIPLE=x86_64-linux-android;      LJARCH=x64 ;;
  armeabi-v7a) TRIPLE=armv7a-linux-androideabi;  LJARCH=arm ;;
  x86)         TRIPLE=i686-linux-android;        LJARCH=x86 ;;
  *) echo "unknown ABI: $ABI" >&2; exit 1 ;;
esac

# LuaJIT's buildvm requires the host and target pointer sizes to match, so
# 32-bit targets need a 32-bit host compiler (gcc-multilib on Debian/Ubuntu).
case "$ABI" in
  arm64-v8a|x86_64) HOST_CC="${HOST_CC:-cc}" ;;
  *)                HOST_CC="${HOST_CC:-gcc -m32}" ;;
esac

TC="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
CC="$TC/bin/${TRIPLE}${API}-clang"
AR="$TC/bin/llvm-ar"
STRIP="$TC/bin/llvm-strip"

BUILD="$WORK/$ABI"
rm -rf "$BUILD"
mkdir -p "$(dirname "$BUILD")"
cp -a "$SRC" "$BUILD"

make -C "$BUILD/src" -j"$(nproc)" \
  HOST_CC="$HOST_CC" \
  CC="$CC" \
  TARGET_CC="$CC" TARGET_LD="$CC" \
  TARGET_AR="$AR rcus" TARGET_STRIP="$STRIP" \
  TARGET_SYS=Linux TARGET_LJARCH="$LJARCH" \
  TARGET_SONAME=libluajit.so \
  TARGET_FLAGS="-O2 -fPIC" \
  TARGET_LDFLAGS="-Wl,-z,max-page-size=16384" \
  BUILDMODE=shared

mkdir -p "$OUT"
cp "$BUILD/src/libluajit.so" "$OUT/libluajit.so"
