Feature: Sample Karate Test

  Background:
    * def baseUrl = 'https://jsonplaceholder.typicode.com'
    * configure headers = { 'Content-Type': 'application/json' }

  @smoke @api
  Scenario: Get a user by ID
    Given url baseUrl + '/users/1'
    When method get
    Then status 200
    And match response.name == '#string'
    And match response.email == '#string'

  Scenario: Create a new post
    Given url baseUrl + '/posts'
    And request { title: 'Sample Title', body: 'Sample Body', userId: 1 }
    When method post
    Then status 201
    And match response.id == '#number'

  Scenario Outline: Validate user existence
    Given url baseUrl + '/users/<userId>'
    When method get
    Then status <expectedStatus>

    Examples:
      | userId | expectedStatus |
      | 1      | 200           |
      | 999    | 404           |