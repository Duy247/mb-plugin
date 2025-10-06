Feature: Sample Karate Test Feature

  @smoke @critical
  Scenario: Test login functionality
    Given user is on login page
    When user enters valid credentials
    Then user should be logged in successfully

  @regression @api
  Scenario: Test API endpoint
    Given API endpoint is available
    When GET request is sent to /users
    Then response status should be 200

  @integration
  Scenario: Test database integration
    Given database is connected
    When user data is queried
    Then correct data should be returned