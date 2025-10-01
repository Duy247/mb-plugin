Feature: Test Karate Plugin Functionality

  Background:
    * def baseUrl = 'https://api.example.com'
    * configure headers = { 'Accept': 'application/json' }

  @test @syntax
  Scenario: Test syntax highlighting
    # This is a comment - should be highlighted
    Given url baseUrl + '/test'
    And header Authorization = 'Bearer token123'
    When method get
    Then status 200
    And match response == { success: true }
    
  @variables
  Scenario: Test variable references
    * def testVar = 'hello world'
    * def jsonData = { name: 'test', value: testVar }
    * print jsonData
    
  Scenario Outline: Test table highlighting
    Given url baseUrl + '/users/<id>'
    When method get
    Then status <expectedStatus>
    
    Examples:
      | id | expectedStatus |
      | 1  | 200           |
      | 2  | 200           |
      | 99 | 404           |