Feature: Blood Bank API tests

  Background:
    * url baseUrl

  Scenario: App is healthy
    Given path 'health'
    When method get
    Then status 200
    And match response.status == 'UP'

  Scenario: A+ patient in Hyderabad gets all compatible banks
    Given path 'api/search'
    And param city = 'Hyderabad'
    And param group = 'A+'
    When method get
    Then status 200
    # A+ can receive A+, A-, O+, O-  -> Red Cross (O-, A+) and NIMS (O+)
    And match response == '#[3]'
    And match each response contains { city: 'Hyderabad' }

  Scenario: Blood bank adds new stock, then it shows up in search
    Given path 'api/stock'
    And request { bank: 'Apollo', city: 'Chennai', group: 'B-', units: 10 }
    When method post
    Then status 200

    Given path 'api/search'
    And param city = 'Chennai'
    And param group = 'B-'
    When method get
    Then status 200
    And match response[0].bank == 'Apollo'
    And match response[0].low == false

  Scenario: Negative units are rejected
    Given path 'api/stock'
    And request { bank: 'Apollo', city: 'Chennai', group: 'B-', units: -5 }
    When method post
    Then status 400
    And match response.error == 'Units cannot be negative'
