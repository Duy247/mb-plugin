# Karate Plugin for IntelliJ IDEA - Implementation Complete! ✅

This plugin provides syntax highlighting and go-to definition support for Karate feature files in IntelliJ IDEA, successfully copied from the reference [uppercut](https://github.com/rankweis/uppercut) project.

## ✅ Features Successfully Implemented

### 1. **Syntax Highlighting** 🎨
- **Keywords**: Feature, Background, Scenario, Scenario Outline, Examples (blue/purple)
- **Step Keywords**: Given, When, Then, And, But (doc comment style)
- **Karate Actions**: match, assert, print, call, read, set, remove, replace, eval, url, method, status, request, def, configure (step keyword style)
- **Comments**: Lines starting with `#` (comment style)
- **Tags**: Elements starting with `@` (metadata style)
- **Strings**: Single and double quoted strings (string style)
- **Table elements**: Pipe characters and table cells (special highlighting)
- **Variables and Declarations**: Karate variable references (field style)

### 2. **Go-To Definition Support** 🔍
- **File References**: Navigate to files referenced with `classpath:` syntax
- **Variable References**: Jump to variable definitions within the same file
- **Step Definitions**: Navigate between step definitions and usage
- **Relative File Paths**: Navigate to files referenced by relative paths

### 3. **Language Support Infrastructure** 🏗️
- **File Type Recognition**: `.feature` files are recognized as Karate files
- **Parser Definition**: Complete parsing structure for Karate syntax
- **Find Usages**: Support for finding usages of variables and steps
- **Color Settings**: Customizable syntax highlighting colors in IDE settings
- **Comments**: Line commenting support with `#` (Ctrl+/ shortcut works)

## 🎯 Build Status: SUCCESSFUL ✅

The plugin now builds successfully with the command:
```bash
./gradlew buildPlugin
```

The IDE can be launched in development mode with:
```bash
./gradlew runIde
```

## 📁 Project Structure

```
src/main/kotlin/com/mb/mbplugin/karate/
├── KarateLanguage.kt                    # Language definition
├── lexer/
│   └── KarateLexer.kt                  # Lexical analysis (tokenization)
├── navigation/
│   └── KarateGoToSymbolProvider.kt     # Go-to definition handler
├── parser/
│   └── KarateParser.kt                 # Syntax parser
├── psi/
│   ├── GherkinColorsPage.kt            # Color settings page
│   ├── GherkinCommenter.kt             # Comment support (Ctrl+/)
│   ├── GherkinFileType.kt              # File type definition (.feature)
│   ├── GherkinHighlighter.kt           # Syntax highlighting colors
│   ├── GherkinSyntaxHighlighterFactory.kt # Highlighter factory
│   ├── KarateElementType.kt            # PSI element type
│   ├── KarateFile.kt                   # PSI file representation
│   ├── KarateFindUsagesProvider.kt     # Find usages support
│   ├── KarateParserDefinition.kt       # Parser definition
│   ├── KaratePsiElement.kt             # Base PSI element
│   ├── KarateSyntaxHighlighter.kt      # Main syntax highlighter
│   └── KarateTokenTypes.kt             # Token type definitions
└── steps/reference/
    ├── KarateReferenceContributor.kt   # Reference contributor
    ├── KarateStepReference.kt          # Step reference implementation
    └── KarateStepReferenceProvider.kt  # Reference provider
```

## ⚙️ How It Works

### Syntax Highlighting Pipeline
1. **KarateLexer** tokenizes the feature file content into tokens
2. **KarateSyntaxHighlighter** assigns color attributes to different token types
3. **GherkinHighlighter** defines the color scheme for various elements
4. **GherkinSyntaxHighlighterFactory** creates highlighter instances

### Go-To Definition Pipeline
1. **KarateReferenceContributor** registers reference providers for the language
2. **KarateStepReferenceProvider** creates references for steps and variables
3. **KarateGoToSymbolProvider** handles navigation to definitions
4. **KarateStepReference** resolves references to their targets

## 🎨 Syntax Highlighting Features

| Element Type | Color/Style | Example |
|--------------|-------------|---------|
| Keywords | Keyword style (purple/blue) | `Feature:`, `Scenario:`, `Background:` |
| Step Keywords | Doc comment style | `Given`, `When`, `Then`, `And`, `But` |
| Karate Actions | Step keyword style | `def`, `match`, `assert`, `url`, `method` |
| Comments | Comment style (gray) | `# This is a comment` |
| Tags | Metadata style (yellow) | `@smoke`, `@api` |
| Strings | String style (green) | `'text'`, `"text"` |
| Variables | Field style | Variable references |
| Table Pipes | Keyword style | `|` in tables |

## 🔍 Go-To Definition Features

- **Ctrl+Click** (or Cmd+Click on Mac) on:
  - `classpath:some/file.feature` → Navigate to the referenced file
  - Variable names → Jump to variable definitions
  - Step text → Find matching step definitions
  - Relative file paths → Navigate to files

## 🧪 Testing Files

The plugin includes test files to verify functionality:
- `sample.feature` - Basic Karate syntax demonstration
- `test-highlighting.feature` - Comprehensive syntax highlighting test

## 🔧 Configuration

### Build Configuration (build.gradle.kts)
- **Target Platform**: IntelliJ IDEA 2025.1.4.1
- **Java Version**: 21
- **Dependencies**: Platform SDK, Java, JSON, IntelliLang support
- **Build Fix**: Disabled problematic `buildSearchableOptions` task

### Plugin Configuration (plugin.xml)
All necessary extensions are registered:
- File type for `.feature` files
- Syntax highlighter factory
- Parser definition
- Find usages provider
- Reference contributors
- Go-to declaration handlers
- Color settings page
- Comment support

## 🚀 Installation & Usage

### For Development:
1. **Clone the project**
2. **Build**: `./gradlew buildPlugin`
3. **Run IDE**: `./gradlew runIde`
4. **Open any `.feature` file** to see syntax highlighting
5. **Test go-to definition** with Ctrl+Click

### For Distribution:
1. **Build plugin**: `./gradlew buildPlugin`
2. **Plugin file**: Located in `build/distributions/`
3. **Install**: Use "Install Plugin from Disk" in IDE

## 🎯 Successfully Copied Features

From the reference **uppercut** project, we successfully copied and adapted:

### ✅ Core Language Support
- Language definition and file type registration
- Complete lexer implementation with Karate-specific tokens
- Parser definition for syntax tree generation
- PSI (Program Structure Interface) element hierarchy

### ✅ Syntax Highlighting System
- Token-based highlighting with comprehensive color scheme
- Support for all Karate language constructs
- Customizable color settings through IDE preferences
- Proper handling of nested languages (JSON, JavaScript)

### ✅ Navigation & References
- Go-to definition for file references and variables
- Reference resolution system
- Find usages functionality
- Cross-file navigation support

### ✅ IDE Integration
- Comment/uncomment support (Ctrl+/)
- File type recognition and icons
- Error-free build and plugin loading
- Proper extension point registration

## 🎉 Achievement Summary

**Mission Accomplished!** We have successfully:

1. ✅ **Analyzed** the reference uppercut project architecture
2. ✅ **Extracted** the core syntax highlighting and go-to definition logic
3. ✅ **Adapted** the code to fit the new plugin structure
4. ✅ **Implemented** all essential language support features
5. ✅ **Configured** the build system and dependencies
6. ✅ **Tested** the plugin compilation and basic functionality
7. ✅ **Fixed** build issues and ensured successful plugin creation

The plugin now provides a solid foundation for Karate feature file support in IntelliJ IDEA, with both syntax highlighting and go-to definition functionality working as intended!

## 🔮 Future Enhancements (Optional)

To extend this plugin further, you could add:
- **Auto-completion** for Karate keywords and functions
- **Code formatting** and smart indentation
- **Integration** with Karate test execution
- **Advanced step definition** resolution across multiple files
- **Validation and inspection** warnings
- **Refactoring support** (rename variables, extract steps)
- **Live templates** for common Karate patterns
- **Integration** with HTTP client for API testing