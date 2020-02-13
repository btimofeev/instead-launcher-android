# Changelog

## [Unreleased]
### Added
### Changed
- faster detection of locally installed games
- turned on ARM NEON optimizations for libpng
### Fixed
- choose custom game themes in the settings

## [0.8.1] - 2019-11-20
### Fixed
This version includes patches from the INSTEAD master branch 

- fixed a bug where the image was redrawn incorrectly when the screen is rotated (https://github.com/instead-hub/instead/commit/2178375716e628126b18dd6c88b6c5c087863385)
- games start again in full screen mode (https://github.com/instead-hub/instead/commit/03c4fcb9454d6d9a7185488ffb74a93605ceb678)

## [0.8] - 2019-10-15
### Added
- update repository in background once a day (default on)
- update repository on opening (default off)
- support Android 10
- dark theme
- animations when changing activities
- placeholder images (thanks to [NEUD](https://vk.com/neudd))

### Changed
- update INSTEAD 3.3.1
- update SDL 2.0.10 (and patch https://hg.libsdl.org/SDL/rev/131ea7dcc225), SDL2_image 2.0.5, SDL2_mixer 2.0.4, SDL2_ttf 2.0.15

### Fixed
- fixed crash when updating the repository

## [0.7.1] - 2019-06-10
### Added
- added animation of game list items

### Fixed
- fixed crash on Android 5, 6, 7

## [0.7] - 2019-04-26
### Added
- the ability to install games from zip-archives
- the application scans the directory for new or deleted games

### Changed
- improved notifications

### Fixed
- fixed incorrect display of images in the list
- fixed possible service killing by the system

## [0.6] - 2019-04-06
### Changed
- INSTEAD updated to version 3.3.0
- games in the repository are sorted by the date of the last update
- games now run in a separate unix process
- keyboard input switched to using standard SDL keyboard
- changed the default setting: text size in games was 150%, became 130%
- improved English translation (thanks to [@Minoru](https://github.com/Minoru))

### Fixed
- fixed incorrect display of escaped characters in the author field
- fixed incorrect display of the label of the new version of the game
- fixed crash after clearing sandbox address

## [0.5] - 2019-02-20
### Added
- search in repository activity
- swipe to refresh in repository
- add drawing to main screen (thanks to [NEUD](https://vk.com/neudd))

## [0.4] - 2019-02-19
### Added
- about activity

### Fixed
- fixed INSTEAD version flag

## [0.3.3] - 2019-02-08
### Added
- x86_64 build
- empty view in installed games activity
- added reload button in repository activity

### Changed
- default setting: the keyboard icon is displayed in the lower left corner of the screen
- automatic loading of games when the repository is empty

### Fixing
- fix unzip files
- fix build on ndk19

## [0.3.2] - 2019-02-03
### Added
- added collapsing titlebar in game activity

### Changed
- slightly improved icon
