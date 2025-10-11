# MBKarate for Autotest

A powerful IntelliJ IDEA plugin that enhances your Karate testing experience with intelligent autocomplete and navigation features.

## Installation

Install directly from the JetBrains Plugin Marketplace:

**[MBKarate for Autotest](https://plugins.jetbrains.com/plugin/28613-mbkarate-for-autotest)**

Or install from within IntelliJ IDEA:
1. Go to `File` → `Settings` → `Plugins`
2. Search for "MBKarate for Autotest"
3. Click `Install`

## Features

- **Smart Autocomplete**: Intelligent suggestions for Karate scenarios and feature files
- **Tag Completion**: Use `!` to trigger tag-based autocomplete for scenario references
- **File Completion**: Use `#` to trigger file-based autocomplete for feature file references
- **Navigation Support**: Easy navigation between feature files and scenarios
- **Test Runner Integration**: Quick test execution with gutter icons

## Usage

### Tag Completion
Type `!` followed by a tag name to get suggestions:
```gherkin
!login    # → * call read('classpath:path/to/feature@login')
```

### File Completion
Type `#` followed by a file name to get suggestions:
```gherkin
#auth     # → * call read('classpath:path/to/auth.feature')
```

## License

See [LICENSE](LICENSE) file for details.

## Issues & Feedback

Found a bug or have a feature request? Please report it on the [JetBrains Plugin page](https://plugins.jetbrains.com/plugin/28613-mbkarate-for-autotest) or create an issue in this repository.

