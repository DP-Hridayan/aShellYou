cmake_minimum_required(VERSION 3.22.1)
project(ashell_llama)

# Build settings
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

# Enable Link-Time Optimization (ThinLTO) for smaller and faster binaries with Clang
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -flto=thin -DNDEBUG")
set(CMAKE_C_FLAGS_RELEASE "-O3 -flto=thin -DNDEBUG")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -flto=thin -DNDEBUG")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -flto=thin -DNDEBUG")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -flto=thin")

# llama.cpp configuration — disable features we don't need on Android
set(LLAMA_NATIVE OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)  # FP16 intrinsics unsupported on armv7

# Add llama.cpp as subdirectory
add_subdirectory(${CMAKE_SOURCE_DIR}/llama.cpp)

# JNI bridge library
add_library(ashell_llama SHARED ashell_llama_jni.cpp)

target_include_directories(ashell_llama PRIVATE
    ${CMAKE_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_SOURCE_DIR}/llama.cpp/ggml/include
)

target_link_libraries(ashell_llama
    llama
    ggml
    log
    android
)
