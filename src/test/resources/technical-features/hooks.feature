# language: en
@technical-local
Feature: Technical integration of API hooks

  Scenario: Initialize an API actor without inherited memory
    Given the API scenario is initialized
    When the technical actor remembers "scenario-one-value"
    Then the API actor has the CallAnApi ability and can recall the value

  Scenario: Start the next scenario with clean memory
    Given the API scenario is initialized
    Then the API actor does not inherit memory from the previous scenario
