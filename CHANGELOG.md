# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN GitLab](https://gitlab.cern.ch/modesti/modesti/issues).
For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/modesti/modesti/milestones?state=all).


## [Unreleased]
### Added

### Changed

### Fixed


## [0.2.18] - 2017-11-02
### Fixed
- Resolved critical Exception when the point type is modified in CREATE request (#230). Got introduced with last version.


## [0.2.17] - 2017-11-01
### Added
- Filter criteria are now kept, even when using the browser back button (#187)
- Added support for change of equipment type (modesti-tim-refdb-common#75)
- SonarCube integration

### Fixed
- Provide feedback to unauthorized users who try to create update/delete requests (#226)
- Logging value deadband is now correctly displayed in TIM domain (#216)


## [0.2.16] - 2017-09-04
### Added
- Handle unexpected errors during validation phase (#220)
- Allow to modify the request creator (#217)
- Modified schema to allow for an editable option to be specified for only certain request types

### Fixed
- Fixed issue for requests not passing stage `FOR_CSAM_SYNC` when JSON request contains unexpected Error format
- Handle the case when there is no original value for a table cell


## [0.2.15] - 2017-07-12
### Fixed
- Fixed filter by value deadband, which was not correctly displayed in TIM domain (#213)


## [0.2.14] - 2017-07-05
### Changed
- Improved calculation speed of change history for big UPDATE requests (#211)


## 0.2.13 - 2017-06-16
### Changed
- Create change history only in status `IN_PROGRESS` and `FOR_ADDRESSING` 

### Fixed
- Fixed handsontable-pro version in package.json

### Removed
- Removed commented files that were migrated to modesti-api


## [0.2.12] - 2017-05-16
### Added
- Show a link per plugin to workflow documentation on the request page (#44)
- Allow to filter with "contains" on all search fields for creating new UPDATE/DELETE requests (#207) 

### Changed
- Allow frontend to load without internet connection, e.g. on TN (#177)

### Fixed
- Corrected link of "About MODESTI" button (#178)
- Fixed problem with "Delete row' on Update request (#206)


## [0.2.11] - 2017-03-29
### Fixed
- Set 'angular-sanitize' version to 1.6.2 due to incompatibility with v1.6.3


## [0.2.10] - 2017-03-29
### Added
- Log message that shows which plugins are loaded at startup

### Changed
- Link for "Report an issue" on the help menu (#189)
- Fix link of "About Modesti" button in help menu (#178)

### Fixed
- Fixed pagination problem in configuration browsing, which showed twice the first page (#194)
- Avoid NPE when no change history entry was found


[Unreleased]: https://gitlab.cern.ch/modesti/modesti/milestones/17
[0.2.18]: https://gitlab.cern.ch/modesti/modesti/milestones/16
[0.2.17]: https://gitlab.cern.ch/modesti/modesti/milestones/15
[0.2.16]: https://gitlab.cern.ch/modesti/modesti/milestones/14
[0.2.15]: https://gitlab.cern.ch/modesti/modesti/milestones/13
[0.2.14]: https://gitlab.cern.ch/modesti/modesti/milestones/12
[0.2.12]: https://gitlab.cern.ch/modesti/modesti/milestones/11
[0.2.10]: https://gitlab.cern.ch/modesti/modesti/milestones/10