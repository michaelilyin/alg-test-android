# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

include_directories(src/main/cpp/include/)

add_library( cl_test SHARED src/main/cpp/cl_test.cpp )

add_library( lib_ocl SHARED IMPORTED )
set_target_properties(lib_ocl PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/thirdparty/libPVROCL.so)

find_library( log-lib log )

target_link_libraries( cl_test ${log-lib} lib_ocl )
