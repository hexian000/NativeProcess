cmake_minimum_required(VERSION 3.6)
project(tasks C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -pedantic -Wall -Wextra -Werror")

add_executable(libtasks.so main.c read_pid.h read_pid.c procinfo.h)
