# Changelog

All notable changes to the Performance Automation Framework will be documented in this file.

## [Unreleased]

### Added
- Enhanced error reporting in TestEngine class
- Additional workflows for verifying security fixes
- New documentation for recent fixes and updates

### Fixed
- ConfigManager security vulnerability (MS_EXPOSE_REP) using enum-based singleton pattern and SpotBugs exclude configuration
- TestEngine.java compilation error by replacing invalid getError() call with getErrorCode() and getErrorMessage()
- Multiple EI_EXPOSE_REP and EI_EXPOSE_REP2 vulnerabilities with defensive copying

### Changed
- Updated documentation to reflect recent security fixes
- Improved build process to include specific verification steps

## [1.0.0] - 2025-03-15

### Added
- Initial release of the Performance Automation Framework
- Multi-protocol support (HTTP, GraphQL, SOAP/XML, JDBC)
- Entity-Component-System (ECS) architecture
- YAML configuration-driven testing
- Unified CLI tool (perftest.sh)
- Advanced reporting capabilities
- JMeter DSL integration
- Comprehensive JUnit support