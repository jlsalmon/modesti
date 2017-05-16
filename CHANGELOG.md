# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN GitLab](https://gitlab.cern.ch/modesti/modesti/issues).
For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/modesti/modesti/milestones?state=all).

## [Unreleased]
### Added

### Changed

### Fixed


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


[0.2.12]: https://gitlab.cern.ch/modesti/modesti/milestones/11
[0.2.10]: https://gitlab.cern.ch/modesti/modesti/milestones/10