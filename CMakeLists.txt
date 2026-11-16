cmake_minimum_required(VERSION 3.16)
project(EmergencyServicePrioritizer LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

# Try Qt 6 first, fallback to Qt 5 for older systems / Linux distributions
find_package(Qt6 COMPONENTS Widgets QUIET)
if(Qt6_FOUND)
    message(STATUS "Found Qt6: using Qt6::Widgets")
    set(QT_LIBRARIES Qt6::Widgets)
else()
    find_package(Qt5 COMPONENTS Widgets REQUIRED)
    message(STATUS "Found Qt5: using Qt5::Widgets")
    set(QT_LIBRARIES Qt5::Widgets)
endif()

add_executable(EmergencyServicePrioritizer
    main.cpp
)

target_link_libraries(EmergencyServicePrioritizer PRIVATE ${QT_LIBRARIES})

# Windows GUI subsystem flag to prevent console window popping up in release builds
if(WIN32 AND CMAKE_BUILD_TYPE STREQUAL "Release")
    set_target_properties(EmergencyServicePrioritizer PROPERTIES WIN32_EXECUTABLE TRUE)
endif()
