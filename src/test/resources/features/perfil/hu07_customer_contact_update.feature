# language: en
@hu07 @profile
Feature: Customer contact information update
  As an authenticated customer
  I want to update my contact information (phone, email and address)
  So that my information stays up to date with the bank

  @critical @e2e
  Scenario: Update my contact information with valid data
    Given I am a signed-in customer who wants to update my contact information
    And I provide new valid contact information
    When I request to update my contact information
    Then my contact information should be updated
    And my profile should keep the new contact information when I view it again
