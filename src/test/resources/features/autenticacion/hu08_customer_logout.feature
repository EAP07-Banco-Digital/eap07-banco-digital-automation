# language: en
@hu08 @authentication
Feature: Secure customer sign-out
  As an authenticated customer
  I want to sign out of the digital bank
  So that my private banking information remains protected

  @critical
  Scenario: Sign out and prevent further access using the closed session
    Given I am an authenticated customer
    And I can access my private banking information
    When I request to sign out
    Then my session should be closed successfully
    And I should see the sign-out confirmation message "Sesion cerrada correctamente"
    And I should no longer be able to access my private banking information using the closed session
