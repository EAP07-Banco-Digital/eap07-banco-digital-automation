# language: en
@hu02 @authentication
Feature: Customer authentication and security
  As a registered customer
  I want to sign in with my email and password
  So that I can access my private banking area

  @critical
  Scenario: Sign in with valid credentials
    Given I am a registered customer with an unlocked account
    And I provide valid credentials
    When I request to sign in
    Then my credentials should be accepted
    And I should receive an access token with an expiration date
    And I should be identified with my customer role

  @critical
  Scenario: Temporarily lock an account after five failed sign-in attempts
    Given I am a registered customer with an unlocked account
    And I have made four consecutive failed sign-in attempts
    When I request to sign in again with an incorrect password
    Then my account should be temporarily locked
    And I should see the warning message "Cuenta temporalmente bloqueada"
    And I should not be able to sign in even with valid credentials
