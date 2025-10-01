# Jira Integration Feature

## Overview
The MBKarate plugin now includes Jira integration that allows you to quickly view Jira issues directly from your feature files.

## Features
- **Hover + Intention Action**: Place your cursor on any `@MBA-XXXXX` tag and use Alt+Enter to see the "🔗 Open Jira Issue" option
- **Gutter Icon**: Look for the Jira icon (J) in the gutter next to lines with Jira issue tags - click to open
- **Default Browser Integration**: Opens issues in your default browser (Chrome, Firefox, Edge, etc.)
- **No Login Issues**: Uses your browser's existing login session - no credential management needed
- **Configurable Base URL**: Set your company's Jira base URL once in the settings

## Setup

### 1. Configure Jira Base URL
1. Go to **File** > **Settings** (or **IntelliJ IDEA** > **Preferences** on macOS)
2. Navigate to **Tools** > **Jira Integration**
3. Enter your Jira base URL (e.g., `https://mycompanyjira.atlassian.net`)
4. Click **Apply** and **OK**

### 2. Usage

#### Method 1: Intention Action (Recommended)
1. Open any `.feature` file containing Jira issue tags like `@MBA-1234`
2. Place your cursor anywhere within the `@MBA-XXXXX` tag
3. Press **Alt+Enter** to open the intention actions menu
4. Select "🔗 Open Jira Issue MBA-XXXX"
5. The Jira issue will open in your default browser

#### Method 2: Gutter Icon
1. Open any `.feature` file containing Jira issue tags like `@MBA-1234`
2. Look for the Jira icon (J) in the gutter (left margin) next to lines with Jira tags
3. Click the icon to open the issue directly in your default browser

Both methods will open the issue in your default browser where you can use your existing login session.

### Multiple Issues Handling
When a line contains multiple Jira issue tags (e.g., `@MBA-1234 @MBA-5678`), the plugin provides enhanced functionality:

- **Intention Action**: Shows "🔗 Open Jira Issues" with a popup menu to select which issue to open
- **Gutter Icon**: Shows tooltip with all issues and clicking opens a selection menu
- **Smart Detection**: Cursor position determines which specific issue is selected when using Alt+Enter on a single tag

## Example

### Single Issue
```gherkin
@MBA-1234 @smoke
Feature: User Login
  
  @MBA-5678
  Scenario: Successful login
    Given user is on login page
    When user enters valid credentials
    Then user should be logged in
```

### Multiple Issues on Same Line
```gherkin
@MBA-1234 @MBA-5678 @smoke
Feature: User Login with Multiple References
  
  @MBA-9999 @MBA-1111 @regression
  Scenario: Login validation
    Given user is on login page
    When user enters invalid credentials
    Then user should see error message
```

In the above examples:
- **Single issue**: Place your cursor on `@MBA-1234` and press Alt+Enter to open that specific issue
- **Multiple issues**: Place your cursor anywhere on the line with `@MBA-1234 @MBA-5678` and press Alt+Enter to see a menu with both issues
- **Gutter icons**: Click the Jira icon in the gutter to open the issue (or show menu for multiple issues)

## Requirements
- IntelliJ IDEA
- Network access to your Jira instance
- Default browser configured on your system
- Valid Jira account for viewing issues

## Troubleshooting

### Issues Not Opening
- Check your network connection
- Verify the Jira base URL is correct in Settings > Tools > Jira Integration
- Ensure you have permission to view the specific Jira issue
- Make sure your default browser is properly configured