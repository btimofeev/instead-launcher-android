
include_directories( Lua/src/ )

add_library( lua SHARED
            Lua/src/lapi.c
            Lua/src/lcode.c
            Lua/src/ldebug.c
            Lua/src/ldo.c
            Lua/src/ldump.c
            Lua/src/lfunc.c
            Lua/src/lgc.c
            Lua/src/llex.c
            Lua/src/lmem.c
            Lua/src/lobject.c
            Lua/src/lopcodes.c
            Lua/src/lparser.c
            Lua/src/lstate.c
            Lua/src/lstring.c
            Lua/src/ltable.c
            Lua/src/ltm.c
            Lua/src/lundump.c
            Lua/src/lvm.c
            Lua/src/lzio.c
            Lua/src/lauxlib.c
            Lua/src/lbaselib.c
            Lua/src/ldblib.c
            Lua/src/liolib.c
            Lua/src/lmathlib.c
            Lua/src/loslib.c
            Lua/src/lstrlib.c
            Lua/src/ltablib.c
            Lua/src/loadlib.c
            Lua/src/linit.c )
