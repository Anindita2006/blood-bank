Feature: Testing a JavaScript program with Karate

  Background:
    * def checkDonor = read('donor-eligibility.js')

  Scenario Outline: Donor eligibility for age <age>, weight <weight>, gap <days>
    * def result = checkDonor(<age>, <weight>, <days>)
    * match result == '<expected>'

    Examples:
      | age | weight | days | expected                      |
      | 25  | 60     | 120  | Eligible                      |
      | 16  | 60     | 120  | Not eligible: age             |
      | 30  | 45     | 120  | Not eligible: weight          |
      | 30  | 60     | 30   | Not eligible: donated recently |
