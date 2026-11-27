# language: en
@hu04 @profile
Feature: Customer profile consultation
  As an authenticated customer
  I want to view my profile with my personal information
  So that I can verify the information registered in the digital bank

  @critical
  Scenario: View my profile as an authenticated customer
    Given I am a signed-in customer with a registered profile
    When I request to view my profile
    Then I should see my registered personal information
    And my identity document should be partially masked
    And my profile should not expose my password

  Scenario: Try to view a profile without signing in
    Given I am a visitor without an active session
    When I try to view a customer profile
    Then my profile request should be rejected because I am not signed in
    And I should see the sign-in required message "Debes iniciar sesión para acceder a este recurso"
